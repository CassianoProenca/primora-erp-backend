-- Estoque module schema and requisition items

DO $$ BEGIN
  CREATE TYPE stock_movement_type AS ENUM ('IN', 'OUT', 'ADJUST');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE stock_reference_type AS ENUM ('REQUISITION', 'MANUAL', 'PURCHASE');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

CREATE TABLE IF NOT EXISTS stock_items (
  id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id         UUID        NOT NULL,
  sku                TEXT        NOT NULL,
  name               TEXT        NOT NULL,
  unit               TEXT        NOT NULL,
  purchase_unit_cost NUMERIC(12,2) NOT NULL,
  active             BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_stock_items_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_stock_items_company_sku ON stock_items (company_id, lower(sku));
CREATE INDEX IF NOT EXISTS ix_stock_items_company ON stock_items (company_id);

CREATE TABLE IF NOT EXISTS warehouses (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id  UUID        NOT NULL,
  code        TEXT        NOT NULL,
  name        TEXT        NOT NULL,
  active      BOOLEAN     NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_warehouses_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_warehouses_company_code ON warehouses (company_id, lower(code));
CREATE INDEX IF NOT EXISTS ix_warehouses_company ON warehouses (company_id);

CREATE TABLE IF NOT EXISTS stock_levels (
  id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id   UUID        NOT NULL,
  warehouse_id UUID        NOT NULL,
  item_id      UUID        NOT NULL,
  quantity     NUMERIC(14,3) NOT NULL DEFAULT 0,
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_stock_levels_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_stock_levels_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE CASCADE,
  CONSTRAINT fk_stock_levels_item
    FOREIGN KEY (item_id) REFERENCES stock_items(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_stock_levels_unique ON stock_levels (company_id, warehouse_id, item_id);
CREATE INDEX IF NOT EXISTS ix_stock_levels_warehouse ON stock_levels (warehouse_id);

CREATE TABLE IF NOT EXISTS stock_movements (
  id                 UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id         UUID                NOT NULL,
  warehouse_id       UUID                NOT NULL,
  item_id            UUID                NOT NULL,
  type               stock_movement_type NOT NULL,
  quantity           NUMERIC(14,3)       NOT NULL,
  unit_cost          NUMERIC(12,2)       NOT NULL,
  reference_type     stock_reference_type NOT NULL,
  reference_id       UUID                NULL,
  created_by_user_id UUID                NOT NULL,
  created_at         TIMESTAMPTZ         NOT NULL DEFAULT now(),

  CONSTRAINT fk_stock_movements_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_stock_movements_warehouse
    FOREIGN KEY (warehouse_id) REFERENCES warehouses(id) ON DELETE RESTRICT,
  CONSTRAINT fk_stock_movements_item
    FOREIGN KEY (item_id) REFERENCES stock_items(id) ON DELETE RESTRICT,
  CONSTRAINT fk_stock_movements_user
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_stock_movements_company ON stock_movements (company_id);
CREATE INDEX IF NOT EXISTS ix_stock_movements_warehouse ON stock_movements (warehouse_id);
CREATE INDEX IF NOT EXISTS ix_stock_movements_item ON stock_movements (item_id);
CREATE INDEX IF NOT EXISTS ix_stock_movements_reference ON stock_movements (reference_type, reference_id);

CREATE TABLE IF NOT EXISTS requisition_items (
  id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  requisition_id  UUID        NOT NULL,
  item_id         UUID        NOT NULL,
  quantity        NUMERIC(14,3) NOT NULL,

  CONSTRAINT fk_requisition_items_requisition
    FOREIGN KEY (requisition_id) REFERENCES requisitions(id) ON DELETE CASCADE,
  CONSTRAINT fk_requisition_items_item
    FOREIGN KEY (item_id) REFERENCES stock_items(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS ix_requisition_items_requisition ON requisition_items (requisition_id);
CREATE INDEX IF NOT EXISTS ix_requisition_items_item ON requisition_items (item_id);
