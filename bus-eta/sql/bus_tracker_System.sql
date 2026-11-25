
CREATE DATABASE bus_tracker_System;
USE bus_tracker_System;




CREATE TABLE routes (
    route_id INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(100) NOT NULL
);

CREATE TABLE Stops (
    stop_id INT PRIMARY KEY AUTO_INCREMENT,
    route_id INT NOT NULL,
    Name VARCHAR(100) NOT NULL,
    Lat DOUBLE NOT NULL,
    Lng DOUBLE NOT NULL,
    FOREIGN KEY (route_id) REFERENCES routes(route_id)
);

CREATE TABLE Buses (
    bus_id INT PRIMARY KEY AUTO_INCREMENT,
    route_id INT NOT NULL,
    Name VARCHAR(100) NOT NULL,
    Current_lat DOUBLE NOT NULL,
    Current_Lng DOUBLE NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (route_id) REFERENCES routes(route_id)
);




INSERT INTO routes (route_id, Name) VALUES
(1, 'Kalpitiya - Puttalam'),
(2, 'Chilaw - Puttalam'),
(3, 'Colombo - Anuradhapura'),
(4, 'Batticola - Trincomalee');



-- Route 1: Kalpitiya - Puttalam
INSERT INTO Stops (route_id, Name, Lat, Lng) VALUES
(1, 'Kalpitiya Bus Stand',  8.240885932176683, 79.76353649077458),
(1, 'Pallivasalthurai',     8.180996505409459, 79.74058571104118),
(1, 'Mudalaipalai',         8.1587480583008,   79.73720961722488),
(1, 'Thalawila',            8.124688127937562, 79.72712374735796),
(1, 'Thigali',              8.101795666605614, 79.73293628447499),
(1, 'Etalai',               8.079545373233538, 79.7308266110183),
(1, 'Alanguda',             8.064440533330467, 79.73323764824303),
(1, 'Noracholai',           8.036034679230964, 79.73993602187679),
(1, 'Mampuri',              7.997450505102762, 79.74162286131404),
(1, 'Dhaluwa',              7.981604985030832, 79.75593642496173),
(1, 'Karamba',              7.973536258570483, 79.79749902179825),
(1, 'Palaviya',             7.979996343383443, 79.83312649041096),
(1, 'Thillayadi',           8.001509818187168, 79.83792060206737),
(1, 'Colombo Face',         8.03030761126302,  79.82874403085374),
(1, 'Puttalam Bus Stand',   8.028714401098924, 79.83327247928136);

-- Route 2: Chilaw - Puttalam 
INSERT INTO Stops (route_id, Name, Lat, Lng) VALUES
(2, 'Chilaw Bus Stand',     7.5769491,         79.7961301),
(2, 'Kakkapalliya',         7.6190000,         79.8180000),  
(2, 'Bangadeniya',          7.6390000,         79.8290000),  
(2, 'Arachchikattuwa',      7.6667094,         79.8359354),
(2, 'Rajakadaluwa',         7.6950000,         79.8330000),  
(2, 'Battuluoya',           7.73377,           79.8240715),
(2, 'Udappuwa Junction',    7.7650000,         79.8240000), 
(2, 'Mundel',               7.8042624,         79.8259099),
(2, 'Mangala Eliya',        7.8500000,         79.8230000),  
(2, 'Madurankuliya',        7.9089551,         79.8229679),
(2, 'Palaviya',             7.9783039,         79.8327987),
(2, 'Thillaiyadi',          8.0080000,         79.8340000),  
(2, 'Puttalam Bus Stand',   8.028714401098924, 79.83327247928136);

-- Route 3: Colombo - Anuradhapura 
INSERT INTO Stops (route_id, Name, Lat, Lng) VALUES
(3, 'Colombo Central Bus Stand',  6.9351053, 79.8543835),
(3, 'Peliyagoda',                 6.9620000, 79.8790000),
(3, 'Kelaniya',                   6.9680000, 79.9030000), 
(3, 'Dalugama',                   6.9750000, 79.9180000), 
(3, 'Kiribathgoda',               6.9790000, 79.9290000),  
(3, 'Mahara',                     6.9910000, 79.9400000),  
(3, 'Kadawatha',                  7.0046324, 79.954155),
(3, 'Kirillawala',                7.0270000, 79.9800000),  
(3, 'Imbulgoda',                  7.0550000, 80.0000000),  
(3, 'Yakkala',                    7.0870000, 80.0310000),  
(3, 'Kalagedihena',               7.1170000, 80.0580000),  
(3, 'Nittambuwa',                 7.1423639, 80.1037721),
(3, 'Pasyala',                    7.1700000, 80.1250000),  
(3, 'Weweldeniya',                7.1950000, 80.1550000),  
(3, 'Radawadunna',                7.2150000, 80.1800000),  
(3, 'Warakapola',                 7.22652,   80.1991281),
(3, 'Ambepussa',                  7.2500000, 80.1880000),  
(3, 'Alawwa',                     7.2930000, 80.2400000),  
(3, 'Polgahawela',                7.3330000, 80.3000000),  
(3, 'Pothuhera',                  7.4170000, 80.3330000),  
(3, 'Wehera',                     7.4600000, 80.3500000),  
(3, 'Kurunegala Bus Stand',       7.4875906, 80.364261),
(3, 'Mallawapitiya',              7.5100000, 80.3900000),  
(3, 'Ibbagamuwa',                 7.5500000, 80.4400000),  
(3, 'Melsiripura',                7.6400000, 80.5000000),  
(3, 'Kumbukgete',                 7.6900000, 80.5300000),  
(3, 'Galewela',                   7.7631255, 80.5730084),
(3, 'Ibbankatuwa',                7.8300000, 80.6200000),  
(3, 'Dambulla Bus Stand',         7.8705596, 80.6511585),
(3, 'Thalakiriyagama',            7.9800000, 80.6200000),  
(3, 'Madatugama',                 8.0100000, 80.6100000),  
(3, 'Kekirawa',                   8.0398443, 80.5983974),
(3, 'Maradankadawala',            8.1300000, 80.5600000),  
(3, 'Thirappane',                 8.2100000, 80.5000000), 
(3, 'Anuradhapura New Bus Stand', 8.3230981, 80.4027631);


