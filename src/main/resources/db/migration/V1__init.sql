CREATE TABLE incidents (
    id                BIGSERIAL PRIMARY KEY,
    title             TEXT        NOT NULL,
    description       TEXT,
    severity          VARCHAR(10) NOT NULL CHECK (severity IN ('P0','P1','P2','P3')),
    status            VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    affected_services TEXT,
    incident_commander TEXT,
    detected_at       TIMESTAMPTZ NOT NULL,
    resolved_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE timeline_events (
    id          BIGSERIAL PRIMARY KEY,
    incident_id BIGINT      NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    description TEXT        NOT NULL,
    author      TEXT,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE post_mortems (
    id                   BIGSERIAL PRIMARY KEY,
    incident_id          BIGINT      NOT NULL UNIQUE REFERENCES incidents(id) ON DELETE CASCADE,
    status               VARCHAR(20) NOT NULL DEFAULT 'GENERATING',
    summary              TEXT,
    root_cause           TEXT,
    contributing_factors TEXT,
    impact               TEXT,
    timeline             TEXT,
    lessons_learned      TEXT,
    failure_message      TEXT,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE action_items (
    id           BIGSERIAL PRIMARY KEY,
    incident_id  BIGINT  NOT NULL REFERENCES incidents(id) ON DELETE CASCADE,
    description  TEXT    NOT NULL,
    owner        TEXT,
    due_date     DATE,
    completed    BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_incidents_status   ON incidents(status);
CREATE INDEX idx_incidents_severity ON incidents(severity);
CREATE INDEX idx_incidents_detected ON incidents(detected_at DESC);
CREATE INDEX idx_timeline_incident  ON timeline_events(incident_id, occurred_at);
CREATE INDEX idx_action_items_incident ON action_items(incident_id);
CREATE INDEX idx_action_items_open  ON action_items(completed, due_date) WHERE completed = FALSE;
