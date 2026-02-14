INSERT INTO dog (id, name, description) VALUES (97, 'Rocky', 'A brown Chihuahua known for being protective.');
INSERT INTO dog (id, name, description) VALUES (87, 'Bailey', 'A tan Dachshund known for being playful.');
INSERT INTO dog (id, name, description) VALUES (89, 'Charlie', 'A black Bulldog known for being curious.');
INSERT INTO dog (id, name, description) VALUES (67, 'Cooper', 'A tan Boxer known for being affectionate.');
INSERT INTO dog (id, name, description) VALUES (73, 'Max', 'A brindle Dachshund known for being energetic.');
INSERT INTO dog (id, name, description) VALUES (3, 'Buddy', 'A Poodle known for being calm.');
INSERT INTO dog (id, name, description) VALUES (93, 'Duke', 'A white German Shepherd known for being friendly.');
INSERT INTO dog (id, name, description) VALUES (63, 'Jasper', 'A grey Shih Tzu known for being protective.');
INSERT INTO dog (id, name, description) VALUES (69, 'Toby', 'A grey Doberman known for being playful.');
INSERT INTO dog (id, name, description) VALUES (101, 'Nala', 'A spotted German Shepherd known for being loyal.');
INSERT INTO dog (id, name, description) VALUES (61, 'Penny', 'A white Great Dane known for being protective.');
INSERT INTO dog (id, name, description) VALUES (1, 'Bella', 'A golden Poodle known for being calm.');
INSERT INTO dog (id, name, description) VALUES (91, 'Willow', 'A brindle Great Dane known for being calm.');
INSERT INTO dog (id, name, description) VALUES (5, 'Daisy', 'A spotted Poodle known for being affectionate.');
INSERT INTO dog (id, name, description) VALUES (95, 'Mia', 'A grey Great Dane known for being loyal.');
INSERT INTO dog (id, name, description) VALUES (71, 'Molly', 'A golden Chihuahua known for being curious.');
INSERT INTO dog (id, name, description) VALUES (65, 'Ruby', 'A white Great Dane known for being protective.');
INSERT INTO dog (id, name, description) VALUES (45, 'Prancer', 'A demonic, neurotic, man hating, animal hating, children hating dogs that look like gremlins.');
INSERT INTO users VALUES ('james', '{sha256}a94f80ed1a6677dedb489a6912e6c83a7c2a7ada2c4d739dd4a4ab9e454d3d875134fa4480b19c55', true) ON CONFLICT (username) DO NOTHING;
INSERT INTO users VALUES ('josh', '{bcrypt}$2a$10$AZP7caqON.QnI0a2pgzHJOEWeEMdSqfI/kNI7kGQh3eKOzCgg9xeS', true) ON CONFLICT (username) DO NOTHING;

INSERT INTO authorities VALUES ('james', 'ROLE_ADMIN') ON CONFLICT (username, authority) DO NOTHING;
INSERT INTO authorities VALUES ('james', 'ROLE_USER') ON CONFLICT (username, authority) DO NOTHING;
INSERT INTO authorities VALUES ('josh', 'ROLE_USER') ON CONFLICT (username, authority) DO NOTHING;
--
