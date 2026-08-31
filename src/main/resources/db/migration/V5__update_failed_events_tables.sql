ALTER TABLE failed_events
DROP CONSTRAINT failed_events_pkey;

ALTER TABLE failed_events
DROP COLUMN id;

ALTER TABLE failed_events
    ADD COLUMN id BIGSERIAL PRIMARY KEY;