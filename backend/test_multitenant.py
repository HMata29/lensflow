from __future__ import annotations

import os
import sys
import uuid
from datetime import date, datetime, timedelta
from urllib.parse import unquote

import requests

BASE_URL = os.getenv("LENSFLOW_API_URL", "http://localhost:8080").rstrip("/")
TIMEOUT = 20
CREATED = {
    "clients": {"admin": [], "photographer": []},
    "quotes": {"admin": [], "photographer": []},
    "shootings": {"admin": [], "photographer": []},
    "payments": {"admin": [], "photographer": []},
}
SESSIONS = {}


def check(condition: bool, message: str) -> None:
    print(f"[{'PASS' if condition else 'FAIL'}] {message}")
    if not condition:
        raise AssertionError(message)


def csrf_token(session: requests.Session) -> str:
    response = session.get(f"{BASE_URL}/api/auth/csrf", timeout=TIMEOUT)
    check(response.status_code in (200, 204),
          f"CSRF disponibile/aggiornato (HTTP {response.status_code})")
    token = unquote(session.cookies.get("XSRF-TOKEN", ""))
    check(bool(token), "Cookie XSRF-TOKEN ricevuto")
    return token


def login(label: str, email: str, password: str) -> requests.Session:
    session = requests.Session()
    token = csrf_token(session)
    response = session.post(
        f"{BASE_URL}/api/auth/login",
        json={"email": email, "password": password},
        headers={"X-XSRF-TOKEN": token},
        timeout=TIMEOUT,
    )
    check(response.status_code == 200,
          f"Login {label} riuscito (HTTP {response.status_code})")
    # Il flusso già verificato dal test precedente rinnova il token dopo il login.
    csrf_token(session)
    SESSIONS[label] = session
    return session


def request(session, method: str, path: str, *, json_body=None):
    headers = {}
    if method.upper() in {"POST", "PUT", "PATCH", "DELETE"}:
        headers["X-XSRF-TOKEN"] = unquote(
            session.cookies.get("XSRF-TOKEN", "")
        )
    return session.request(
        method,
        f"{BASE_URL}{path}",
        json=json_body,
        headers=headers,
        timeout=TIMEOUT,
    )


def body_id(response, resource: str) -> int:
    try:
        value = response.json().get("id")
    except (ValueError, AttributeError):
        value = None
    check(value is not None, f"ID {resource} restituito dal backend")
    return value


def expect_forbidden(response, description: str):
    check(response.status_code in (403, 404),
          f"{description} (HTTP {response.status_code})")


def make_client(session, owner, marker):
    payload = {
        "firstName": "LensFlow",
        "lastName": f"Security-{owner}-{marker}",
        "email": f"lf-{owner}-{marker}@example.com",
        "phone": "0000000000",
        "company": "LensFlow Multi-tenant Test",
        "notes": "Creato automaticamente dal test multi-tenant",
    }
    response = request(session, "POST", "/api/clients", json_body=payload)
    check(response.status_code == 201,
          f"Creazione cliente {owner} (HTTP {response.status_code})")
    entity_id = body_id(response, "cliente")
    CREATED["clients"][owner].append(entity_id)
    return entity_id


def make_quote(session, owner, marker, client_id):
    payload = {
        "title": f"Preventivo test {owner} {marker}",
        "notes": "Creato automaticamente dal test multi-tenant",
        "issueDate": date.today().isoformat(),
        "expiryDate": (date.today() + timedelta(days=30)).isoformat(),
        "status": "ACCEPTED",
        "clientId": client_id,
        "items": [
            {
                "description": "Servizio fotografico test",
                "quantity": 1,
                "unitPrice": 150.00,
            }
        ],
    }
    response = request(session, "POST", "/api/quotes", json_body=payload)
    check(response.status_code == 201,
          f"Creazione preventivo {owner} (HTTP {response.status_code}): {response.text[:300]}")
    entity_id = body_id(response, "preventivo")
    CREATED["quotes"][owner].append(entity_id)
    return entity_id


def make_shooting(session, owner, marker, client_id, quote_id):
    start = (datetime.now() + timedelta(days=7)).replace(
        hour=10, minute=0, second=0, microsecond=0
    )
    end = start + timedelta(hours=2)
    payload = {
        "title": f"Shooting test {owner} {marker}",
        "category": "TEST",
        "startAt": start.isoformat(timespec="seconds"),
        "endAt": end.isoformat(timespec="seconds"),
        "location": "LensFlow Security Test",
        "status": "PLANNED",
        "notes": "Creato automaticamente dal test multi-tenant",
        "clientId": client_id,
        "quoteId": quote_id,
    }
    response = request(session, "POST", "/api/shootings", json_body=payload)
    check(response.status_code == 201,
          f"Creazione shooting {owner} (HTTP {response.status_code}): {response.text[:300]}")
    entity_id = body_id(response, "shooting")
    CREATED["shootings"][owner].append(entity_id)
    return entity_id


