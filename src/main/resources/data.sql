INSERT INTO users (username, email, password, profile_image, role, banned, deleted, registration_date, last_login)
VALUES ('user', 'user@example.com', '{noop}12345678', '/img/default_profile.png', 'USER', false, false, CURRENT_TIMESTAMP, NULL);
INSERT INTO users (username, email, password, profile_image, role, banned, deleted, registration_date, last_login)
VALUES ('admin', 'admin@example.com', '{noop}12345678', '/img/default_profile.png', 'ADMIN', false, false, CURRENT_TIMESTAMP, NULL);

-- Dodanie 10 serwerów przypisanych do admina z opisem i statusem APPROVED
INSERT INTO servers (server_name, ip_address, version, mode, description, up_votes, down_votes, score, status, created_at, updated_at, created_by)
VALUES
('Survival Server 1', '192.168.1.1', 'V1_16_5', 'SURVIVAL', 'Opis serwera 1', 10, 2, 8, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Survival Server 2', '192.168.1.2', 'V1_17_1', 'SURVIVAL', 'Opis serwera 2', 15, 3, 12, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Creative Server 1', '192.168.1.3', 'V1_16_4', 'CREATIVE', 'Opis serwera 3', 7, 1, 6, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Skyblock Server 1', '192.168.1.4', 'V1_16_3', 'SKYBLOCK', 'Opis serwera 4', 12, 4, 8, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Factions Server 1', '192.168.1.5', 'V1_17_0', 'FACTIONS', 'Opis serwera 5', 20, 5, 15, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Prison Server 1', '192.168.1.6', 'V1_15_2', 'PRISON', 'Opis serwera 6', 5, 1, 4, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Minigames Server 1', '192.168.1.7', 'V1_16_2', 'MINIGAMES', 'Opis serwera 7', 8, 2, 6, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Hardcore Server 1', '192.168.1.8', 'V1_17_1', 'HARDCORE', 'Opis serwera 8', 30, 6, 24, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Adventure Server 1', '192.168.1.9', 'V1_18_1', 'ADVENTURE', 'Opis serwera 9', 25, 3, 22, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Anarchy SMP Server 1', '192.168.1.10', 'V1_20_2', 'ANARCHY_SMP', 'Opis serwera 10', 40, 10, 30, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Survival Server Pending', '192.168.1.11', 'V1_16_5', 'SURVIVAL', 'Opis serwera Pending', 0, 0, 0, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'user@example.com')),
('Survival Server Rejected', '192.168.1.12', 'V1_16_5', 'SURVIVAL', 'Opis serwera Rejected', 0, 0, 0, 'REJECTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'user@example.com')),
('Creative Server Approved', '192.168.1.13', 'V1_16_4', 'CREATIVE', 'Opis serwera Approved 1', 10, 1, 9, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Skyblock Server Approved', '192.168.1.14', 'V1_16_3', 'SKYBLOCK', 'Opis serwera Approved 2', 20, 3, 17, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com'));
