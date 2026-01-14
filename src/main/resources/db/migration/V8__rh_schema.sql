-- RH module schema

DO $$ BEGIN
  CREATE TYPE employee_status AS ENUM ('ACTIVE', 'INACTIVE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE contract_status AS ENUM ('ACTIVE', 'ENDED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS employees (
  id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id    UUID          NOT NULL,
  name          TEXT          NOT NULL,
  email         TEXT          NULL,
  document      TEXT          NULL,
  department_id UUID          NULL,
  status        employee_status NOT NULL DEFAULT 'ACTIVE',
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),

  CONSTRAINT fk_employees_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_employees_department
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_employees_company ON employees (company_id);
CREATE INDEX IF NOT EXISTS ix_employees_department ON employees (department_id);

CREATE TABLE IF NOT EXISTS employment_contracts (
  id             UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id     UUID             NOT NULL,
  employee_id    UUID             NOT NULL,
  title          TEXT             NOT NULL,
  start_date     DATE             NOT NULL,
  end_date       DATE             NULL,
  monthly_salary NUMERIC(14,2)    NOT NULL,
  status         contract_status  NOT NULL DEFAULT 'ACTIVE',
  created_at     TIMESTAMPTZ      NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ      NOT NULL DEFAULT now(),

  CONSTRAINT fk_contracts_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_contracts_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_contracts_company ON employment_contracts (company_id);
CREATE INDEX IF NOT EXISTS ix_contracts_employee ON employment_contracts (employee_id);

CREATE TABLE IF NOT EXISTS benefits (
  id           UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id   UUID          NOT NULL,
  employee_id  UUID          NOT NULL,
  name         TEXT          NOT NULL,
  amount       NUMERIC(14,2) NOT NULL,
  active       BOOLEAN       NOT NULL DEFAULT TRUE,
  created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),

  CONSTRAINT fk_benefits_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_benefits_employee
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_benefits_company ON benefits (company_id);
CREATE INDEX IF NOT EXISTS ix_benefits_employee ON benefits (employee_id);
