-- V1__initial_schema.sql
-- Primora ERP (PostgreSQL) - Initial schema for SaaS core (auth/iam/saas/onboarding + shared)
-- Assumptions:
-- - Shared DB + multi-tenant via company_id on business tables
-- - 1 user = 1 company (enforced via UNIQUE(user_id) in user_companies)
-- - JWT access tokens are stateless (not stored). Refresh/reset/onboarding tokens are stored hashed.

-- Enable UUID generation (gen_random_uuid)
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- Enums
-- =========================
DO $$ BEGIN
  CREATE TYPE user_type AS ENUM ('TENANT_USER', 'SAAS_OWNER', 'SAAS_SUPPORT');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE user_status AS ENUM ('PENDING_PAYMENT', 'ACTIVE', 'LOCKED', 'DISABLED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE onboarding_status AS ENUM ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE subscription_status AS ENUM ('TRIAL', 'ACTIVE', 'PAST_DUE', 'CANCELED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE token_created_by AS ENUM ('SELF_SERVICE', 'SUPPORT', 'SYSTEM');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

DO $$ BEGIN
  CREATE TYPE email_status AS ENUM ('QUEUED', 'SENT', 'FAILED');
EXCEPTION WHEN duplicate_object THEN NULL; END $$;

-- =========================
-- Users (Auth / Identity)
-- =========================
CREATE TABLE IF NOT EXISTS users (
  id                 UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  email              TEXT        NOT NULL,
  name               TEXT        NOT NULL,
  password_hash      TEXT        NOT NULL,
  user_type          user_type   NOT NULL DEFAULT 'TENANT_USER',
  status             user_status NOT NULL DEFAULT 'PENDING_PAYMENT',

  -- bookkeeping
  created_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at         TIMESTAMPTZ NOT NULL DEFAULT now(),
  last_login_at      TIMESTAMPTZ NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_email ON users (lower(email));
CREATE INDEX IF NOT EXISTS ix_users_type ON users (user_type);
CREATE INDEX IF NOT EXISTS ix_users_status ON users (status);

-- =========================
-- Companies (Tenants)
-- =========================
CREATE TABLE IF NOT EXISTS companies (
  id                      UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
  legal_name              TEXT             NULL,
  trade_name              TEXT             NULL,
  document                TEXT             NULL, -- CNPJ/CPF optional
  status                  TEXT             NOT NULL DEFAULT 'ACTIVE', -- keep simple for now

  onboarding_status       onboarding_status NOT NULL DEFAULT 'NOT_STARTED',
  onboarding_step         INT               NULL,

  primary_admin_user_id   UUID             NULL,

  created_at              TIMESTAMPTZ      NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ      NOT NULL DEFAULT now(),

  CONSTRAINT fk_companies_primary_admin
    FOREIGN KEY (primary_admin_user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS ix_companies_onboarding_status ON companies (onboarding_status);

-- =========================
-- User <-> Company link (Tenant membership)
-- Enforce 1 user = 1 company using UNIQUE(user_id)
-- =========================
CREATE TABLE IF NOT EXISTS user_companies (
  id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID        NOT NULL,
  company_id   UUID        NOT NULL,

  status       TEXT        NOT NULL DEFAULT 'ACTIVE',
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_user_companies_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_companies_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_user_companies_user_id ON user_companies (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_user_companies_company_user ON user_companies (company_id, user_id);
CREATE INDEX IF NOT EXISTS ix_user_companies_company_id ON user_companies (company_id);

-- =========================
-- RBAC (IAM)
-- =========================
CREATE TABLE IF NOT EXISTS roles (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  code        TEXT        NOT NULL, -- e.g. ADMIN, RH, FINANCEIRO
  name        TEXT        NOT NULL,
  is_system   BOOLEAN     NOT NULL DEFAULT TRUE,

  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_roles_code ON roles (lower(code));

CREATE TABLE IF NOT EXISTS permissions (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  code        TEXT        NOT NULL, -- e.g. ESTOQUE_APROVAR
  description TEXT        NULL,

  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_permissions_code ON permissions (lower(code));

CREATE TABLE IF NOT EXISTS role_permissions (
  role_id       UUID NOT NULL,
  permission_id UUID NOT NULL,

  PRIMARY KEY (role_id, permission_id),
  CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
  CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_company_roles (
  id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL,
  company_id  UUID        NOT NULL,
  role_id     UUID        NOT NULL,

  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_ucr_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_ucr_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
  CONSTRAINT fk_ucr_role
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_ucr_unique ON user_company_roles (user_id, company_id, role_id);
CREATE INDEX IF NOT EXISTS ix_ucr_company ON user_company_roles (company_id);
CREATE INDEX IF NOT EXISTS ix_ucr_user ON user_company_roles (user_id);

-- =========================
-- Subscriptions (SaaS billing state)
-- =========================
CREATE TABLE IF NOT EXISTS subscriptions (
  id                     UUID               PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id              UUID               NOT NULL,

  plan_code               TEXT               NOT NULL DEFAULT 'STANDARD',
  status                  subscription_status NOT NULL DEFAULT 'TRIAL',

  current_period_start    TIMESTAMPTZ        NULL,
  current_period_end      TIMESTAMPTZ        NULL,

  auto_renew              BOOLEAN            NOT NULL DEFAULT TRUE,
  cancel_at_period_end    BOOLEAN            NOT NULL DEFAULT FALSE,

  provider                TEXT               NULL, -- e.g. STRIPE, MERCADOPAGO
  provider_customer_id    TEXT               NULL,
  provider_subscription_id TEXT              NULL,

  created_at              TIMESTAMPTZ        NOT NULL DEFAULT now(),
  updated_at              TIMESTAMPTZ        NOT NULL DEFAULT now(),

  CONSTRAINT fk_subscriptions_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_subscriptions_company ON subscriptions (company_id);
CREATE INDEX IF NOT EXISTS ix_subscriptions_status ON subscriptions (status);
CREATE INDEX IF NOT EXISTS ix_subscriptions_period_end ON subscriptions (current_period_end);

-- =========================
-- Tokens
-- =========================

-- Refresh tokens: long-lived, rotated, stored hashed
CREATE TABLE IF NOT EXISTS refresh_tokens (
  id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id               UUID        NOT NULL,

  token_hash            TEXT        NOT NULL, -- store hash only
  expires_at            TIMESTAMPTZ NOT NULL,

  revoked_at            TIMESTAMPTZ NULL,
  replaced_by_token_id  UUID        NULL,

  created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
  ip                    TEXT        NULL,
  user_agent            TEXT        NULL,

  CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_refresh_tokens_replaced_by
    FOREIGN KEY (replaced_by_token_id) REFERENCES refresh_tokens(id)
);

CREATE INDEX IF NOT EXISTS ix_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS ix_refresh_tokens_expires ON refresh_tokens (expires_at);
CREATE UNIQUE INDEX IF NOT EXISTS ux_refresh_tokens_hash ON refresh_tokens (token_hash);

-- Password reset tokens: one-time, stored hashed
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id           UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id      UUID           NOT NULL,

  token_hash   TEXT           NOT NULL,
  expires_at   TIMESTAMPTZ    NOT NULL,
  used_at      TIMESTAMPTZ    NULL,

  created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
  created_by   token_created_by NOT NULL DEFAULT 'SELF_SERVICE',
  ip           TEXT           NULL,
  user_agent   TEXT           NULL,

  CONSTRAINT fk_password_reset_tokens_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_password_reset_tokens_user ON password_reset_tokens (user_id);
CREATE INDEX IF NOT EXISTS ix_password_reset_tokens_expires ON password_reset_tokens (expires_at);
CREATE UNIQUE INDEX IF NOT EXISTS ux_password_reset_tokens_hash ON password_reset_tokens (token_hash);

-- Onboarding tokens: link sent after payment, stored hashed (separate from password reset)
CREATE TABLE IF NOT EXISTS onboarding_tokens (
  id            UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id       UUID           NOT NULL,
  company_id    UUID           NOT NULL,

  token_hash    TEXT           NOT NULL,
  expires_at    TIMESTAMPTZ    NOT NULL,
  used_at       TIMESTAMPTZ    NULL,

  created_at    TIMESTAMPTZ    NOT NULL DEFAULT now(),
  created_by    token_created_by NOT NULL DEFAULT 'SYSTEM',

  CONSTRAINT fk_onboarding_tokens_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_onboarding_tokens_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS ix_onboarding_tokens_company ON onboarding_tokens (company_id);
CREATE INDEX IF NOT EXISTS ix_onboarding_tokens_user ON onboarding_tokens (user_id);
CREATE INDEX IF NOT EXISTS ix_onboarding_tokens_expires ON onboarding_tokens (expires_at);
CREATE UNIQUE INDEX IF NOT EXISTS ux_onboarding_tokens_hash ON onboarding_tokens (token_hash);

-- =========================
-- Email outbox (optional, but very useful for SaaS)
-- =========================
CREATE TABLE IF NOT EXISTS email_outbox (
  id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id      UUID        NULL,
  to_email        TEXT        NOT NULL,

  template        TEXT        NOT NULL, -- e.g. ONBOARDING_LINK, PASSWORD_RESET
  payload         JSONB       NOT NULL DEFAULT '{}'::jsonb,

  status          email_status NOT NULL DEFAULT 'QUEUED',
  attempts        INT         NOT NULL DEFAULT 0,
  last_error      TEXT        NULL,

  created_by_user_id UUID     NULL, -- support/system
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  sent_at         TIMESTAMPTZ NULL,

  CONSTRAINT fk_email_outbox_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL,
  CONSTRAINT fk_email_outbox_created_by
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_email_outbox_status ON email_outbox (status);
CREATE INDEX IF NOT EXISTS ix_email_outbox_company ON email_outbox (company_id);

-- =========================
-- Audit log (shared)
-- =========================
CREATE TABLE IF NOT EXISTS audit_log (
  id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  company_id      UUID        NULL,
  actor_user_id   UUID        NULL,

  action          TEXT        NOT NULL, -- e.g. LOGIN_SUCCESS, SUPPORT_RESET_ADMIN
  entity_type     TEXT        NULL,
  entity_id       UUID        NULL,
  metadata        JSONB       NOT NULL DEFAULT '{}'::jsonb,

  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),

  CONSTRAINT fk_audit_log_company
    FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL,
  CONSTRAINT fk_audit_log_actor
    FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS ix_audit_log_company ON audit_log (company_id);
CREATE INDEX IF NOT EXISTS ix_audit_log_actor ON audit_log (actor_user_id);
CREATE INDEX IF NOT EXISTS ix_audit_log_action ON audit_log (action);
CREATE INDEX IF NOT EXISTS ix_audit_log_created_at ON audit_log (created_at);

-- =========================
-- Minimal seed roles (system roles)
-- =========================
INSERT INTO roles (code, name, is_system)
VALUES
  ('ADMIN', 'Administrador', TRUE),
  ('DIRETORIA', 'Diretoria', TRUE),
  ('FINANCEIRO', 'Financeiro', TRUE),
  ('RH', 'Recursos Humanos', TRUE),
  ('ESTOQUE', 'Estoque', TRUE),
  ('COLABORADOR', 'Colaborador', TRUE)
ON CONFLICT (lower(code)) DO NOTHING;
