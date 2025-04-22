-- --------------------------------------------------------
-- 主機:                           127.0.0.1
-- 伺服器版本:                        11.7.2-MariaDB - mariadb.org binary distribution
-- 伺服器作業系統:                      Win64
-- HeidiSQL 版本:                  12.10.0.7000
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- 傾印 locker_reservation_system 的資料庫結構
CREATE DATABASE IF NOT EXISTS `locker_reservation_system` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */;
USE `locker_reservation_system`;

-- 傾印  資料表 locker_reservation_system.locker 結構
CREATE TABLE IF NOT EXISTS `locker` (
  `locker_id` int(11) NOT NULL,
  `site` varchar(255) DEFAULT NULL,
  `capacity` int(11) NOT NULL,
  `usability` tinyint(1) NOT NULL,
  PRIMARY KEY (`locker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在傾印表格  locker_reservation_system.locker 的資料：~20 rows (近似值)
REPLACE INTO `locker` (`locker_id`, `site`, `capacity`, `usability`) VALUES
	(1, '1', 1, 1),
	(2, 'Site 2', 1, 1),
	(3, 'Site 3', 1, 1),
	(4, 'Site 4', 1, 1),
	(5, 'Site 5', 1, 1),
	(6, 'Site 6', 2, 1),
	(7, 'Site 7', 2, 1),
	(8, 'Site 8', 2, 1),
	(9, 'Site 9', 2, 1),
	(10, 'Site 10', 2, 1),
	(11, 'Site 11', 2, 1),
	(12, 'Site 12', 1, 1),
	(13, 'Site 13', 1, 1),
	(14, 'Site 14', 1, 1),
	(15, 'Site 15', 1, 1),
	(16, 'Site 16', 1, 1),
	(17, 'Site 17', 2, 1),
	(18, 'Site 18', 2, 1),
	(19, 'Site 19', 2, 1),
	(20, 'Site 20', 2, 1);

-- 傾印  資料表 locker_reservation_system.locker_date_detail 結構
CREATE TABLE IF NOT EXISTS `locker_date_detail` (
  `locker_date_detail_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `locker_id` int(11) NOT NULL,
  `date` datetime(6) NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `memo` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`locker_date_detail_id`) USING BTREE,
  UNIQUE KEY `unique_date_locker` (`date`,`locker_id`),
  UNIQUE KEY `UKeiof5mwjp9tsakl0i78dgrdgc` (`date`,`locker_id`),
  KEY `locker_id` (`locker_id`) USING BTREE,
  CONSTRAINT `lockerdatedetail_ibfk_1` FOREIGN KEY (`locker_id`) REFERENCES `locker` (`locker_id`)
) ENGINE=InnoDB AUTO_INCREMENT=185 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在傾印表格  locker_reservation_system.locker_date_detail 的資料：~180 rows (近似值)
REPLACE INTO `locker_date_detail` (`locker_date_detail_id`, `locker_id`, `date`, `status`, `memo`) VALUES
	(5, 1, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(6, 1, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(7, 1, '2025-04-24 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(8, 1, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(9, 1, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(10, 1, '2025-04-27 00:00:00.000000', 'occupied', 'In use'),
	(11, 1, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(12, 1, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(13, 1, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(14, 2, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(15, 2, '2025-04-23 00:00:00.000000', 'occupied', 'In use'),
	(16, 2, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(17, 2, '2025-04-25 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(18, 2, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(19, 2, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(20, 2, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(21, 2, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(22, 2, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(23, 3, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(24, 3, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(25, 3, '2025-04-24 00:00:00.000000', 'occupied', 'In use'),
	(26, 3, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(27, 3, '2025-04-26 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(28, 3, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(29, 3, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(30, 3, '2025-04-29 00:00:00.000000', 'occupied', 'In use'),
	(31, 3, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(32, 4, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(33, 4, '2025-04-23 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(34, 4, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(35, 4, '2025-04-25 00:00:00.000000', 'occupied', 'In use'),
	(36, 4, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(37, 4, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(38, 4, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(39, 4, '2025-04-29 00:00:00.000000', 'occupied', 'In use'),
	(40, 4, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(41, 5, '2025-04-22 00:00:00.000000', 'occupied', 'In use'),
	(42, 5, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(43, 5, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(44, 5, '2025-04-25 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(45, 5, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(46, 5, '2025-04-27 00:00:00.000000', 'occupied', 'In use'),
	(47, 5, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(48, 5, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(49, 5, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(50, 6, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(51, 6, '2025-04-23 00:00:00.000000', 'occupied', 'In use'),
	(52, 6, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(53, 6, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(54, 6, '2025-04-26 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(55, 6, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(56, 6, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(57, 6, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(58, 6, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(59, 7, '2025-04-22 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(60, 7, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(61, 7, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(62, 7, '2025-04-25 00:00:00.000000', 'occupied', 'In use'),
	(63, 7, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(64, 7, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(65, 7, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(66, 7, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(67, 7, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(68, 8, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(69, 8, '2025-04-23 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(70, 8, '2025-04-24 00:00:00.000000', 'occupied', 'In use'),
	(71, 8, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(72, 8, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(73, 8, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(74, 8, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(75, 8, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(76, 8, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(77, 9, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(78, 9, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(79, 9, '2025-04-24 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(80, 9, '2025-04-25 00:00:00.000000', 'occupied', 'In use'),
	(81, 9, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(82, 9, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(83, 9, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(84, 9, '2025-04-29 00:00:00.000000', 'occupied', 'In use'),
	(85, 9, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(86, 10, '2025-04-22 00:00:00.000000', 'occupied', 'In use'),
	(87, 10, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(88, 10, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(89, 10, '2025-04-25 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(90, 10, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(91, 10, '2025-04-27 00:00:00.000000', 'occupied', 'In use'),
	(92, 10, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(93, 10, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(94, 10, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(95, 11, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(96, 11, '2025-04-23 00:00:00.000000', 'occupied', 'In use'),
	(97, 11, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(98, 11, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(99, 11, '2025-04-26 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(100, 11, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(101, 11, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(102, 11, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(103, 11, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(104, 12, '2025-04-22 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(105, 12, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(106, 12, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(107, 12, '2025-04-25 00:00:00.000000', 'occupied', 'In use'),
	(108, 12, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(109, 12, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(110, 12, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(111, 12, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(112, 12, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(113, 13, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(114, 13, '2025-04-23 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(115, 13, '2025-04-24 00:00:00.000000', 'occupied', 'In use'),
	(116, 13, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(117, 13, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(118, 13, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(119, 13, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(120, 13, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(121, 13, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(122, 14, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(123, 14, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(124, 14, '2025-04-24 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(125, 14, '2025-04-25 00:00:00.000000', 'occupied', 'In use'),
	(126, 14, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(127, 14, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(128, 14, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(129, 14, '2025-04-29 00:00:00.000000', 'occupied', 'In use'),
	(130, 14, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(131, 15, '2025-04-22 00:00:00.000000', 'occupied', 'In use'),
	(132, 15, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(133, 15, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(134, 15, '2025-04-25 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(135, 15, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(136, 15, '2025-04-27 00:00:00.000000', 'occupied', 'In use'),
	(137, 15, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(138, 15, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(139, 15, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(140, 16, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(141, 16, '2025-04-23 00:00:00.000000', 'occupied', 'In use'),
	(142, 16, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(143, 16, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(144, 16, '2025-04-26 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(145, 16, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(146, 16, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(147, 16, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(148, 16, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(149, 17, '2025-04-22 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(150, 17, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(151, 17, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(152, 17, '2025-04-25 00:00:00.000000', 'occupied', 'In use'),
	(153, 17, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(154, 17, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(155, 17, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(156, 17, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(157, 17, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(158, 18, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(159, 18, '2025-04-23 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(160, 18, '2025-04-24 00:00:00.000000', 'occupied', 'In use'),
	(161, 18, '2025-04-25 00:00:00.000000', 'available', 'No issues'),
	(162, 18, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(163, 18, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(164, 18, '2025-04-28 00:00:00.000000', 'occupied', 'In use'),
	(165, 18, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(166, 18, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(167, 19, '2025-04-22 00:00:00.000000', 'available', 'No issues'),
	(168, 19, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(169, 19, '2025-04-24 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(170, 19, '2025-04-25 00:00:00.000000', 'occupied', 'In use'),
	(171, 19, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(172, 19, '2025-04-27 00:00:00.000000', 'available', 'No issues'),
	(173, 19, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(174, 19, '2025-04-29 00:00:00.000000', 'occupied', 'In use'),
	(175, 19, '2025-04-30 00:00:00.000000', 'available', 'No issues'),
	(176, 20, '2025-04-22 00:00:00.000000', 'occupied', 'In use'),
	(177, 20, '2025-04-23 00:00:00.000000', 'available', 'No issues'),
	(178, 20, '2025-04-24 00:00:00.000000', 'available', 'No issues'),
	(179, 20, '2025-04-25 00:00:00.000000', 'maintenance', 'Scheduled maintenance'),
	(180, 20, '2025-04-26 00:00:00.000000', 'available', 'No issues'),
	(181, 20, '2025-04-27 00:00:00.000000', 'occupied', 'In use'),
	(182, 20, '2025-04-28 00:00:00.000000', 'available', 'No issues'),
	(183, 20, '2025-04-29 00:00:00.000000', 'available', 'No issues'),
	(184, 20, '2025-04-30 00:00:00.000000', 'available', 'No issues');

-- 傾印  資料表 locker_reservation_system.reservation 結構
CREATE TABLE IF NOT EXISTS `reservation` (
  `reservation_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `locker_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `barcode` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`reservation_id`),
  KEY `locker_id` (`locker_id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE,
  CONSTRAINT `reservation_ibfk_1` FOREIGN KEY (`locker_id`) REFERENCES `locker` (`locker_id`),
  CONSTRAINT `reservation_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在傾印表格  locker_reservation_system.reservation 的資料：~2 rows (近似值)
REPLACE INTO `reservation` (`reservation_id`, `locker_id`, `user_id`, `start_date`, `end_date`, `barcode`) VALUES
	(1, 1, 1, '2025-04-22', '2025-04-23', NULL),
	(2, 1, 1, '2025-04-22', '2025-04-22', NULL);

-- 傾印  資料表 locker_reservation_system.user 結構
CREATE TABLE IF NOT EXISTS `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `account_name` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  `is_admin` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `account_name` (`account_name`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 正在傾印表格  locker_reservation_system.user 的資料：~6 rows (近似值)
REPLACE INTO `user` (`user_id`, `account_name`, `password`, `phone_number`, `is_admin`) VALUES
	(1, 'admin', '123', '0909090909', 1),
	(2, 'testuser', 'testpassword', 'testphone', 0),
	(6, 'user2', '123', '0966666666', 0),
	(8, 'user3', '123', '123458786', 0),
	(11, 'user4', '123', '123465789', 0),
	(12, 'user5', '123', '123465789', 0);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