-- Rout 4 : Baticola - Trincomalee
INSERT INTO Stops (route_id, Name, Lat, Lng) VALUES
(4,  'Batticaloa Bus Stand',       7.713900, 81.696000),
(4,  'Thiraimadu',                 7.735000, 81.680000),
(4,  'Mylampaveli',                7.752000, 81.660000),
(4,  'Eravur',                     7.766700, 81.616700),
(4,  'Chenkalady',                 7.798000, 81.585000),
(4,'Eastern University (Main Campus)',       7.7944366, 81.5790171),
(4,  'Kommathurai',                7.808000, 81.578000),
(4,  'Sittandy',                   7.820000, 81.570000),
(4,  'Murakkottanchenai',          7.840000, 81.550000),
(4,  'Kiran Junction',             7.866700, 81.533300),
(4, 'Kumburumoolai',              7.890000, 81.530000),
(4, 'Valaichchenai',              7.916700, 81.533300),
(4, 'Oddamavadi',                 7.933300, 81.516700),
(4, 'Mancholai',                  7.955000, 81.495000),
(4, 'Kayankerny',                 7.975000, 81.470000),
(4, 'Mankerny',                   8.000000, 81.450000),
(4, 'Panichchankerny',            8.040000, 81.430000),
(4, 'Kandalady',                  8.090000, 81.425000),
(4, 'Vakarai',                    8.150000, 81.416700),
(4, 'Palchenai',                  8.200000, 81.400000),
(4, 'Kathiraveli',                8.233300, 81.383300),
(4, 'Verugal',                    8.263300, 81.360000),
(4, 'Ilangaithurai',              8.300000, 81.330000),
(4, 'Echchilampattu',             8.340000, 81.300000),
(4, 'Kiliveddy',                  8.383300, 81.266700),
(4, 'Thoppur Junction',           8.420000, 81.230000),
(4, 'Kurinchakerny',              8.465000, 81.195000),
(4, 'Kinniya',                    8.483300, 81.183300),
(4, 'Kinniya Bridge (north end)', 8.505000, 81.185000),
(4, 'China Bay',                  8.540000, 81.180000),
(4, 'Trincomalee Bus Stand',      8.571100, 81.233500);



INSERT INTO Buses (route_id, Name, Current_lat, Current_Lng) VALUES
(1, 'Rilsan Express',     8.240885932176683, 79.76353649077458),
(1, 'Power Express', 8.240885932176683, 79.76353649077458),
(1, 'Lagoon Star',        8.240885932176683, 79.76353649077458),
(1, 'Kite Surf Express',  8.240885932176683, 79.76353649077458),
(1, 'Dolphin Line',       8.240885932176683, 79.76353649077458),
(1, 'UDS Express',      8.240885932176683, 79.76353649077458),

(2, 'Hirushi Queen',      7.5769491, 79.7961301),
(2, 'Rilsan Super Line',          7.5769491, 79.7961301),
(2, 'Spider Express',       7.5769491, 79.7961301),
(2, 'Mundel Mail',        7.5769491, 79.7961301),
(2, 'Everest',      7.5769491, 79.7961301),
(2, 'Green Super',         7.5769491, 79.7961301),

(3, 'Rajarata Express',   6.9351053, 79.8543835),
(3, 'Amna Express',   6.9351053, 79.8543835),
(3, 'Arafath Star',      6.9351053, 79.8543835),
(3, 'Lanka Pride',        6.9351053, 79.8543835),
(3, 'Kurunegala Rider',   6.9351053, 79.8543835),
(3, 'Rilsan Express',     6.9351053, 79.8543835),

(4,  'Rilsan Express',       7.713900, 81.696000),
(4,  'Hirushi Express',       7.713900, 81.696000),
(4,  'Shenali Express',       7.713900, 81.696000),
(4,  'Arafath super Line',       7.713900, 81.696000);
