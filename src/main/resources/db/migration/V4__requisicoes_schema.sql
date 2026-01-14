-- Requisicoes module schema

DO $$ BEGIN
  CREATE TYPE requisition_status AS ENUM ('OPEN', 'READ', 'RESOLVED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS requisitions (
  id                       UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id               UUID              NOT NULL,
  title                    TEXT              NOT NULL,
  description              TEXT              NOT NULL,
  requester_department_id  UUID              NOT NULL,
  target_department_id     UUID              NOT NULL,
  author_user_id           UUID              NOT NULL,
  recipient_user_id        UUID              NULL,
  status                   requisition_status NOT NULL DEFAULT 'OPEN',
  deleted                  BOOLEAN           NOT NULL DEFAULT FALSE,
  created_at               TIMESTAMPTZ       NOT NULL DEFAULT now(),
  updated_at               TIMESTAMPTZ       NOT NULL DEFAULT now(),

  CONSTRAINT fk_requisitions_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_requisitions_requester_department
    FOREIGN KEY (requester_department_id) REFERENCES departments(id) ON DELETE RESTRICT,
  CONSTRAINT fk_requisitions_target_department
    FOREIGN KEY (target_department_id) REFERENCES departments(id) ON DELETE RESTRICT,
  CONSTRAINT fk_requisitions_author_user
    FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_requisitions_recipient_user
    FOREIGN KEY (recipient_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_requisitions_company ON requisitions (company_id);
CREATE INDEX IF NOT EXISTS ix_requisitions_status ON requisitions (status);
CREATE INDEX IF NOT EXISTS ix_requisitions_recipient ON requisitions (recipient_user_id);
CREATE INDEX IF NOT EXISTS ix_requisitions_requester_department ON requisitions (requester_department_id);
CREATE INDEX IF NOT EXISTS ix_requisitions_target_department ON requisitions (target_department_id);
CREATE INDEX IF NOT EXISTS ix_requisitions_company_deleted ON requisitions (company_id, deleted);
