-- Quick Fix: Clear All Connections to Demo Database
-- Run this in pgAdmin or psql before restarting your application

-- Option 1: Terminate all connections to 'demo' database
SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'demo'
  AND pid <> pg_backend_pid();

-- Option 2: Check how many connections were terminated
SELECT COUNT(*) as terminated_connections
FROM pg_stat_activity
WHERE datname = 'demo';

-- Option 3: Drop and recreate database (if above doesn't work)
-- First connect to 'postgres' database: \c postgres
-- Then run:
-- SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'demo';
-- DROP DATABASE IF EXISTS demo;
-- CREATE DATABASE demo;

