-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 08, 2025 at 10:38 AM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `programmed_escapist`
--

-- --------------------------------------------------------

--
-- Table structure for table `achievement`
--

CREATE TABLE `achievement` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `unlocked` tinyint(1) NOT NULL DEFAULT 0,
  `unlockDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `enemy`
--

CREATE TABLE `enemy` (
  `id` int(11) NOT NULL,
  `tier` varchar(10) NOT NULL,
  `difficulty` varchar(10) NOT NULL,
  `currentName` varchar(255) DEFAULT NULL,
  `maxHP` int(11) DEFAULT NULL,
  `hp` int(11) DEFAULT NULL,
  `minDmg` int(11) DEFAULT NULL,
  `maxDmg` int(11) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  `gluttonyCooldown` int(11) DEFAULT NULL,
  `bleedingActive` tinyint(1) DEFAULT NULL,
  `stunnedForNextTurn` tinyint(1) DEFAULT NULL,
  `nextAttackIsDoubleDamage` tinyint(1) DEFAULT NULL,
  `manaDrainActive` tinyint(1) DEFAULT NULL,
  `manaDrainTurns` int(11) DEFAULT NULL,
  `playerDamageReduced` tinyint(1) DEFAULT NULL,
  `damageReductionTurns` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `enemy`
--

INSERT INTO `enemy` (`id`, `tier`, `difficulty`, `currentName`, `maxHP`, `hp`, `minDmg`, `maxDmg`, `level`, `gluttonyCooldown`, `bleedingActive`, `stunnedForNextTurn`, `nextAttackIsDoubleDamage`, `manaDrainActive`, `manaDrainTurns`, `playerDamageReduced`, `damageReductionTurns`) VALUES
(1, 'WEAK', 'NORMAL', 'Virus', 80, 80, 10, 18, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 'NORMAL', 'NORMAL', 'Trojan', 120, 120, 15, 28, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 'STRONG', 'NORMAL', 'Malware', 200, 200, 30, 48, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'WEAK', 'NORMAL', 'Spyware', 70, 70, 8, 16, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 'NORMAL', 'NORMAL', 'Ransomware', 130, 130, 17, 31, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(6, 'WEAK', 'NORMAL', 'Worm', 90, 90, 12, 20, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(7, 'WEAK', 'NORMAL', 'Bug', 75, 75, 9, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(8, 'STRONG', 'NORMAL', 'Rootkit', 220, 220, 35, 55, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(9, 'NORMAL', 'NORMAL', 'Phishing Scam', 110, 110, 13, 25, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(10, 'NORMAL', 'NORMAL', 'Adware', 100, 100, 12, 21, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(11, 'NORMAL', 'NORMAL', 'Keylogger', 95, 95, 11, 22, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(12, 'NORMAL', 'NORMAL', 'Botnet', 130, 130, 16, 29, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(13, 'STRONG', 'NORMAL', 'Exploit', 180, 180, 24, 40, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(14, 'STRONG', 'NORMAL', 'Zero Day', 180, 180, 24, 40, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(15, 'NORMAL', 'NORMAL', 'Firewall Guard', 110, 110, 13, 27, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(16, 'NORMAL', 'NORMAL', 'Antivirus', 105, 105, 13, 25, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(17, 'NORMAL', 'NORMAL', 'Debugger', 100, 100, 10, 20, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(18, 'NORMAL', 'NORMAL', 'System Monitor', 110, 110, 11, 22, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(19, 'NORMAL', 'NORMAL', 'Backup Service', 120, 120, 13, 22, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(20, 'NORMAL', 'NORMAL', 'Patch Manager', 115, 115, 15, 30, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(21, 'NORMAL', 'NORMAL', 'Security Scanner', 90, 90, 10, 21, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(22, 'STRONG', 'NORMAL', 'Data Miner', 180, 180, 23, 39, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(23, 'STRONG', 'NORMAL', 'Cryptojacker', 190, 190, 25, 42, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(24, 'STRONG', 'NORMAL', 'DDoS Bot', 200, 200, 27, 45, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(25, 'STRONG', 'NORMAL', 'Logic Bomb', 210, 210, 28, 48, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(26, 'NORMAL', 'NORMAL', 'Macro Virus', 80, 80, 14, 30, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(27, 'NORMAL', 'NORMAL', 'File Infector', 90, 90, 9, 18, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(28, 'NORMAL', 'NORMAL', 'Network Worm', 100, 100, 13, 24, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(29, 'NORMAL', 'NORMAL', 'Drive By', 88, 88, 10, 18, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(30, 'NORMAL', 'NORMAL', 'Clickjacking', 95, 95, 11, 21, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(31, 'NORMAL', 'NORMAL', 'Session Hijacker', 97, 97, 12, 21, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(32, 'STRONG', 'NORMAL', 'Man in the Middle', 170, 170, 21, 38, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(33, 'NORMAL', 'NORMAL', 'Credential Harvester', 113, 113, 13, 22, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(34, 'NORMAL', 'NORMAL', 'Brute Force', 115, 115, 14, 25, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(35, 'NORMAL', 'NORMAL', 'Dictionary Attack', 90, 90, 10, 17, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(36, 'NORMAL', 'NORMAL', 'Rainbow Table', 100, 100, 12, 19, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(37, 'NORMAL', 'NORMAL', 'Social Engineer', 95, 95, 9, 15, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(38, 'NORMAL', 'NORMAL', 'Pharming Attack', 105, 105, 11, 17, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(39, 'NORMAL', 'NORMAL', 'DNS Poisoner', 110, 110, 12, 23, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(40, 'NORMAL', 'NORMAL', 'ARP Spoofer', 92, 92, 13, 20, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(41, 'NORMAL', 'NORMAL', 'IP Spoofer', 95, 95, 10, 20, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(42, 'NORMAL', 'NORMAL', 'Packet Sniffer', 87, 87, 8, 16, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(43, 'NORMAL', 'NORMAL', 'Port Scanner', 90, 90, 8, 16, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(44, 'NORMAL', 'NORMAL', 'Vulnerability Scanner', 95, 95, 10, 20, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(45, 'STRONG', 'NORMAL', 'Exploit Kit', 200, 200, 30, 52, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(46, 'NORMAL', 'NORMAL', 'Command Injector', 120, 120, 15, 29, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(47, 'NORMAL', 'NORMAL', 'Cross Site', 110, 110, 12, 25, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(48, 'NORMAL', 'NORMAL', 'Script Kiddie', 105, 105, 15, 29, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(49, 'STRONG', 'NORMAL', 'Black Hat', 220, 220, 38, 58, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `equipment`
--

CREATE TABLE `equipment` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(10) NOT NULL,
  `rarity` varchar(10) NOT NULL,
  `statBonus` int(11) DEFAULT NULL,
  `specialEffect` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `equipment`
--

INSERT INTO `equipment` (`id`, `name`, `type`, `rarity`, `statBonus`, `specialEffect`) VALUES
(1, 'Iron Sword', 'WEAPON', 'COMMON', 5, NULL),
(2, 'Steel Sword', 'WEAPON', 'COMMON', 7, NULL),
(3, 'Exploit Kit', 'WEAPON', 'UNCOMMON', 8, NULL),
(4, 'DDoS Script', 'WEAPON', 'RARE', 12, 'Stun enemy'),
(5, 'Rootkit Module', 'WEAPON', 'EPIC', 16, 'Drain enemy strength'),
(6, 'Debug Tool', 'WEAPON', 'COMMON', 4, NULL),
(7, 'Patch Kit', 'WEAPON', 'UNCOMMON', 8, 'Heal on hit'),
(8, 'Advanced Exploit', 'WEAPON', 'UNCOMMON', 8, NULL),
(9, 'Zero-Day Exploit', 'WEAPON', 'RARE', 12, NULL),
(10, 'Injection Tool', 'WEAPON', 'COMMON', 6, NULL),
(11, 'Staff of Fireballs', 'WEAPON', 'RARE', 13, 'AoE fire'),
(12, 'Staff of Ice Storm', 'WEAPON', 'RARE', 13, 'AoE ice'),
(13, 'Rootkit Code', 'WEAPON', 'LEGENDARY', 20, 'Permanent weaken'),
(14, 'Iron Dagger', 'WEAPON', 'COMMON', 4, NULL),
(15, 'Ebony Dagger', 'WEAPON', 'EPIC', 15, NULL),
(16, 'Firewall Vest', 'ARMOR', 'COMMON', 4, NULL),
(17, 'Encryption Cloak', 'ARMOR', 'UNCOMMON', 7, NULL),
(18, 'Protection Robe', 'ARMOR', 'RARE', 9, 'Reflect damage'),
(19, 'Chain Mail', 'ARMOR', 'UNCOMMON', 8, NULL),
(20, 'Plate Armor', 'ARMOR', 'RARE', 12, NULL),
(21, 'Coder Robes', 'ARMOR', 'COMMON', 5, NULL),
(22, 'Dragonbone Armor', 'ARMOR', 'LEGENDARY', 20, 'HP regen'),
(23, 'Nightshade Cloak', 'ARMOR', 'EPIC', 15, NULL),
(24, 'Health Potion', 'ITEM', 'COMMON', 0, 'Heal 50 HP'),
(25, 'Mana Potion', 'ITEM', 'COMMON', 0, 'Restore 50 Mana'),
(26, 'Potion of Ultimate Healing', 'ITEM', 'RARE', 0, 'Full HP heal'),
(27, 'Amulet of Talos', 'ITEM', 'EPIC', 0, 'Special boost'),
(28, 'Ancient Relic', 'ITEM', 'RARE', 0, 'Quest reward'),
(29, 'Energy Drink', 'ITEM', 'COMMON', 0, 'Heal 50 HP'),
(30, 'Caffeine Shot', 'ITEM', 'COMMON', 0, 'Restore 50 Mana'),
(31, 'Ultimate Potion', 'ITEM', 'EPIC', 0, 'Max HP restore');

-- --------------------------------------------------------

--
-- Table structure for table `faction`
--

CREATE TABLE `faction` (
  `id` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  `isMember` tinyint(1) DEFAULT 0,
  `reputation` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `faction`
--

INSERT INTO `faction` (`id`, `name`, `isMember`, `reputation`) VALUES
(1, 'Hackers Alliance', 0, 0),
(2, 'Cyber Thieves', 0, 0),
(3, 'Shadow Coders', 0, 0),
(4, 'Tech University', 0, 0),
(5, 'Firewall Guardians', 0, 0),
(6, 'Companions', 0, 0),
(7, 'Thieves Guild', 0, 0),
(8, 'Dark Brotherhood', 0, 0),
(9, 'College of Winterhold', 0, 0),
(10, 'Imperial Legion', 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `hero`
--

CREATE TABLE `hero` (
  `id` int(11) NOT NULL,
  `className` varchar(40) NOT NULL,
  `maxHP` int(11) DEFAULT NULL,
  `hp` int(11) DEFAULT NULL,
  `minDmg` int(11) DEFAULT NULL,
  `maxDmg` int(11) DEFAULT NULL,
  `maxMana` int(11) DEFAULT NULL,
  `mana` int(11) DEFAULT NULL,
  `gold` int(11) DEFAULT 0,
  `xp` int(11) DEFAULT 0,
  `level` int(11) DEFAULT 1,
  `xpToLevel` int(11) DEFAULT 50,
  `difficulty` varchar(10) NOT NULL,
  `hardcoreMode` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `heroachievement`
--

CREATE TABLE `heroachievement` (
  `heroId` int(11) NOT NULL,
  `achievementId` varchar(255) NOT NULL,
  `unlockDate` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `heroequipment`
--

CREATE TABLE `heroequipment` (
  `heroId` int(11) NOT NULL,
  `weapon` varchar(64) DEFAULT NULL,
  `armor` varchar(64) DEFAULT NULL,
  `accessory` varchar(64) DEFAULT NULL,
  `weaponBonus` int(11) DEFAULT 0,
  `armorBonus` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `herofaction`
--

CREATE TABLE `herofaction` (
  `heroId` int(11) NOT NULL,
  `factionId` int(11) NOT NULL,
  `reputation` int(11) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `inventoryitem`
--

CREATE TABLE `inventoryitem` (
  `id` int(11) NOT NULL,
  `heroId` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `quantity` int(11) DEFAULT 1,
  `weight` float DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `location`
--

CREATE TABLE `location` (
  `id` int(11) NOT NULL,
  `name` varchar(128) NOT NULL,
  `description` text DEFAULT NULL,
  `dangerLevel` int(11) DEFAULT 1,
  `hasTown` tinyint(1) DEFAULT 0,
  `environmentalEffect` varchar(32) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `location`
--

INSERT INTO `location` (`id`, `name`, `description`, `dangerLevel`, `hasTown`, `environmentalEffect`) VALUES
(1, 'Central Server Hub', 'A vast data center with interconnected servers, home to programmers and the mighty Hackers Alliance.', 1, 1, NULL),
(2, 'Dark Web Forest', 'A dense network shrouded in encryption, hiding cybercriminals and data smugglers.', 2, 1, NULL),
(3, 'Firewall Cliffs', 'Towering firewalls overlooking the digital sea, home to the Firewall Guardians and elite coders.', 3, 1, NULL),
(4, 'Frozen Code Tundra', 'A frozen wasteland where hackers study ancient algorithms at the Tech University.', 4, 0, 'Blizzard'),
(5, 'Corrupted Data Ruins', 'Ancient server ruins carved into mountains, plagued by malware and dark code.', 5, 0, 'Cave-in'),
(6, 'Backup Coast', 'A chilly coastal server battered by data storms, known for its hardy backups.', 2, 1, NULL),
(7, 'Malware Forest', 'A lush network with towering data trees, haunted by viruses and cybercriminals.', 2, 1, NULL),
(8, 'Glitch Marshes', 'A foggy swamp filled with buggy code and dangerous glitches.', 3, 1, 'Thick Fog'),
(9, 'Encrypted Snowfields', 'A snow-covered expanse surrounding the ancient city of the encrypted.', 3, 1, NULL),
(10, 'Data Stream Valley', 'A peaceful valley with a rushing data stream, home to miners and traders.', 1, 1, NULL),
(11, 'Forgotten Cache Barrow', 'An ancient data cache filled with traps and corrupt files.', 4, 0, NULL),
(12, 'Summit Server', 'The sacred mountain server of the Greybeards, shrouded in mist.', 5, 0, NULL),
(13, 'Remote Node Village', 'A small settlement at the base of the Summit Server.', 1, 1, NULL),
(14, 'Firewall Bridge', 'A strategic crossing with a firewall bridge shaped like a dragon.', 2, 1, NULL),
(15, 'Mining Hills', 'Rugged hills rich with data mines, contested by ransomware.', 3, 1, NULL),
(16, 'Fertile Fields', 'Fertile data fields known for bountiful code and peaceful folk.', 1, 1, NULL),
(17, 'Crashed System Ruins', 'A destroyed server recently ravaged by a rootkit attack.', 3, 0, NULL),
(18, 'Underground Network', 'A vast underground network lit by glowing LEDs and server ruins.', 5, 0, 'Glowing Spores'),
(19, 'Void Cache', 'A desolate plane of the void filled with lost data and necrotic energy.', 5, 0, 'Soul Drain'),
(20, 'Hidden Partition', 'A hidden glacial partition home to ancient malware temples.', 4, 0, 'Ancient Power'),
(21, 'Overclocked Springs', 'Steaming geothermal pools surrounded by volcanic circuits.', 3, 0, 'Soothing Vapors'),
(22, 'Bug Bog', 'A treacherous wetland teeming with dangerous bugs.', 3, 0, NULL),
(23, 'Frozen Sector', 'A stark, snowy landscape with scattered server ruins.', 2, 0, NULL),
(24, 'Isolated Coast', 'A frozen shoreline littered with crashed servers and ice floes.', 3, 0, NULL),
(25, 'Vampire Server', 'A foreboding vampire server on a remote island.', 5, 0, 'Vampiric Aura'),
(26, 'Ash Node', 'A Dunmer colony on the ash-covered island of Solstheim.', 4, 1, NULL),
(27, 'Wizard Tower', 'A Telvanni wizard tower surrounded by ash and fungal growths.', 4, 0, NULL),
(28, 'Nordic Village', 'A small Nordic settlement on Solstheim, devoted to the All-Maker.', 2, 1, NULL),
(29, 'Warrior Hall', 'A warrior lodge on Solstheim, recently reclaimed from Riekling.', 3, 1, NULL),
(30, 'Forbidden Realm', 'The otherworldly realm of Hermaeus Mora, filled with forbidden knowledge.', 5, 0, 'Forbidden Knowledge');

-- --------------------------------------------------------

--
-- Table structure for table `locationenemy`
--

CREATE TABLE `locationenemy` (
  `locationId` int(11) NOT NULL,
  `enemyName` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `locationfeature`
--

CREATE TABLE `locationfeature` (
  `locationId` int(11) NOT NULL,
  `featureName` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `locationfeature`
--

INSERT INTO `locationfeature` (`locationId`, `featureName`) VALUES
(1, 'Black Market'),
(1, 'Hackers Den'),
(2, 'Cyber Thieves Hideout'),
(2, 'Encrypted Keep'),
(3, 'Blue Palace'),
(3, 'Docks'),
(4, 'Frozen Archives'),
(4, 'Tech University'),
(5, 'Data Museum'),
(5, 'Shadow Coders Sanctuary'),
(6, 'Backup Harbor'),
(6, 'Data Mine'),
(7, 'Jarl\'s Longhouse'),
(7, 'Malware Graveyard'),
(8, 'Glitch Inn'),
(8, 'Highmoon Hall'),
(9, 'Candlehearth Hall'),
(9, 'Palace of the Kings'),
(10, 'Data Stream Trader'),
(10, 'Sleeping Giant Inn'),
(11, 'Ancient Altar'),
(11, 'Code Wall'),
(12, 'Greybeard Sanctum'),
(12, 'Meditation Chamber');

-- --------------------------------------------------------

--
-- Table structure for table `quest`
--

CREATE TABLE `quest` (
  `id` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` text DEFAULT NULL,
  `currentObjectiveIndex` int(11) DEFAULT 0,
  `completed` tinyint(1) DEFAULT 0,
  `faction` varchar(128) DEFAULT NULL,
  `heroId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `questobjective`
--

CREATE TABLE `questobjective` (
  `id` int(11) NOT NULL,
  `questId` int(11) DEFAULT NULL,
  `objective` text DEFAULT NULL,
  `idx` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `questreward`
--

CREATE TABLE `questreward` (
  `questId` int(11) DEFAULT NULL,
  `rewardType` varchar(32) DEFAULT NULL,
  `rewardAmount` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `save_slots`
--

CREATE TABLE `save_slots` (
  `save_name` varchar(255) NOT NULL,
  `hero_id` int(11) NOT NULL,
  `last_modified` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `statuseffect`
--

CREATE TABLE `statuseffect` (
  `id` int(11) NOT NULL,
  `name` varchar(64) DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `modifier` float DEFAULT NULL,
  `targetStat` varchar(32) DEFAULT NULL,
  `damagePerTurn` int(11) DEFAULT NULL,
  `isBuff` tinyint(1) DEFAULT NULL,
  `heroId` int(11) DEFAULT NULL,
  `enemyId` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `achievement`
--
ALTER TABLE `achievement`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `enemy`
--
ALTER TABLE `enemy`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `equipment`
--
ALTER TABLE `equipment`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `faction`
--
ALTER TABLE `faction`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `hero`
--
ALTER TABLE `hero`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `heroachievement`
--
ALTER TABLE `heroachievement`
  ADD PRIMARY KEY (`heroId`,`achievementId`),
  ADD KEY `achievementId` (`achievementId`);

--
-- Indexes for table `heroequipment`
--
ALTER TABLE `heroequipment`
  ADD PRIMARY KEY (`heroId`);

--
-- Indexes for table `herofaction`
--
ALTER TABLE `herofaction`
  ADD PRIMARY KEY (`heroId`,`factionId`),
  ADD KEY `factionId` (`factionId`);

--
-- Indexes for table `inventoryitem`
--
ALTER TABLE `inventoryitem`
  ADD PRIMARY KEY (`id`),
  ADD KEY `heroId` (`heroId`);

--
-- Indexes for table `location`
--
ALTER TABLE `location`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `locationenemy`
--
ALTER TABLE `locationenemy`
  ADD PRIMARY KEY (`locationId`,`enemyName`);

--
-- Indexes for table `locationfeature`
--
ALTER TABLE `locationfeature`
  ADD PRIMARY KEY (`locationId`,`featureName`);

--
-- Indexes for table `quest`
--
ALTER TABLE `quest`
  ADD PRIMARY KEY (`id`),
  ADD KEY `heroId` (`heroId`);

--
-- Indexes for table `questobjective`
--
ALTER TABLE `questobjective`
  ADD PRIMARY KEY (`id`),
  ADD KEY `questId` (`questId`);

--
-- Indexes for table `questreward`
--
ALTER TABLE `questreward`
  ADD KEY `questId` (`questId`);

--
-- Indexes for table `save_slots`
--
ALTER TABLE `save_slots`
  ADD PRIMARY KEY (`save_name`),
  ADD KEY `hero_id_fk` (`hero_id`);

--
-- Indexes for table `statuseffect`
--
ALTER TABLE `statuseffect`
  ADD PRIMARY KEY (`id`),
  ADD KEY `heroId` (`heroId`),
  ADD KEY `enemyId` (`enemyId`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `enemy`
--
ALTER TABLE `enemy`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=50;

--
-- AUTO_INCREMENT for table `equipment`
--
ALTER TABLE `equipment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT for table `faction`
--
ALTER TABLE `faction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `hero`
--
ALTER TABLE `hero`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `inventoryitem`
--
ALTER TABLE `inventoryitem`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `location`
--
ALTER TABLE `location`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=31;

--
-- AUTO_INCREMENT for table `quest`
--
ALTER TABLE `quest`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `questobjective`
--
ALTER TABLE `questobjective`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `statuseffect`
--
ALTER TABLE `statuseffect`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `heroachievement`
--
ALTER TABLE `heroachievement`
  ADD CONSTRAINT `heroachievement_ibfk_1` FOREIGN KEY (`heroId`) REFERENCES `hero` (`id`),
  ADD CONSTRAINT `heroachievement_ibfk_2` FOREIGN KEY (`achievementId`) REFERENCES `achievement` (`id`);

--
-- Constraints for table `heroequipment`
--
ALTER TABLE `heroequipment`
  ADD CONSTRAINT `heroequipment_ibfk_1` FOREIGN KEY (`heroId`) REFERENCES `hero` (`id`);

--
-- Constraints for table `herofaction`
--
ALTER TABLE `herofaction`
  ADD CONSTRAINT `herofaction_ibfk_1` FOREIGN KEY (`heroId`) REFERENCES `hero` (`id`),
  ADD CONSTRAINT `herofaction_ibfk_2` FOREIGN KEY (`factionId`) REFERENCES `faction` (`id`);

--
-- Constraints for table `inventoryitem`
--
ALTER TABLE `inventoryitem`
  ADD CONSTRAINT `inventoryitem_ibfk_1` FOREIGN KEY (`heroId`) REFERENCES `hero` (`id`);

--
-- Constraints for table `locationenemy`
--
ALTER TABLE `locationenemy`
  ADD CONSTRAINT `locationenemy_ibfk_1` FOREIGN KEY (`locationId`) REFERENCES `location` (`id`);

--
-- Constraints for table `locationfeature`
--
ALTER TABLE `locationfeature`
  ADD CONSTRAINT `locationfeature_ibfk_1` FOREIGN KEY (`locationId`) REFERENCES `location` (`id`);

--
-- Constraints for table `quest`
--
ALTER TABLE `quest`
  ADD CONSTRAINT `quest_ibfk_1` FOREIGN KEY (`heroId`) REFERENCES `hero` (`id`);

--
-- Constraints for table `questobjective`
--
ALTER TABLE `questobjective`
  ADD CONSTRAINT `questobjective_ibfk_1` FOREIGN KEY (`questId`) REFERENCES `quest` (`id`);

--
-- Constraints for table `questreward`
--
ALTER TABLE `questreward`
  ADD CONSTRAINT `questreward_ibfk_1` FOREIGN KEY (`questId`) REFERENCES `quest` (`id`);

--
-- Constraints for table `save_slots`
--
ALTER TABLE `save_slots`
  ADD CONSTRAINT `hero_id_fk` FOREIGN KEY (`hero_id`) REFERENCES `hero` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `statuseffect`
--
ALTER TABLE `statuseffect`
  ADD CONSTRAINT `statuseffect_ibfk_1` FOREIGN KEY (`heroId`) REFERENCES `hero` (`id`),
  ADD CONSTRAINT `statuseffect_ibfk_2` FOREIGN KEY (`enemyId`) REFERENCES `enemy` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
