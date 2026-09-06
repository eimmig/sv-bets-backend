# Changelog

Cada linha de `[Unreleased]` é um link para a issue do Jira que a gerou (story ou subtask),
formato `- [chave](url) - título` — sem prosa, sem categoria. Escrita automaticamente por
`tools/jira_story.py` no momento em que a issue é criada (ver `docs/CI-CD.md` seção "Changelog
por serviço"). O "porquê" de cada mudança vive na issue e na mensagem de commit, não aqui.

## [Unreleased]

- [SV-9](https://stakevault.atlassian.net/browse/SV-9) - Alinhar as chaves de projeto ao padrão do SonarCloud
- [SV-60](https://stakevault.atlassian.net/browse/SV-60) - Setup do projeto
- [SV-61](https://stakevault.atlassian.net/browse/SV-61) - Endurecer pipeline de CI antes do bootstrap (porta o padrao ja validado de auth-service)
- [SV-62](https://stakevault.atlassian.net/browse/SV-62) - Bootstrap do pom.xml e esqueleto hexagonal
- [SV-63](https://stakevault.atlassian.net/browse/SV-63) - Conexao Postgres com profiles dev/test/prod
- [SV-64](https://stakevault.atlassian.net/browse/SV-64) - Provisionamento de schema de tenant (Flyway lazy) + filtro X-Tenant-Id + rota admin X-Admin-Api-Key
- [SV-65](https://stakevault.atlassian.net/browse/SV-65) - Gate de cobertura JaCoCo 80%
- [SV-66](https://stakevault.atlassian.net/browse/SV-66) - Scaffold de i18n (MessageSource) e teste smoke
- [SV-67](https://stakevault.atlassian.net/browse/SV-67) - Health checks do Actuator
- [SV-68](https://stakevault.atlassian.net/browse/SV-68) - .env.example e logging estruturado
- [SV-69](https://stakevault.atlassian.net/browse/SV-69) - CHANGELOG e verificacao final
- [SV-70](https://stakevault.atlassian.net/browse/SV-70) - Catalogos base
- [SV-71](https://stakevault.atlassian.net/browse/SV-71) - Migration Flyway dos 4 catalogos
- [SV-72](https://stakevault.atlassian.net/browse/SV-72) - Multi-tenancy do Hibernate + enforcement de X-Tenant-Id
- [SV-73](https://stakevault.atlassian.net/browse/SV-73) - Entidades JPA e persistencia (domain + adapter)
- [SV-74](https://stakevault.atlassian.net/browse/SV-74) - Endpoints REST (POST + GET paginado) dos 4 catalogos
- [SV-75](https://stakevault.atlassian.net/browse/SV-75) - CHANGELOG e verificacao final
- [SV-76](https://stakevault.atlassian.net/browse/SV-76) - RF03/RF13 - Casas de apostas e movimentacoes
- [SV-77](https://stakevault.atlassian.net/browse/SV-77) - Migration Flyway de betting_house e transaction
- [SV-78](https://stakevault.atlassian.net/browse/SV-78) - Dominio, TransactionType, entidades JPA e persistencia
- [SV-79](https://stakevault.atlassian.net/browse/SV-79) - Endpoints REST (POST + GET paginado) de betting-houses e transactions
- [SV-80](https://stakevault.atlassian.net/browse/SV-80) - CHANGELOG e verificacao final
- [SV-81](https://stakevault.atlassian.net/browse/SV-81) - RF04/RF12 - Registro e ciclo de vida da aposta
- [SV-82](https://stakevault.atlassian.net/browse/SV-82) - Migration Flyway da tabela bet
- [SV-83](https://stakevault.atlassian.net/browse/SV-83) - Dominio, BetStatus, entidades JPA e persistencia (+ existsById nos catalogos)
- [SV-84](https://stakevault.atlassian.net/browse/SV-84) - Endpoint POST /api/v1/bets
- [SV-85](https://stakevault.atlassian.net/browse/SV-85) - Endpoints GET /api/v1/bets/{id} e PATCH /api/v1/bets/{id}/status (RF12)
- [SV-86](https://stakevault.atlassian.net/browse/SV-86) - CHANGELOG e verificacao final
- [SV-87](https://stakevault.atlassian.net/browse/SV-87) - RF06/RF07 - Processamento de resultado e bankroll consolidado
- [SV-88](https://stakevault.atlassian.net/browse/SV-88) - Migration Flyway da tabela bet_result
- [SV-89](https://stakevault.atlassian.net/browse/SV-89) - Dominio BetResult e persistencia
- [SV-90](https://stakevault.atlassian.net/browse/SV-90) - Liquidacao atomica: transicao condicional + BET_RESULT + X-User-Id no PATCH
- [SV-91](https://stakevault.atlassian.net/browse/SV-91) - Balance de betting-houses passa a incluir profit de apostas liquidadas (RN01/RN05)
- [SV-92](https://stakevault.atlassian.net/browse/SV-92) - CHANGELOG e verificacao final
