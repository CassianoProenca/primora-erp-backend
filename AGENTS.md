# AGENTS.md — Primora ERP

## 1. Visão do projeto

**Primora** é um **ERP SaaS para empresas em crescimento**, pensado para ser:
- o **primeiro ERP** de uma empresa
- simples de usar
- completo, porém **nichado**
- sustentável para **um desenvolvedor solo**

O sistema é construído como um **monólito modular** em **Java 21 + Spring Boot**.

O foco do projeto é:
- clareza de código
- regras de negócio explícitas
- evolução incremental
- evitar complexidade desnecessária

Este projeto **não é**:
- um experimento acadêmico
- uma arquitetura de microserviços
- um ERP genérico para qualquer tipo de empresa

---

## 2. Arquitetura geral

### Estilo arquitetural
- Monólito modular
- Um único deploy
- Um único banco de dados (PostgreSQL)
- Separação lógica por **módulos de domínio**

### Decisões conscientes
- Não usamos microserviços
- Não usamos Clean Architecture rígida
- Não usamos event-driven distribuído
- Preferimos código simples, direto e legível

---

## 3. Estrutura base de pacotes

com.primora.erp
│
├─ shared
├─ auth
├─ iam
├─ saas
├─ onboarding
├─ core
│
├─ contratos
├─ estoque
├─ financeiro
├─ rh
└─ requisicoes

Cada pasta acima representa **um módulo do sistema**.

---

## 4. Estrutura interna obrigatória de cada módulo

Todo módulo **deve** seguir esta estrutura:

modulo
├─ api
├─ app
├─ domain
└─ infra

### Significado de cada camada

#### `api`
Responsável por:
- Controllers (`@RestController`)
- DTOs de entrada e saída
- Validações de request
- Mapeamento HTTP → caso de uso

Regras:
- Nunca acessa repositórios
- Nunca contém regra de negócio
- Nunca expõe entidades JPA

---

#### `app`
Responsável por:
- Casos de uso
- Orquestração de regras
- Controle transacional
- Comunicação entre módulos

Exemplos:
- `LoginService`
- `CreateCompanyService`
- `FinishOnboardingService`
- `ApproveRequisitionService`

Regras:
- Serviços representam **ações do sistema**
- Não conter lógica de infraestrutura
- Pode chamar repositórios e outros serviços de aplicação

---

#### `domain`
Responsável por:
- Entidades de negócio
- Regras de domínio
- Enums e value objects

Exemplos:
- `User`
- `Company`
- `Subscription`
- `Requisition`

Regras:
- Não depende de Spring
- Não depende de controllers
- Deve ser o código mais estável do sistema

---

#### `infra`
Responsável por:
- JPA repositories
- Implementações técnicas
- Integrações externas
- Configurações específicas do módulo

Exemplos:
- `JpaUserRepository`
- `JwtService`
- `EmailSender`
- Gateways de pagamento (quando existirem)

---

## 5. Módulo `shared`

shared
├─ security
├─ audit
├─ errors
├─ util
├─ time
├─ files
└─ outbox

### O que entra em `shared`
- Código reutilizável
- Infraestrutura transversal
- Código sem regra de negócio específica

Exemplos:
- `CurrentUser`
- `TenantContext`
- Auditoria
- Exceptions globais
- Upload/download de arquivos
- Fila simples de e-mails (outbox)

Regras:
- `shared` não deve conhecer regras de outros módulos de negócio
- Usar com moderação

---

## 6. Módulos principais

### `auth`
Responsável por autenticação e identidade.

Contém:
- login
- refresh token
- reset de senha
- geração e validação de JWT

Não contém:
- regras de autorização do ERP
- RBAC do cliente

---

### `iam` (Identity and Access Management)
Responsável pelo **RBAC da empresa cliente**.

Contém:
- usuários da empresa
- roles e permissões
- vínculo usuário ↔ empresa

Não contém:
- login
- JWT
- lógica do dono do SaaS

---

### `saas`
Responsável pelo **painel do dono do SaaS**.

Contém:
- provisionamento de tenants
- assinatura e status de plano
- ações de suporte
- métricas básicas de uso

