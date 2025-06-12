package com.sadna_market.market.InfrastructureLayer.Initialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseCleaner {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseCleaner.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public void clearAllTables() {
        logger.info("🧹 Clearing all database tables...");

        try {
            // Your SQL script - much cleaner than repository.clear()!
            String sql = """
                DO $$ DECLARE
                    r RECORD;
                BEGIN
                    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                        EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE';
                    END LOOP;
                END $$;
                """;

            jdbcTemplate.execute(sql);
            logger.info("✅ All tables cleared successfully");

        } catch (Exception e) {
            logger.error("❌ Failed to clear database: {}", e.getMessage(), e);
            throw new RuntimeException("Database clear failed", e);
        }
    }
}
