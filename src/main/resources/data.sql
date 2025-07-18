-- Initial data for CLoginUser table
-- This file is executed on application startup to create initial login users
-- Spring Boot will run this file when spring.sql.init.mode=always in application.properties
-- Note: Using single table inheritance, so CLoginUser data goes into 'cuser' table with discriminator

-- Note: Passwords are BCrypt encoded:
-- 'admin' -> $2a$10$sRqJoY56s0o8OL0Vcs3R8O3kHvUvNBwSxZvm4HHTccMDkneTyDT3O
-- 'user' -> $2a$10$RFCCQbcXxM0Gbj7BbI8jVOimzeD96XS0bUh0KuaQh9xv6hQzsHxpS
-- 'test123' -> $2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu

-- Insert initial login users only if they don't already exist
-- Using single table inheritance with discriminator column
-- Admin user with full access
INSERT INTO cuser (user_type, name, lastname, login, email, phone, password, roles, enabled) 
SELECT 'LOGIN_USER', 'System', 'Administrator', 'admin', 'admin@derbent.tech', '+90-555-000-0001', 
       '$2a$10$sRqJoY56s0o8OL0Vcs3R8O3kHvUvNBwSxZvm4HHTccMDkneTyDT3O', 
       'ADMIN,USER', true
WHERE NOT EXISTS (SELECT 1 FROM cuser WHERE login = 'admin' AND user_type = 'LOGIN_USER');

-- Regular user for testing
INSERT INTO cuser (user_type, name, lastname, login, email, phone, password, roles, enabled) 
SELECT 'LOGIN_USER', 'Test', 'User', 'user', 'user@derbent.tech', '+90-555-000-0002', 
       '$2a$10$RFCCQbcXxM0Gbj7BbI8jVOimzeD96XS0bUh0KuaQh9xv6hQzsHxpS', 
       'USER', true
WHERE NOT EXISTS (SELECT 1 FROM cuser WHERE login = 'user' AND user_type = 'LOGIN_USER');

-- Demo user with test123 password
INSERT INTO cuser (user_type, name, lastname, login, email, phone, password, roles, enabled) 
SELECT 'LOGIN_USER', 'Demo', 'User', 'demo', 'demo@derbent.tech', '+90-555-000-0003', 
       '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu', 
       'USER', true
WHERE NOT EXISTS (SELECT 1 FROM cuser WHERE login = 'demo' AND user_type = 'LOGIN_USER');