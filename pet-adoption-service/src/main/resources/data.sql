INSERT INTO pets (name, species, danger_level, temperament, magic_level, adoption_status, description)
SELECT 'Nimbus', 'Cloud Fox', 3, 'Playful', 7, 'PENDING', 'A floating fox that leaves sparkling mist behind.'
WHERE NOT EXISTS (
    SELECT 1 FROM pets WHERE name = 'Nimbus'
);

INSERT INTO pets (name, species, danger_level, temperament, magic_level, adoption_status, description)
SELECT 'Emberclaw', 'Mini Dragon', 6, 'Loyal', 9, 'PENDING', 'A small dragon that warms the room when happy.'
WHERE NOT EXISTS (
    SELECT 1 FROM pets WHERE name = 'Emberclaw'
);

INSERT INTO pets (name, species, danger_level, temperament, magic_level, adoption_status, description)
SELECT 'Moonwhisker', 'Lunar Cat', 2, 'Calm', 8, 'PENDING', 'A silver cat that glows softly at night.'
WHERE NOT EXISTS (
    SELECT 1 FROM pets WHERE name = 'Moonwhisker'
);
