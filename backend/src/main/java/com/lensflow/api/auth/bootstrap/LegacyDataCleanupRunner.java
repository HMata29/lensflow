package com.lensflow.api.auth.bootstrap;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class LegacyDataCleanupRunner implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;

    public LegacyDataCleanupRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Rimuove esclusivamente i record pre-multi-tenant, cioè privi di proprietario.
        jdbcTemplate.update("DELETE FROM payments WHERE shooting_id IN (SELECT id FROM shootings WHERE user_id IS NULL)");
        jdbcTemplate.update("DELETE FROM shootings WHERE user_id IS NULL");
        jdbcTemplate.update("DELETE FROM quote_items WHERE quote_id IN (SELECT id FROM quotes WHERE user_id IS NULL)");
        jdbcTemplate.update("DELETE FROM quotes WHERE user_id IS NULL");
        jdbcTemplate.update("DELETE FROM clients WHERE user_id IS NULL");

        jdbcTemplate.execute("ALTER TABLE clients ALTER COLUMN user_id SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE quotes ALTER COLUMN user_id SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE shootings ALTER COLUMN user_id SET NOT NULL");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_clients_user_id ON clients(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_quotes_user_id ON quotes(user_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS ix_shootings_user_id ON shootings(user_id)");
        jdbcTemplate.execute("ALTER TABLE quotes DROP CONSTRAINT IF EXISTS quotes_quote_number_key");
        jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_quotes_user_quote_number ON quotes(user_id, quote_number)");
    }
}
