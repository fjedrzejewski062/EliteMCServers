INSERT INTO users (username, email, password, profile_image, role, banned, deleted, registration_date, last_login)
VALUES ('user', 'user@example.com', '{noop}12345678', '/img/default_profile.png', 'USER', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO users (username, email, password, profile_image, role, banned, deleted, registration_date, last_login)
VALUES ('admin', 'admin@example.com', '{noop}12345678', '/img/default_profile.png', 'ADMIN', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Dodanie 10 prawdziwych serwerów przypisanych do admina z opisem i statusem APPROVED
INSERT INTO servers (server_name, ip_address, version, mode, description, up_votes, down_votes, score, status, created_at, updated_at, created_by)
VALUES
('Hypixel', 'mc.hypixel.net', 'V1_20_2', 'MINIGAMES', 'The largest minigames server in the world.', 1000, 50, 950, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Mineplex', 'us.mineplex.com', 'V1_19_4', 'MINIGAMES', 'Popular server with tons of mini-games.', 800, 30, 770, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('The Hive', 'play.hivemc.com', 'V1_19_3', 'MINIGAMES', 'Server with various modes and mini-games..', 600, 20, 580, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('CubeCraft Games', 'play.cubecraft.net', 'V1_18_2', 'MINIGAMES', 'Minigames and PVP mode', 450, 15, 435, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('GommeHD', 'gommehd.net', 'V1_17_1', 'FACTIONS', 'German server with factions mode.', 300, 10, 290, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Wynncraft', 'play.wynncraft.com', 'V1_16_5', 'ADVENTURE', 'RPG MMO on Minecraft.', 700, 25, 675, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('2b2t', '2b2t.org', 'V1_15_2', 'ANARCHY_SMP', 'The oldest Anarchy Server', 900, 100, 800, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('HermitCraft', 'play.hermitcraft.com', 'V1_20_1', 'SURVIVAL', 'Server of popular Youtubers', 650, 18, 632, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Hypixel Skyblock', 'skyblock.hypixel.net', 'V1_20_2', 'SKYBLOCK', 'Skyblock mode on Hypixel', 850, 20, 830, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('Badlion', 'na.badlion.net', 'V1_19_3', 'PVP', 'Server focused on PvP and tournaments.', 500, 12, 488, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),

-- Fikcyjne serwery z sensownymi nazwami i statusami
('Mystic Survival', 'mysticsurvival.example.com', 'V1_20_2', 'SURVIVAL', 'Friendly survival server for gamers', 50, 5, 45, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('PendingCraft', 'pendingcraft.example.com', 'V1_20_2', 'CREATIVE', 'Server is being approved', 0, 0, 0, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'user@example.com')),
('Rejected Realms', 'rejectedrealms.example.com', 'V1_19_4', 'ADVENTURE', 'Server rejected due to problems', 0, 10, -10, 'REJECTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'user@example.com')),
('Creative Builders', 'creativebuilders.example.com', 'V1_20_2', 'CREATIVE', 'Creative server with huge community', 70, 8, 62, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com'));
