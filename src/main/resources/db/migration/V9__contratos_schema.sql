-- Contratos module schema

DO $$ BEGIN
  CREATE TYPE contract_status AS ENUM ('ACTIVE', 'ENDED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS contracts (
  id           UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id   UUID             NOT NULL,
  title        TEXT             NOT NULL,
  description  TEXT             NOT NULL,
  vendor_name  TEXT             NOT NULL,
  start_date   DATE             NOT NULL,
  end_date     DATE             NULL,
  status       contract_status  NOT NULL DEFAULT 'ACTIVE',
  created_at   TIMESTAMPTZ      NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ      NOT NULL DEFAULT now(),

  CONSTRAINT fk_contracts_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_contracts_company ON contracts (company_id);
CREATE INDEX IF NOT EXISTS ix_contracts_status ON contracts (status);
CREATE INDEX IF NOT EXISTS ix_contracts_end_date ON contracts (end_date);

CREATE TABLE IF NOT EXISTS contract_documents (
  id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id   UUID        NOT NULL,
  contract_id  UUID        NOT NULL,
  file_name    TEXT        NOT NULL,
  content_type TEXT        NULL,
  storage_url  TEXT        NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_contract_documents_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_contract_documents_contract
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_contract_documents_company ON contract_documents (company_id);
CREATE INDEX IF NOT EXISTS ix_contract_documents_contract ON contract_documents (contract_id);
CREATE INDEX IF NOT EXISTS ix_contract_documents_company_contract ON contract_documents (company_id, contract_id);
