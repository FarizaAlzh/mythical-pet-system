INSERT INTO pets (name, species, danger_level, temperament, magic_level, adoption_status, description, image_url)
SELECT 'Nimbus', 'Cloud Fox', 3, 'Playful', 7, 'PENDING', 'A floating fox that leaves sparkling mist behind.','/pet_images/Nimbus.png'
WHERE NOT EXISTS (
    SELECT 1 FROM pets WHERE name = 'Nimbus'
);

INSERT INTO pets (name, species, danger_level, temperament, magic_level, adoption_status, description, image_url)
SELECT 'Emberclaw', 'Mini Dragon', 6, 'Loyal', 9, 'PENDING', 'A small dragon that warms the room when happy.','/pet_images/Emberclaw.png'
WHERE NOT EXISTS (
    SELECT 1 FROM pets WHERE name = 'Emberclaw'
);

INSERT INTO pets (name, species, danger_level, temperament, magic_level, adoption_status, description, image_url)
SELECT 'Moonwhisker', 'Lunar Cat', 2, 'Calm', 8, 'PENDING', 'A silver cat that glows softly at night.','/pet_images/Moonwhisker.png'
WHERE NOT EXISTS (
    SELECT 1 FROM pets WHERE name = 'Moonwhisker'
);

UPDATE pets
SET image_url = '/pet_images/Nimbus.png'
WHERE name = 'Nimbus' AND image_url IS NULL;

UPDATE pets
SET image_url = '/pet_images/Emberclaw.png'
WHERE name = 'Emberclaw' AND image_url IS NULL;

UPDATE pets
SET image_url = '/pet_images/Moonwhisker.png'
WHERE name = 'Moonwhisker' AND image_url IS NULL;
