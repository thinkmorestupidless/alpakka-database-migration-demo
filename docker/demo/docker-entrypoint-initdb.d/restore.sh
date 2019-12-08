#!/bin/sh

psql -U postgres <<DDL
CREATE DATABASE alpakka_demo;
REVOKE CONNECT ON DATABASE alpakka_demo FROM PUBLIC;
CREATE USER alpakka_demo WITH PASSWORD 'alpakka_demo';
GRANT CONNECT ON DATABASE alpakka_demo TO alpakka_demo;
DDL

psql -U alpakka_demo <<DDL
\c alpakka_demo
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE ON SCHEMA public TO alpakka_demo;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO alpakka_demo;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, USAGE ON SEQUENCES TO alpakka_demo;
DDL

FILE="/docker-entrypoint-initdb.d/alpakka_demo.pgdata"
DATABASE=alpakka_demo

echo "Restoring $DATABASE using $FILE"

pg_restore -U postgres --dbname="$DATABASE" --verbose --single-transaction < "$FILE" || exit 1
