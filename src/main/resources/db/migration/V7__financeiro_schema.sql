-- Financeiro module schema

DO $$ BEGIN
  CREATE TYPE financial_entry_type AS ENUM ('EXPENSE', 'INCOME');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE financial_reference_type AS ENUM ('STOCK_MOVEMENT', 'MANUAL');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS financial_categories (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id  UUID        NOT NULL,
  code        TEXT        NOT NULL,
  name        TEXT        NOT NULL,
  is_system   BOOLEAN     NOT NULL DEFAULT FALSE,
  active      BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_financial_categories_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_financial_categories_company_code
  ON financial_categories (company_id, lower(code));
CREATE INDEX IF NOT EXISTS ix_financial_categories_company
  ON financial_categories (company_id);

CREATE TABLE IF NOT EXISTS financial_entries (
  id             UUID                   PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id     UUID                   NOT NULL,
  type           financial_entry_type   NOT NULL,
  description    TEXT                   NOT NULL,
  amount         NUMERIC(14,2)          NOT NULL,
  currency       TEXT                   NOT NULL,
  entry_date     DATE                   NOT NULL,
  category_id    UUID                   NULL,
  department_id  UUID                   NULL,
  cost_center_id UUID                   NULL,
  reference_type financial_reference_type NOT NULL,
  reference_id   UUID                   NULL,
  created_at     TIMESTAMPTZ            NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ            NOT NULL DEFAULT now(),

  CONSTRAINT fk_financial_entries_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_financial_entries_category
    FOREIGN KEY (category_id) REFERENCES financial_categories(id) ON DELETE SET NULL,
  CONSTRAINT fk_financial_entries_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL,
  CONSTRAINT fk_financial_entries_cost_center
    FOREIGN KEY (cost_center_id) REFERENCES cost_centers(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_financial_entries_company ON financial_entries (company_id);
CREATE INDEX IF NOT EXISTS ix_financial_entries_type ON financial_entries (type);
CREATE INDEX IF NOT EXISTS ix_financial_entries_date ON financial_entries (entry_date);
CREATE INDEX IF NOT EXISTS ix_financial_entries_reference ON financial_entries (reference_type, reference_id);