Somente usuários com:
- `SAAS_OWNER`
- `SAAS_SUPPORT`

podem acessar este módulo.

---

### `onboarding`
Responsável pelo fluxo pós-pagamento.

Contém:
- criação da empresa
- configuração inicial
- finalização do onboarding

Regras:
- onboarding pertence à **empresa**, não ao usuário
- enquanto não finalizado, o acesso ao ERP é bloqueado

---

### `core`
Responsável por cadastros base reutilizáveis.

Exemplos:
- departamentos
- centros de custo
- configurações da empresa

Evita duplicação entre módulos.

---

## 7. Módulos de negócio do ERP

### `contratos`
- contratos de terceiros
- documentos
- alertas de vencimento

### `estoque`
- movimentações de estoque
- controle de materiais
- impacto financeiro por setor

### `financeiro`
- lançamentos gerenciais
- controle de gastos
- integração com estoque e RH

### `rh`
- colaboradores
- contratos
- benefícios
- folha de pagamento

---

## 8. Requisições (comunicação entre setores)

O ERP Primora possui um módulo de **Requisições**, responsável por registrar
pedidos e comunicações formais entre setores da empresa.

Este módulo faz parte da gestão administrativa e **não representa um sistema de
helpdesk ou chamados**.

Características do módulo:
- registro de pedidos internos entre áreas
- fluxo simples de aprovação
- rastreabilidade e histórico
- vínculo com estoque, financeiro e contratos
- impacto administrativo e financeiro

O módulo de Requisições **não possui**:
- SLA
- filas de atendimento
- técnicos ou agentes
- lógica de suporte

O sistema de helpdesk (chamados) é um produto separado e fora do escopo do ERP.

---

## 9. Multi-tenant

- Banco compartilhado
- Todas as tabelas de domínio possuem `company_id`
- O `company_id` vem sempre do JWT
- Nunca confiar em `company_id` vindo do request

---

## 10. Identidade e tipos de usuário

Existe **uma única tabela de usuários** com diferenciação por tipo:

- `TENANT_USER`
- `SAAS_OWNER`
- `SAAS_SUPPORT`

Regras:
- `SAAS_OWNER` não pertence a empresas
- `TENANT_USER` não acessa `/saas/**`
- RBAC do cliente não se aplica ao dono do SaaS

---

## 11. Padrões de código

- Java 21
- DTOs obrigatórios na camada `api`
- MapStruct para conversão DTO ↔ entidade
- Services com nomes de ação
- Código explícito > abstração excessiva
- Código escrito em inglês (nomes de classes, métodos, variáveis e comentários)
- Endpoints de listagem devem usar paginação por padrão

---

## 12. Banco de dados

- PostgreSQL
- Migrations com Flyway
- Nenhuma alteração manual em produção
- Toda mudança passa por migration versionada

---

## 13. Auditoria

Ações sensíveis devem ser auditadas:
- login e logout
- reset de senha
- onboarding
- ações de suporte
- operações financeiras relevantes

---

## 14. Princípios para agentes (anti-alucinação)

Ao sugerir código ou mudanças, o agente deve:
1. Manter coerência com monólito modular
2. Evitar introduzir novos frameworks sem pedido explícito
3. Preferir soluções simples e explícitas
4. Não sugerir microserviços
5. Perguntar antes de assumir mudanças estruturais

---

## 15. Filosofia do Primora

Primora é feito para:
- ser compreensível
- ser confiável
- crescer sem reescritas traumáticas
- não gerar burnout no desenvolvedor


## 16. Filosofia do AGENT

O AGENT deve:
- Entender o contexto do Primora
- Seguir as diretrizes deste documento
- Priorizar clareza e simplicidade
- Evitar complexidade desnecessária
- Fazer perguntas quando necessário
- Ajudar a evoluir o Primora de forma sustentável
- Pode Criar ou Modificar arquivos .java conforme necessário
- Não excluir sem permissão explícita
- Não criar novos módulos sem permissão explícita
- Não alterar a arquitetura base sem permissão explícita
- 
