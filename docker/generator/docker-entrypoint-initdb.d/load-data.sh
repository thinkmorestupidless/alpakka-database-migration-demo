#!/bin/sh

set -e

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

CREATE TABLE IF NOT EXISTS unbilled_data (
  customer_id VARCHAR(255) NOT NULL,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  amount DECIMAL(6,2) NOT NULL,
  PRIMARY KEY(customer_id)
);
DDL

psql -U postgres <<DDL
\c alpakka_demo
COPY unbilled_data(customer_id,first_name,last_name,email,amount) FROM '/docker-entrypoint-initdb.d/generated-records.csv' DELIMITER ',' CSV HEADER;
DDL
