# Requisitos do Projeto — Primora ERP

## 1. Visão geral

O **Primora** é um **ERP SaaS para empresas em crescimento**, com foco em ser
o **primeiro ERP** adotado por uma empresa.

O sistema é entregue como:
- SaaS de uso imediato (pagou → usou)
- opção de plano personalizado (deploy dedicado, onboarding assistido)

Este documento define os **requisitos funcionais e não funcionais** do sistema,
separados entre **mínimos (MVP)** e **ideais (evolução)**.

---

## 2. Requisitos Funcionais

### 2.1 Requisitos Funcionais Mínimos (MVP)

#### Autenticação e Identidade
- RF-01: O sistema deve permitir login por e-mail e senha.
- RF-02: O sistema deve utilizar JWT para autenticação stateless.
- RF-03: O sistema deve suportar refresh token com rotação.
- RF-04: O sistema deve permitir redefinição de senha via e-mail.
- RF-05: O sistema deve diferenciar usuários por tipo:
    - TENANT_USER
    - SAAS_OWNER
    - SAAS_SUPPORT

---

#### Multi-tenant
- RF-06: O sistema deve suportar múltiplas empresas (tenants) em um único banco.
- RF-07: Cada usuário deve pertencer a exatamente uma empresa.
- RF-08: Todas as operações de negócio devem ser isoladas por `company_id`.
- RF-09: O `company_id` deve ser obtido exclusivamente do token JWT.

---

#### Onboarding
- RF-10: O sistema deve permitir onboarding após confirmação de pagamento.
- RF-11: O onboarding deve ser associado à empresa, não ao usuário.
- RF-12: O onboarding deve possuir estados:
    - NOT_STARTED
    - IN_PROGRESS
    - COMPLETED
- RF-13: Enquanto o onboarding não estiver completo, o acesso ao ERP deve ser restrito.

---

#### SaaS (Dono do produto)
- RF-14: O dono do SaaS deve poder visualizar empresas ativas.
- RF-15: O dono do SaaS deve visualizar status da assinatura.
- RF-16: O dono do SaaS deve poder reenviar e-mails de onboarding.
- RF-17: O suporte deve poder gerar links de redefinição de senha para administradores.
- RF-18: Todas as ações de suporte devem ser auditadas.

---

#### RBAC (IAM)
- RF-19: O sistema deve permitir criação de roles por empresa.
- RF-20: O sistema deve permitir atribuição de roles a usuários.
- RF-21: As permissões devem ser avaliadas por empresa (tenant).

---

#### Cadastros Base (Core)
- RF-22: O sistema deve permitir cadastro de departamentos.
- RF-23: O sistema deve permitir cadastro de centros de custo.
- RF-24: O sistema deve permitir configurar dados básicos da empresa.

---

#### Requisições (Comunicação entre setores)
- RF-25: O sistema deve permitir criação de requisições internas entre setores.
- RF-26: Requisições devem possuir fluxo simples de aprovação.
- RF-27: Requisições devem possuir histórico e rastreabilidade.
- RF-28: Requisições podem gerar impacto financeiro ou de estoque.

---

#### Contratos
- RF-29: O sistema deve permitir cadastro de contratos de terceiros.
- RF-30: O sistema deve permitir armazenamento e visualização de contratos.
- RF-31: O sistema deve permitir alertas de vencimento de contratos.

---

#### Estoque
- RF-32: O sistema deve permitir controle de entrada e saída de estoque.
- RF-33: O sistema deve permitir requisição de itens por setor.
- RF-34: Movimentações de estoque devem ser rastreáveis.

---

#### Financeiro (gerencial)
- RF-35: O sistema deve registrar lançamentos financeiros básicos.
- RF-36: O sistema deve permitir visualização de gastos por setor.
- RF-37: O sistema deve integrar gastos com estoque e requisições.

---

#### RH
- RF-38: O sistema deve permitir cadastro de colaboradores.
- RF-39: O sistema deve permitir cadastro de contratos de colaboradores.
- RF-40: O sistema deve permitir registro de benefícios básicos.

---

### 2.2 Requisitos Funcionais Ideais (Evolução)

- RF-I01: Integração com gateways de pagamento (Stripe, Mercado Pago).
- RF-I02: Renovação automática de assinaturas.
- RF-I03: Notificações automáticas por e-mail (vencimentos, alertas).
- RF-I04: Dashboards gerenciais por módulo.
- RF-I05: Exportação de relatórios (PDF/CSV).
- RF-I06: Aprovações em múltiplos níveis.
- RF-I07: Histórico completo de alterações (versionamento lógico).
- RF-I08: Integração futura com sistema de chamados via SSO.
- RF-I09: Permitir múltiplos administradores por empresa.
- RF-I10: Customização básica de frontend por cliente (plano premium).

---

## 3. Requisitos Não Funcionais

### 3.1 Requisitos Não Funcionais Mínimos (MVP)

#### Arquitetura e Código
- RNF-01: O sistema deve ser um monólito modular.
- RNF-02: O código deve ser escrito em Java 21.
- RNF-03: O backend deve utilizar Spring Boot.
- RNF-04: O domínio não deve depender de frameworks.
- RNF-05: O código deve priorizar legibilidade e simplicidade.

---

#### Segurança
- RNF-06: Senhas devem ser armazenadas com hash seguro.
- RNF-07: Tokens sensíveis devem ser armazenados de forma hash.
- RNF-08: O sistema deve proteger rotas por autenticação e autorização.
- RNF-09: O sistema deve impedir acesso entre tenants.

---

#### Banco de Dados
- RNF-10: O banco de dados deve ser PostgreSQL.
- RNF-11: O schema deve ser versionado via Flyway.
- RNF-12: Não deve haver alterações manuais em produção.

---

#### Auditoria e Observabilidade
- RNF-13: Ações sensíveis devem ser auditadas.
- RNF-14: Logs devem permitir rastrear erros por empresa.
- RNF-15: Falhas críticas devem ser registradas.

---

#### Operação
- RNF-16: O sistema deve permitir deploy único e simples.
- RNF-17: O sistema deve suportar execução em ambiente on-premises.
- RNF-18: O sistema deve ser operável por um único desenvolvedor.

---

### 3.2 Requisitos Não Funcionais Ideais (Evolução)

- RNF-I01: Escalabilidade horizontal do backend.
- RNF-I02: Observabilidade avançada (metrics, tracing).
- RNF-I03: Alta disponibilidade para planos premium.
- RNF-I04: Feature flags para ativação por plano.
- RNF-I05: Isolamento lógico avançado por tenant.
- RNF-I06: Monitoramento proativo de falhas.
- RNF-I07: Backups automáticos por tenant.
- RNF-I08: SLA diferenciado por plano.
- RNF-I09: Documentação pública de API.
- RNF-I10: Auditoria exportável para compliance.

---

## 4. Observações finais

- Os requisitos mínimos definem o **produto vendável inicial**.
- Os requisitos ideais representam **evolução natural**, não obrigatória.
- Qualquer funcionalidade fora deste escopo deve ser avaliada com cuidado
  para evitar aumento desnecessário de complexidade.

Primora prioriza **clareza, controle e sustentabilidade do desenvolvimento**.
