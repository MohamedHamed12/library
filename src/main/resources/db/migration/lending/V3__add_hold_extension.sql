ALTER TABLE hold_database_entity
    ALTER COLUMN till DROP NOT NULL;

ALTER TABLE hold_database_entity
    ADD COLUMN extension_count INTEGER NOT NULL DEFAULT 0;
