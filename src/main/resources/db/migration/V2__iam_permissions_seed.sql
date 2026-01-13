-- Seed IAM permissions and link to ADMIN role
INSERT INTO permissions (id, code, description)
VALUES
  (gen_random_uuid(), 'IAM_MANAGE_ROLES', 'Manage roles and permissions'),
  (gen_random_uuid(), 'IAM_ASSIGN_ROLES', 'Assign roles to users')
ON CONFLICT (lower(code)) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON lower(p.code) IN ('iam_manage_roles', 'iam_assign_roles')
WHERE lower(r.code) = 'admin'
ON CONFLICT DO NOTHING;
