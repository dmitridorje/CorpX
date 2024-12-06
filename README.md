# CorpX

CorpX — учебный проект, представляющий собой реализацию платформы, созданной для поддержки стартапов и профессионального взаимодействия. Она помогает создателям найти единомышленников, сформировать команды и управлять проектами. Основная идея — упростить процесс создания и продвижения проектов, предоставляя удобные инструменты для организации работы и взаимодействия с сообществом.

## Основные возможности
- **Проекты**: Организация команд, описание миссий, обсуждение идей и привлечение аудитории.
- **Социальные активности**: Посты, ленты новостей, подписки на проекты и комментарии.
- **Профессиональные профили**: Хранение информации о навыках, достижениях и опыте пользователей.
- **Платные функции**: Привлечение инвестиций, продвижение профилей и рост популярности.
- **Современные технологии**: Реферальные системы, аналитика популярности и уведомления.

## Цели
1. **Ускорение развития стартапов**: Предоставление инструментов для поиска участников, управления ролями и коммуникации.
2. **Продвижение идей**: Привлечение внимания к проектам через профессиональные профили и социальные активности.
3. **Формирование сообществ**: Взаимодействие пользователей, обмен опытом и поддержка друг друга.

# Использованные технологии

* [Spring Boot](https://spring.io/projects/spring-boot) – как основной фрэймворк
* [PostgreSQL](https://www.postgresql.org/) – как основная реляционная база данных
* [Redis](https://redis.io/) – как кэш и очередь сообщений через pub/sub
* [testcontainers](https://testcontainers.com/) – для изолированного тестирования с базой данных
* [Liquibase](https://www.liquibase.org/) – для ведения миграций схемы БД
* [Gradle](https://gradle.org/) – как система сборки приложения
* [Lombok](https://projectlombok.org/) – для удобной работы с POJO классами
* [MapStruct](https://mapstruct.org/) – для удобного маппинга между POJO классами

# База данных

* База поднимается в отдельном сервисе infra
* Redis поднимается в единственном инстансе тоже в infra
* Liquibase сам накатывает нужные миграции на голый PostgreSql при старте приложения
* В тестах используется [testcontainers](https://testcontainers.com/), в котором тоже запускается отдельный инстанс
  postgres
* В коде продемонстрирована работа как с JdbcTemplate, так и с JPA (Hibernate)
