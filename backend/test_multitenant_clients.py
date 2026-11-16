from urllib.parse import unquote
import os
import sys
import uuid
import requests

BASE_URL = os.getenv("LENSFLOW_API_URL", "http://localhost:8080")
TIMEOUT = 15

def refresh_csrf(session):
    response = session.get(
        f"{BASE_URL}/api/auth/csrf",
        timeout=TIMEOUT
    )

    check(
        response.status_code in (200, 204),
        f"Refresh CSRF (HTTP {response.status_code})"
    )

    token = session.cookies.get("XSRF-TOKEN")

    check(bool(token), "Nuovo cookie CSRF ricevuto")

    return unquote(token)

def check(condition, message):
    symbol = "PASS" if condition else "FAIL"
    print(f"[{symbol}] {message}")
    if not condition:
        raise AssertionError(message)


def login(email, password):
    session = requests.Session()

    # Recupera il token CSRF
    response = session.get(
        f"{BASE_URL}/api/auth/csrf",
        timeout=TIMEOUT
    )
    check(
        response.status_code in (200, 204),
        f"CSRF disponibile per {email} (HTTP {response.status_code})"
    )

    csrf_token = unquote(session.cookies.get("XSRF-TOKEN", ""))

    check(
        bool(csrf_token),
        "Cookie XSRF-TOKEN ricevuto"
    )

    # Effettua il login
    response = session.post(
        f"{BASE_URL}/api/auth/login",
        json={
            "email": email,
            "password": password
        },
        headers={
            "X-XSRF-TOKEN": csrf_token
        },
        timeout=TIMEOUT
    )

    check(
        response.status_code == 200,
        f"Login riuscito per {email} (HTTP {response.status_code})"
    )

    return session


def main():
    admin_email = os.getenv("LENSFLOW_ADMIN_EMAIL")
    admin_password = os.getenv("LENSFLOW_ADMIN_PASSWORD")
    photographer_email = os.getenv("LENSFLOW_TEST_EMAIL")
    photographer_password = os.getenv("LENSFLOW_TEST_PASSWORD")

    check(
        all([
            admin_email,
            admin_password,
            photographer_email,
            photographer_password
        ]),
        "Credenziali configurate"
    )

    print("\n=== TEST MULTI-TENANT LENSFLOW ===\n")

    # 1. Login ADMIN
    admin = login(admin_email, admin_password)
    admin_csrf = refresh_csrf(admin)

    # 2. Crea cliente di prova
    unique_id = uuid.uuid4().hex[:8]

    test_client = {
        "firstName": "Test",
        "lastName": f"Admin-{unique_id}",
        "email": f"test-{unique_id}@example.com",
        "phone": "0000000000",
        "company": "LensFlow Security Test",
        "notes": "Record creato automaticamente dal test multi-tenant"
    }

    response = admin.post(
        f"{BASE_URL}/api/clients",
        json=test_client,
        headers={
            "X-XSRF-TOKEN": admin_csrf
        },
        timeout=TIMEOUT
    )

    check(
        response.status_code == 201,
        f"Creazione cliente ADMIN (HTTP {response.status_code})"
    )

    client_data = response.json()
    client_id = client_data.get("id")

    check(client_id is not None, "ID cliente restituito dal backend")

    # 3. Verifica che ADMIN possa vedere il cliente
    response = admin.get(
        f"{BASE_URL}/api/clients",
        timeout=TIMEOUT
    )

    check(response.status_code == 200, "ADMIN può consultare i clienti")

    admin_clients = response.json()

    check(
        any(client.get("id") == client_id for client in admin_clients),
        "Il cliente creato è visibile all'ADMIN"
    )

    # 4. Login secondo fotografo
    photographer = login(
        photographer_email,
        photographer_password
    )

    # 5. Verifica isolamento lista
    response = photographer.get(
        f"{BASE_URL}/api/clients",
        timeout=TIMEOUT
    )

    check(
        response.status_code == 200,
        "Il secondo fotografo può consultare i propri clienti"
    )

    photographer_clients = response.json()

    check(
        all(client.get("id") != client_id for client in photographer_clients),
        "Il cliente ADMIN NON compare nella lista del secondo fotografo"
    )

    # 6. Tentativo di accesso diretto
    response = photographer.get(
        f"{BASE_URL}/api/clients/{client_id}",
        timeout=TIMEOUT
    )

    check(
        response.status_code in (403, 404),
        f"Accesso diretto al cliente altrui negato (HTTP {response.status_code})"
    )

    print("\n=== RISULTATO ===")
    print("Tutti i controlli sono stati superati.")
    print(f"Cliente di prova creato: ID {client_id}")
    print("Il record è stato lasciato nel database.")
    print("Non è stata eseguita alcuna eliminazione.")


if __name__ == "__main__":
    try:
        main()
    except Exception as error:
        print(f"\nTEST INTERROTTO: {error}")
        sys.exit(1)

def refresh_csrf(session):
    response = session.get(
        f"{BASE_URL}/api/auth/csrf",
        timeout=TIMEOUT
    )

    check(
        response.status_code in (200, 204),
        f"Refresh CSRF (HTTP {response.status_code})"
    )

    token = session.cookies.get("XSRF-TOKEN")

    check(bool(token), "Nuovo cookie CSRF ricevuto")

    return token