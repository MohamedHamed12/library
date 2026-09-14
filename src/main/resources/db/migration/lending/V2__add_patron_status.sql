ALTER TABLE patron_database_entity
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE patron_database_entity
    ADD COLUMN suspension_reason VARCHAR(1024);
