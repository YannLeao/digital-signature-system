# Database migrations

Flyway migrations must use the format `V{number}__{description}.sql`.

Examples:

- `V1__create_users_table.sql`
- `V2__create_audit_log_table.sql`

Schema changes must be made only through migrations. Hibernate is configured with
`spring.jpa.hibernate.ddl-auto=validate`, so it validates the schema but does not
create or alter tables automatically.