def make_payment(session, owner, marker, shooting_id):
    payload = {
        "amount": 50.00,
        "paymentDate": date.today().isoformat(),
        "method": "BANK_TRANSFER",
        "type": "DEPOSIT",
        "status": "RECEIVED",
        "notes": f"Pagamento test {marker}",
        "shootingId": shooting_id,
    }
    response = request(session, "POST", "/api/payments", json_body=payload)
    check(response.status_code == 201,
          f"Creazione pagamento {owner} (HTTP {response.status_code}): {response.text[:300]}")
    entity_id = body_id(response, "pagamento")
    CREATED["payments"][owner].append(entity_id)
    return entity_id


def verify_list_isolation(session, owner, resource, own_id, foreign_id, path):
    response = request(session, "GET", path)
    check(response.status_code == 200,
          f"{owner}: lista {resource} accessibile (HTTP {response.status_code})")
    items = response.json()
    ids = [item.get("id") for item in items]
    check(own_id in ids, f"{owner}: {resource} proprio presente nella lista")
    check(foreign_id not in ids, f"{owner}: {resource} altrui assente dalla lista")


def verify_direct_read_denied(session, owner, resource, foreign_id, path):
    response = request(session, "GET", f"{path}/{foreign_id}")
    expect_forbidden(response, f"{owner}: lettura diretta {resource} altrui negata")


def cleanup():
    print("\n=== PULIZIA DEI SOLI RECORD CREATI DAL TEST ===")
    # Ordine dipendenze: pagamenti -> shooting -> preventivi -> clienti.
    for resource, path in (
        ("payments", "/api/payments"),
        ("shootings", "/api/shootings"),
        ("quotes", "/api/quotes"),
        ("clients", "/api/clients"),
    ):
        for owner in ("admin", "photographer"):
            session = SESSIONS.get(owner)
            if not session:
                continue
            for entity_id in reversed(CREATED[resource][owner]):
                try:
                    response = request(session, "DELETE", f"{path}/{entity_id}")
                    if response.status_code in (200, 202, 204, 404):
                        print(f"[CLEAN] {resource} ID {entity_id} ({owner}) - HTTP {response.status_code}")
                    else:
                        print(f"[WARN] Pulizia {resource} ID {entity_id} ({owner}) - HTTP {response.status_code}: {response.text[:200]}")
                except Exception as error:
                    print(f"[WARN] Pulizia {resource} ID {entity_id} ({owner}) non riuscita: {error}")


