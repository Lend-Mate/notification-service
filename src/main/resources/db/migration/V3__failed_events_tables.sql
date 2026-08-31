CREATE TABLE failed_events (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               event_id UUID NOT NULL,
                               order_id BIGINT,
                               original_topic VARCHAR(255) NOT NULL,
                               payload JSONB NOT NULL,
                               exception_message TEXT,
                               failed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                               retried_at TIMESTAMPTZ,
                               status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);

CREATE INDEX idx_failed_events_status ON failed_events (status);
CREATE INDEX idx_failed_events_event_id ON failed_events (event_id);