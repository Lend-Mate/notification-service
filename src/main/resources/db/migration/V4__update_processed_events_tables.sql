ALTER TABLE processed_events
DROP CONSTRAINT processed_events_pkey;

ALTER TABLE processed_events
    ADD COLUMN id BIGSERIAL PRIMARY KEY;

ALTER TABLE processed_events
    ADD CONSTRAINT uq_processed_events_event_id UNIQUE (event_id);