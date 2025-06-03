INSERT INTO users (username, email, password, profile_image, role, banned, deleted, registration_date, last_login)
VALUES ('user', 'user@example.com', '{noop}12345678', '/img/default_profile.png', 'USER', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO users (username, email, password, profile_image, role, banned, deleted, registration_date, last_login)
VALUES ('admin', 'admin@example.com', '{noop}12345678', '/img/default_profile.png', 'ADMIN', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

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

('Mystic Survival', 'mysticsurvival.example.com', 'V1_20_2', 'SURVIVAL', 'Friendly survival server for gamers', 50, 5, 45, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com')),
('PendingCraft', 'pendingcraft.example.com', 'V1_20_2', 'CREATIVE', 'Server is being approved', 0, 0, 0, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'user@example.com')),
('Rejected Realms', 'rejectedrealms.example.com', 'V1_19_4', 'ADVENTURE', 'Server rejected due to problems', 0, 10, -10, 'REJECTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'user@example.com')),
('Creative Builders', 'creativebuilders.example.com', 'V1_20_2', 'CREATIVE', 'Creative server with huge community', 70, 8, 62, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'admin@example.com'));

INSERT INTO users (username, email, password, profile_image, role, banned, deleted, registration_date, last_login)
VALUES
('steve', 'steve@example.com', '{noop}12345678', '/img/default_profile.png', 'USER', false, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('alex', 'alex@example.com', '{noop}12345678', '/img/default_profile.png', 'USER', true, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('notch', 'notch@example.com', '{noop}12345678', '/img/default_profile.png', 'USER', false, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO servers (server_name, ip_address, version, mode, description, up_votes, down_votes, score, status, created_at, updated_at, created_by)
VALUES
('PixelRealms', 'play.pixelrealms.net', 'V1_20_4', 'ADVENTURE', 'MMORPG-style survival server', 200, 10, 190, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'notch@example.com')),
('EnderCraft', 'end3rcraft.net', 'V1_19_3', 'SURVIVAL', 'Survive and thrive in an end-themed world.', 150, 5, 145, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'alex@example.com')),
('SkyLegends', 'sky.legends.net', 'V1_18_2', 'SKYBLOCK', 'Legendary Skyblock gameplay', 300, 30, 270, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'notch@example.com')),
('PvPMania', 'pvpmania.org', 'V1_20_1', 'PVP', 'No mercy PvP-focused server', 400, 35, 365, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'alex@example.com')),
('RedstoneHub', 'redstonehub.io', 'V1_20_2', 'CREATIVE', 'Creative server for redstone engineers', 120, 3, 117, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'notch@example.com')),
('BuilderBase', 'build.base.com', 'V1_20_2', 'CREATIVE', 'The place for pro builders.', 110, 8, 102, 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'steve@example.com')),
('FrozenMC', 'frozenmc.net', 'V1_20_1', 'SURVIVAL', 'Survival server in icy biome', 95, 2, 93, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'alex@example.com')),
('HardcoreNations', 'hardcore.nations.net', 'V1_20_4', 'HARDCORE', 'Hardcore factions with griefing allowed', 88, 12, 76, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'steve@example.com')),
('ChaosIsKey', 'chaosiskey.net', 'V1_20_3', 'ANARCHY_SMP', 'Pure chaos, zero rules', 350, 80, 270, 'APPROVED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'steve@example.com')),
('RejectedCraft', 'rejected.example.com', 'V1_18_2', 'SURVIVAL', 'Server failed approval', 3, 20, -17, 'REJECTED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, (SELECT id FROM users WHERE email = 'alex@example.com'));
