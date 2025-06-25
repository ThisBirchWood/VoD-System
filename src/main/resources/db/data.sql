INSERT INTO users ( google_id, username, email, name)
VALUES
  ('google-uid-001', 'alice', 'alice@example.com', 'Alice Example'),
  ( 'google-uid-002', 'bob', 'bob@example.com', 'Bob Example'),
  ('google-uid-003', 'carol', 'carol@example.com', 'Carol Example'),
  ( 'google-uid-004', 'wizard42', 'gandalf@middle.earth', 'Gandalf the Grey'),
  ( 'google-uid-005', 'catnap', 'whiskers@meowmail.com', 'Sir Whiskers McFluff'),
  ( 'google-uid-006', 'robotron', 'bender@futurama.tv', 'Bender Rodriguez'),
  ('google-uid-007', 'unicorn', 'sparkle@rainbow.com', 'Princess Sparklehoof'),
  ( 'google-uid-008', 'pirate', 'blackbeard@seas.com', 'Edward Teach'),
  ( 'google-uid-009', 'detective', 'holmes@bakerstreet.uk', 'Sherlock Holmes'),
  ( 'google-uid-010', 'timey', 'docbrown@delorean.net', 'Dr. Emmett Brown');

INSERT INTO clips (user_id, title, description, width, height, fps, duration, file_size, video_path)
VALUES
  (4, 'Fireworks Over Hobbiton', 'A magical display of fireworks by Gandalf.', 1920, 1080, 30, 120, 104857600, '/videos/fireworks_hobbiton.mp4'),
  (5, 'Catnap Chronicles', 'Sir Whiskers McFluff naps in 12 different positions.', 1280, 720, 24, 60, 52428800, '/videos/catnap_chronicles.mp4'),
  (6, 'Bite My Shiny Metal...', 'Bender shows off his new upgrades.', 1920, 1080, 60, 45, 73400320, '/videos/bender_upgrades.mp4'),
  (7, 'Rainbow Dash', 'Princess Sparklehoof gallops across a double rainbow.', 1920, 1080, 30, 90, 67108864, '/videos/rainbow_dash.mp4'),
  ( 8, 'Pirate Karaoke Night', 'Blackbeard sings sea shanties with his crew.', 1280, 720, 25, 180, 157286400, '/videos/pirate_karaoke.mp4'),
  ( 9, 'The Case of the Missing Sandwich', 'Sherlock Holmes investigates a lunchtime mystery.', 1920, 1080, 30, 75, 50331648, '/videos/missing_sandwich.mp4'),
  ( 10, '88 Miles Per Hour', 'Doc Brown demonstrates time travel with style.', 1920, 1080, 60, 30, 41943040, '/videos/88mph.mp4'),
  ( 1, 'Alice in Videoland', 'Alice explores a surreal digital wonderland.', 1280, 720, 30, 150, 94371840, '/videos/alice_videoland.mp4'),
  ( 2, 'Bob''s Building Bonanza', 'Bob constructs a house out of cheese.', 1920, 1080, 24, 200, 209715200, '/videos/bob_cheesehouse.mp4'),
  ( 3, 'Carol''s Coding Catastrophe', 'Carol debugs a spaghetti codebase.', 1280, 720, 30, 100, 73400320, '/videos/carol_coding.mp4');