def main():
    admin_email = os.getenv("LENSFLOW_ADMIN_EMAIL")
    admin_password = os.getenv("LENSFLOW_ADMIN_PASSWORD")
    photographer_email = os.getenv("LENSFLOW_TEST_EMAIL")
    photographer_password = os.getenv("LENSFLOW_TEST_PASSWORD")

    check(all((admin_email, admin_password, photographer_email, photographer_password)),
          "Credenziali configurate (password lette dalle variabili d'ambiente)")

    print("\n=== TEST MULTI-TENANT COMPLETO LENSFLOW ===")
    print(f"API: {BASE_URL}\n")

    admin = login("admin", admin_email, admin_password)
    photographer = login("photographer", photographer_email, photographer_password)
    marker = uuid.uuid4().hex[:8]

    try:
        # 1. Crea un set indipendente di dati per ciascun fotografo.
        admin_client = make_client(admin, "admin", marker)
        photographer_client = make_client(photographer, "photographer", marker)

        admin_quote = make_quote(admin, "admin", marker, admin_client)
        photographer_quote = make_quote(
            photographer, "photographer", marker, photographer_client
        )

        admin_shooting = make_shooting(
            admin, "admin", marker, admin_client, admin_quote
        )
        photographer_shooting = make_shooting(
            photographer, "photographer", marker,
            photographer_client, photographer_quote
        )

        admin_payment = make_payment(admin, "admin", marker, admin_shooting)
        photographer_payment = make_payment(
            photographer, "photographer", marker, photographer_shooting
        )

        # 2. Verifica isolamento delle liste.
        verify_list_isolation(
            admin, "ADMIN", "clienti", admin_client, photographer_client, "/api/clients"
        )
        verify_list_isolation(
            photographer, "Fotografo", "clienti",
            photographer_client, admin_client, "/api/clients"
        )
        verify_list_isolation(
            admin, "ADMIN", "preventivi", admin_quote, photographer_quote, "/api/quotes"
        )
        verify_list_isolation(
            photographer, "Fotografo", "preventivi",
            photographer_quote, admin_quote, "/api/quotes"
        )
        verify_list_isolation(
            admin, "ADMIN", "shooting", admin_shooting,
            photographer_shooting, "/api/shootings"
        )
        verify_list_isolation(
            photographer, "Fotografo", "shooting",
            photographer_shooting, admin_shooting, "/api/shootings"
        )
        verify_list_isolation(
            admin, "ADMIN", "pagamenti", admin_payment,
            photographer_payment, "/api/payments"
        )
        verify_list_isolation(
            photographer, "Fotografo", "pagamenti",
            photographer_payment, admin_payment, "/api/payments"
        )

        # 3. Verifica accesso diretto tramite ID.
        for session, owner, foreign_id, path, resource in (
            (admin, "ADMIN", photographer_client, "/api/clients", "cliente"),
            (photographer, "Fotografo", admin_client, "/api/clients", "cliente"),
            (admin, "ADMIN", photographer_quote, "/api/quotes", "preventivo"),
            (photographer, "Fotografo", admin_quote, "/api/quotes", "preventivo"),
            (admin, "ADMIN", photographer_shooting, "/api/shootings", "shooting"),
            (photographer, "Fotografo", admin_shooting, "/api/shootings", "shooting"),
            (admin, "ADMIN", photographer_payment, "/api/payments", "pagamento"),
            (photographer, "Fotografo", admin_payment, "/api/payments", "pagamento"),
        ):
            verify_direct_read_denied(session, owner, resource, foreign_id, path)

        # 4. Verifica che non si possano modificare o cancellare record altrui.
        foreign_client_update = {
            "firstName": "HACKED",
            "lastName": f"CrossTenant-{marker}",
            "email": f"cross-{marker}@example.com",
            "phone": "0000000000",
            "company": "Should not be saved",
            "notes": "Tentativo cross-tenant",
        }
        for session, owner, foreign_id, path, payload, resource in (
            (photographer, "Fotografo", admin_client, "/api/clients",
             foreign_client_update, "cliente"),
            (photographer, "Fotografo", admin_quote, "/api/quotes",
             {
                 "title": "Preventivo modificato da altro utente",
                 "issueDate": date.today().isoformat(),
                 "expiryDate": (date.today() + timedelta(days=30)).isoformat(),
                 "status": "DRAFT",
                 "clientId": photographer_client,
                 "items": [
                     {
                         "description": "Test modifica cross-tenant",
                         "quantity": 1,
                         "unitPrice": 10.00
                     }
                 ]
             }, "preventivo"),
            (photographer, "Fotografo", admin_shooting, "/api/shootings",
             {
                 "title": "Shooting modificato da altro utente",
                 "category": "TEST",
                 "startAt": (datetime.now() + timedelta(days=8)).replace(
                     hour=10, minute=0, second=0, microsecond=0
                 ).isoformat(timespec="seconds"),
                 "endAt": (datetime.now() + timedelta(days=8)).replace(
                     hour=12, minute=0, second=0, microsecond=0
                 ).isoformat(timespec="seconds"),
                 "status": "PLANNED", "clientId": photographer_client,
                 "quoteId": photographer_quote
             }, "shooting"),
            (photographer, "Fotografo", admin_payment, "/api/payments",
             {
                 "amount": 75.00, "paymentDate": date.today().isoformat(),
                 "method": "CASH", "type": "INSTALLMENT", "status": "PENDING",
                 "shootingId": photographer_shooting
             }, "pagamento"),
        ):
            response = request(session, "PUT", f"{path}/{foreign_id}", json_body=payload)
            expect_forbidden(response, f"{owner}: modifica {resource} altrui negata")

        # 5. Relazioni cross-tenant: non deve essere possibile collegare risorse altrui.
        bad_quote = make_quote_attempt = request(
            photographer, "POST", "/api/quotes",
            json_body={
                "title": f"Preventivo con cliente altrui {marker}",
                "issueDate": date.today().isoformat(),
                "expiryDate": (date.today() + timedelta(days=30)).isoformat(),
                "status": "DRAFT", "clientId": admin_client,
                "items": [{"description": "Test", "quantity": 1, "unitPrice": 10.00}],
            },
        )
        if make_quote_attempt.status_code == 201:
            CREATED["quotes"]["photographer"].append(body_id(make_quote_attempt, "preventivo anomalo"))
        expect_forbidden(bad_quote, "Creazione preventivo con cliente altrui bloccata")

        bad_shooting_payload = {
            "title": f"Shooting con cliente altrui {marker}",
            "category": "TEST",
            "startAt": (datetime.now() + timedelta(days=9)).replace(
                hour=10, minute=0, second=0, microsecond=0
            ).isoformat(timespec="seconds"),
            "endAt": (datetime.now() + timedelta(days=9)).replace(
                hour=12, minute=0, second=0, microsecond=0
            ).isoformat(timespec="seconds"),
            "status": "PLANNED",
            "clientId": admin_client,
            "quoteId": photographer_quote,
        }
        bad_shooting = request(
            photographer, "POST", "/api/shootings", json_body=bad_shooting_payload
        )
        if bad_shooting.status_code == 201:
            CREATED["shootings"]["photographer"].append(body_id(bad_shooting, "shooting anomalo"))
        expect_forbidden(bad_shooting, "Creazione shooting con cliente altrui bloccata")

        bad_payment = request(
            photographer, "POST", "/api/payments",
            json_body={
                "amount": 10.00, "paymentDate": date.today().isoformat(),
                "method": "CASH", "type": "DEPOSIT", "status": "RECEIVED",
                "shootingId": admin_shooting,
            },
        )
        if bad_payment.status_code == 201:
            CREATED["payments"]["photographer"].append(body_id(bad_payment, "pagamento anomalo"))
        expect_forbidden(bad_payment, "Creazione pagamento su shooting altrui bloccata")

        # 6. Verifica endpoint pagamenti per shooting e riepilogo finanziario.
        for session, owner, shooting_id in (
            (admin, "ADMIN", admin_shooting),
            (photographer, "Fotografo", photographer_shooting),
        ):
            response = request(session, "GET", f"/api/payments/shooting/{shooting_id}")
            check(response.status_code == 200,
                  f"{owner}: pagamenti del proprio shooting accessibili")
            check(any(p.get("id") == (admin_payment if owner == "ADMIN" else photographer_payment)
                      for p in response.json()),
                  f"{owner}: pagamento presente nel proprio shooting")

            response = request(session, "GET", f"/api/payments/shooting/{shooting_id}/summary")
            check(response.status_code == 200,
                  f"{owner}: riepilogo finanziario del proprio shooting accessibile")

        response = request(
            photographer, "GET", f"/api/payments/shooting/{admin_shooting}"
        )
        expect_forbidden(response, "Accesso ai pagamenti dello shooting altrui negato")

        response = request(
            photographer, "GET", f"/api/payments/shooting/{admin_shooting}/summary"
        )
        expect_forbidden(response, "Accesso al riepilogo dello shooting altrui negato")

        # 7. Stato galleria: modifica il proprio shooting e prova quello altrui.
        response = request(
            admin, "PATCH", f"/api/shootings/{admin_shooting}/gallery-status",
            json_body={"galleryStatus": "IN_PROGRESS"},
        )
        check(response.status_code == 200,
              f"ADMIN può aggiornare lo stato galleria del proprio shooting (HTTP {response.status_code})")

        response = request(
            photographer, "PATCH",
            f"/api/shootings/{admin_shooting}/gallery-status",
            json_body={"galleryStatus": "ARCHIVED"},
        )
        expect_forbidden(response, "Modifica stato galleria di shooting altrui negata")

        # 8. Cancellazione cross-tenant: deve essere negata e il record deve restare.
        for path, foreign_id, resource in (
            ("/api/clients", admin_client, "cliente"),
            ("/api/quotes", admin_quote, "preventivo"),
            ("/api/shootings", admin_shooting, "shooting"),
            ("/api/payments", admin_payment, "pagamento"),
        ):
            response = request(photographer, "DELETE", f"{path}/{foreign_id}")
            expect_forbidden(response, f"Fotografo: cancellazione {resource} altrui negata")

        print("\n=== RISULTATO ===")
        print("Tutti i controlli multi-tenant sono stati superati.")
        print("Sono stati verificati clienti, preventivi, shooting, pagamenti, relazioni e gallerie.")

    finally:
        cleanup()


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"\nTEST INTERROTTO: {error}")
        sys.exit(1)
