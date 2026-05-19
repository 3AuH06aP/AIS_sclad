package com.example.aisstock.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Hibernate ddl-auto=update не обновляет CHECK-constraint в PostgreSQL.
 * Старый volume мог содержать ограничение без роли STOREKEEPER.
 */
@Component
@Order(0)
public class UsersRoleConstraintMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public UsersRoleConstraintMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
        jdbcTemplate.execute("""
                ALTER TABLE users ADD CONSTRAINT users_role_check
                CHECK (role IN ('ADMIN', 'STOREKEEPER', 'USER'))
                """);
    }
}
