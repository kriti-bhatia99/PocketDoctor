-- phpMyAdmin SQL Dump
-- version 5.1.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Jun 12, 2022 at 06:53 PM
-- Server version: 10.4.21-MariaDB
-- PHP Version: 8.0.10

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `pocket_doctor`
--

-- --------------------------------------------------------

--
-- Table structure for table `predictions`
--

CREATE TABLE `predictions` (
  `p_id` int(11) NOT NULL,
  `u_phone` varchar(15) NOT NULL,
  `p_date` varchar(20) NOT NULL,
  `p_type` varchar(20) NOT NULL,
  `p_output` varchar(100) NOT NULL,
  `p_probability` varchar(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `predictions`
--

INSERT INTO `predictions` (`p_id`, `u_phone`, `p_date`, `p_type`, `p_output`, `p_probability`) VALUES
(1, '+16505556789', '11-06-22 03:44:25', 'Brain Tumour', 'No Tumour', '89.25'),
(2, '+16505556789', '12-06-22 02:03:10', 'Diabetes', 'Diabetic', '57.89'),
(3, '+16505556789', '12-06-22 02:55:30', 'Cancer', 'Benign', '100.0'),
(4, '+16505556789', '12-06-22 03:00:30', 'Heart Stroke', 'Chances of Heart Stroke', ''),
(5, '+16505556789', '12-06-22 03:01:05', 'Cancer', 'Benign', '100.0'),
(6, '+16505556789', '12-06-22 10:19:03', 'Cancer', 'Benign', '100.0'),
(7, '+16505556789', '12-06-22 10:21:15', 'Brain Tumour', 'No Tumour', '91.42'),
(8, '+16505556789', '12-06-22 10:22:38', 'Brain Tumour', 'Glioma Tumour', '95.18'),
(9, '+16505556789', '12-06-22 10:22:59', 'Diabetes', 'Diabetic', '68.42'),
(10, '+16505556789', '12-06-22 10:23:16', 'Heart Stroke', 'Chances of Heart Stroke', '');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `u_id` int(11) NOT NULL,
  `u_name` varchar(100) DEFAULT NULL,
  `u_phone` varchar(15) NOT NULL,
  `u_gender` char(1) DEFAULT NULL,
  `u_age` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`u_id`, `u_name`, `u_phone`, `u_gender`, `u_age`) VALUES
(8, 'Test', '+16505556789', 'F', 35);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `predictions`
--
ALTER TABLE `predictions`
  ADD PRIMARY KEY (`p_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`u_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `predictions`
--
ALTER TABLE `predictions`
  MODIFY `p_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `u_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
