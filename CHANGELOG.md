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
