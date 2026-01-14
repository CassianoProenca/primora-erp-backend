-- Comunicados module schema

DO $$ BEGIN
  CREATE TYPE comunicado_status AS ENUM ('SENT', 'RECEIVED', 'READ');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS comunicados (
  id                      UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id              UUID             NOT NULL,
  title                   TEXT             NOT NULL,
  message                 TEXT             NOT NULL,
  requester_department_id UUID             NOT NULL,
  target_department_id    UUID             NOT NULL,
  author_user_id          UUID             NOT NULL,
  recipient_user_id       UUID             NULL,
  status                  comunicado_status NOT NULL DEFAULT 'SENT',
  created_at              TIMESTAMPTZ      NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ      NOT NULL DEFAULT now(),

  CONSTRAINT fk_comunicados_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_comunicados_requester_department
    FOREIGN KEY (requester_department_id) REFERENCES departments(id) ON DELETE RESTRICT,
  CONSTRAINT fk_comunicados_target_department
    FOREIGN KEY (target_department_id) REFERENCES departments(id) ON DELETE RESTRICT,
  CONSTRAINT fk_comunicados_author_user
    FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_comunicados_recipient_user
    FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_comunicados_company ON comunicados (company_id);
CREATE INDEX IF NOT EXISTS ix_comunicados_status ON comunicados (status);
CREATE INDEX IF NOT EXISTS ix_comunicados_recipient ON comunicados (recipient_user_id);
