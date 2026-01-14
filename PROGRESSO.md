# Progresso — Primora ERP

Este arquivo organiza o andamento do desenvolvimento em sessoes diarias de codigo.
Cada sessao representa um bloco de trabalho coeso e finalizavel no mesmo dia.

## Sessao 01 — Base do projeto e infra local (feito)
- Confirmar estrutura modular por dominio (api/app/domain/infra)
- Ajustar application.yaml para variaveis de ambiente
- Dockerfile e docker-compose com Postgres + API
- Padroes de log e observabilidade minima

## Sessao 02 — Identidade basica (auth) (feito)
- Entidades base de usuario e tipos (TENANT_USER, SAAS_OWNER, SAAS_SUPPORT)
- Login e refresh token
- Hash de senha e validacoes
- Auditoria de login/logout

## Sessao 03 — Multi-tenant e contexto (feito)
- Resolver company_id a partir do JWT
- TenantContext em shared
- Filtros para garantir company_id nas consultas
- Validacao de acesso por tipo de usuario

## Sessao 04 — IAM (RBAC do cliente) (feito)
- Entidades de roles e permissoes
- Vinculo usuario-empresa
- API para roles e permissoes
- Checagem de permissao por acao

## Sessao 05 — Onboarding (feito)
- Fluxo de criacao de empresa
- Bloqueio de acesso ate finalizacao
- Auditoria das etapas criticas

## Sessao 05.1 — SaaS (dono do produto) (feito)
- Visualizacao de empresas ativas e assinaturas
- Reenvio de onboarding
- Reset de senha admin por suporte
- Auditoria das acoes de suporte

## Sessao 06 — Core (cadastros base) (feito)
- Departamentos
- Centros de custo
- Configuracoes da empresa

## Sessao 07 — Requisicoes (MVP) (feito)
- Entidade de requisicao e estados
- API de criacao, leitura e resolucao
- Historico e auditoria

## Sessao 07.1 — Comunicados (feito)
- Comunicacao entre departamentos/pessoas
- Status de envio, recebimento e leitura

## Sessao 08 — Estoque (MVP) (feito)
- Materiais e movimentacoes
- Impacto financeiro basico
- Integracao com requisicoes

## Sessao 09 — Financeiro (MVP) (feito)
- Lancamentos gerenciais
- Categorias e controle de gastos
- Integracao com estoque (RH pendente)

## Sessao 10 — RH (MVP) (feito)
- Colaboradores
- Contratos e beneficios

## Sessao 11 — Contratos (MVP)
- Cadastro de contratos
- Alertas de vencimento
- Documentos anexos

## Sessao 12 — Hardening e qualidade
- Testes essenciais por modulo
- Ajustes de performance basicos
- Revisao de logs, auditoria e seguranca
