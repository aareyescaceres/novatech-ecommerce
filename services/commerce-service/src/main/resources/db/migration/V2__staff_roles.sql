-- Ampliacion compatible con la base de datos de la version anterior.
ALTER TABLE app_users DROP CONSTRAINT IF EXISTS app_users_role_check;
ALTER TABLE app_users ADD CONSTRAINT app_users_role_check
  CHECK (role IN ('CUSTOMER', 'ADMIN', 'SALES_STAFF', 'INVENTORY_MANAGER'));
