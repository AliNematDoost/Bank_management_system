-- This script runs automatically when Oracle container starts
-- Create a new user for our application
ALTER SESSION SET "_ORACLE_SCRIPT"=true;

-- Create user
CREATE USER mohaimen_user IDENTIFIED BY Mohaimen123;

-- Grant necessary privileges
GRANT CONNECT, RESOURCE TO mohaimen_user;
GRANT CREATE SESSION TO mohaimen_user;
GRANT CREATE TABLE TO mohaimen_user;
GRANT CREATE SEQUENCE TO mohaimen_user;
GRANT CREATE TRIGGER TO mohaimen_user;
GRANT UNLIMITED TABLESPACE TO mohaimen_user;

-- Connect as the new user (this won't work in startup script, but shows the process)
-- The application will connect as mohaimen_user

-- Create sequence for transaction IDs
-- This will be created automatically by Hibernate, but we can create it manually if needed
-- Note: Hibernate will create this when ddl-auto=update is used

COMMIT;

-- Display success message
SELECT 'Database setup completed successfully!' as STATUS from dual;