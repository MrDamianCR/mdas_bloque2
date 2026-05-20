-- phpMyAdmin SQL Dump
-- version 2.7.0-pl2
-- http://www.phpmyadmin.net
-- 
-- Servidor: oraclepr.uco.es
-- Tiempo de generación: 22-10-2025 a las 11:50:07
-- Versión del servidor: 5.1.73
-- Versión de PHP: 5.3.3
-- 
-- Base de datos: `i32gumuy`
-- 

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `Alquiler`
-- 

CREATE TABLE `Alquiler` (
  `id_alquiler` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fecha_inicio` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_fin` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `plazas_reservadas` tinyint(3) unsigned NOT NULL,
  `matricula` varchar(20) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  `dni_socio` varchar(15) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  PRIMARY KEY (`id_alquiler`),
  KEY `fk_alq_socio` (`dni_socio`),
  KEY `fk_alq_emb` (`matricula`)
) ENGINE=MyISAM AUTO_INCREMENT=5 DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

-- 
-- Volcar la base de datos para la tabla `Alquiler`
-- 

INSERT INTO `Alquiler` VALUES (1, '2025-06-20 10:00:00', '2025-06-20 18:00:00', 6, '5-V-9876-19', '87654321X');
INSERT INTO `Alquiler` VALUES (2, '2025-07-10 09:00:00', '2025-07-10 19:00:00', 6, '7-BA-1234-21', '12345678Z');
INSERT INTO `Alquiler` VALUES (3, '2025-08-22 07:00:00', '2025-08-22 12:00:00', 3, '1-AL-2201-18', '99887766B');
INSERT INTO `Alquiler` VALUES (4, '2025-09-12 10:00:00', '2025-09-13 10:00:00', 8, '2-GC-4521-20', '11223344A');

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `Embarcación`
-- 

CREATE TABLE `Embarcación` (
  `matricula` varchar(20) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  `tipo` varchar(30) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  `nombre` varchar(100) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  `numero_plazas` tinyint(3) unsigned NOT NULL,
  `dimensiones` varchar(50) CHARACTER SET utf8 COLLATE utf8_spanish2_ci DEFAULT NULL,
  `dni_patron` varchar(15) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  PRIMARY KEY (`matricula`),
  KEY `fk_emb_patron` (`dni_patron`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;

-- 
-- Volcar la base de datos para la tabla `Embarcación`
-- 

INSERT INTO `Embarcación` VALUES ('7-BA-1234-21', 'velero', 'Aquila', 6, '10.5m x 3.4m', '12345678Z');
INSERT INTO `Embarcación` VALUES ('5-V-9876-19', 'lancha', 'Mistral', 8, '8.2m x 2.9m', '55667788D');
INSERT INTO `Embarcación` VALUES ('2-GC-4521-20', 'catamarán', 'Iguazú', 10, '12.0m x 6.0m', '11223344A');
INSERT INTO `Embarcación` VALUES ('1-AL-2201-18', 'pesquero', 'Sirena', 4, '7.5m x 2.5m', '12345678Z');

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `Inscripción`
-- 

CREATE TABLE `Inscripcion` (
 `id_inscripcion` int(10) unsigned NOT NULL AUTO_INCREMENT,
 `tipo` enum('Individual','Familiar') CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `fecha_creacion` date NOT NULL,
 `cuota` decimal(10,2) NOT NULL DEFAULT '0.00',
 `dni_socio` varchar(15) CHARACTER SET utf8 COLLATE utf8_spanish2_ci DEFAULT NULL,
 PRIMARY KEY (`id_inscripcion`),
 KEY `fk_inscripcion_socio` (`dni_socio`),
 CONSTRAINT `fk_inscripcion_socio` FOREIGN KEY (`dni_socio`) REFERENCES `Socio` (`dni_socio`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=latin1

-- 
-- Volcar la base de datos para la tabla `Inscripción`
-- 

INSERT INTO `Inscripción` VALUES (1, 'alta', '2024-06-01 09:00:00', 150.00, '12345678Z');
INSERT INTO `Inscripción` VALUES (2, 'alta', '2024-06-01 09:00:00', 150.00, '12345678Z');
INSERT INTO `Inscripción` VALUES (3, 'alta', '2024-07-10 11:30:00', 150.00, '87654321X');
INSERT INTO `Inscripción` VALUES (4, 'renovacion', '2025-01-05 10:00:00', 120.00, '11223344A');
INSERT INTO `Inscripción` VALUES (5, 'alta', '2025-01-20 16:45:00', 150.00, '99887766B');
INSERT INTO `Inscripción` VALUES (6, 'baja', '2025-02-01 12:15:00', 0.00, '33445566C');
INSERT INTO `Inscripción` VALUES (7, 'renovacion', '2025-07-10 09:20:00', 120.00, '87654321X');

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `Patrón`
-- 

CREATE TABLE `Patron` (
 `dni_patron` varchar(15) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `nombre` varchar(100) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `apellidos` varchar(150) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `fecha_nacimiento` date NOT NULL,
 `fecha_exp_patron` date NOT NULL,
 PRIMARY KEY (`dni_patron`),
 CONSTRAINT `fk_patron_socio` FOREIGN KEY (`dni_patron`) REFERENCES `Socio` (`dni_socio`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1

-- 
-- Volcar la base de datos para la tabla `Patrón`
-- 

INSERT INTO `Patrón` VALUES ('12345678Z', 'Juan', 'García López', '1988-05-14', '2027-06-01');
INSERT INTO `Patrón` VALUES ('11223344A', 'Carlos', 'Ruiz Martín', '1975-12-22', '2026-04-15');
INSERT INTO `Patrón` VALUES ('55667788D', 'Ana', 'Morales Prieto', '1986-02-18', '2024-11-30');

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `Reserva`
-- 

CREATE TABLE `Reserva` (
  `id_reserva` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fecha` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `plazas_reservadas` tinyint(3) unsigned NOT NULL,
  `descripcion` varchar(255) CHARACTER SET utf8 COLLATE utf8_spanish2_ci DEFAULT NULL,
  `precio_total` decimal(10,2) NOT NULL DEFAULT '0.00',
  `dni_socio` varchar(15) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  `matricula` varchar(20) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
  PRIMARY KEY (`id_reserva`),
  KEY `fk_res_socio` (`dni_socio`),
  KEY `fk_res_emb` (`matricula`)
) ENGINE=MyISAM AUTO_INCREMENT=7 DEFAULT CHARSET=latin1 AUTO_INCREMENT=7 ;

-- 
-- Volcar la base de datos para la tabla `Reserva`
-- 

INSERT INTO `Reserva` VALUES (1, '2025-06-15 10:00:00', 4, 'Salida matinal', 240.00, '87654321X', '7-BA-1234-21');
INSERT INTO `Reserva` VALUES (2, '2025-07-01 09:00:00', 6, 'Regata entrenamiento', 600.00, '12345678Z', '2-GC-4521-20');
INSERT INTO `Reserva` VALUES (3, '2025-08-20 15:00:00', 3, 'Paseo de tarde', 180.00, '99887766B', '5-V-9876-19');
INSERT INTO `Reserva` VALUES (4, '2025-05-10 06:00:00', 2, 'Salida de pesca', 90.00, '33445566C', '1-AL-2201-18');
INSERT INTO `Reserva` VALUES (5, '2025-09-05 12:00:00', 5, 'Prácticas de patrón', 350.00, '11223344A', '7-BA-1234-21');
INSERT INTO `Reserva` VALUES (6, '2025-10-10 14:00:00', 8, 'Evento empresa', 800.00, '12345678Z', '5-V-9876-19');

-- --------------------------------------------------------

-- 
-- Estructura de tabla para la tabla `Socio`
-- 

-- Tabla Socio (InnoDB + FK a `Inscripción`)
CREATE TABLE `Socio` (
 `dni_socio` varchar(15) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `nombre` varchar(100) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `apellidos` varchar(150) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `fecha_nacimiento` date NOT NULL,
 `direccion` varchar(100) CHARACTER SET utf8 COLLATE utf8_spanish2_ci NOT NULL,
 `fecha_inscripcion` date NOT NULL,
 `es_patron` tinyint(1) NOT NULL,
 `id_inscripcion` int(10) unsigned DEFAULT NULL,
 `rol` enum('Titular','Adulto_Adicional','Hijo') NOT NULL,
 PRIMARY KEY (`dni_socio`),
 KEY `fk_socio_inscripcion` (`id_inscripcion`),
 CONSTRAINT `fk_socio_inscripcion` FOREIGN KEY (`id_inscripcion`) REFERENCES `Inscripcion` (`id_inscripcion`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1

-- 
-- Volcar la base de datos para la tabla `Socio`
-- 

INSERT INTO `Socio` VALUES ('12345678Z', 'Juan', 'García López', '1988-05-14', 'Calle Alcalá 123, Madrid', '2024-06-01', 1);
INSERT INTO `Socio` VALUES ('87654321X', 'María', 'Pérez Sánchez', '1990-09-02', 'Av. Diagonal 456, Barcelona', '2024-07-10', 0);
INSERT INTO `Socio` VALUES ('11223344A', 'Carlos', 'Ruiz Martín', '1975-12-22', 'Gran Vía 12, Madrid', '2023-04-15', 1);
INSERT INTO `Socio` VALUES ('99887766B', 'Laura', 'Fernández Ortiz', '1995-03-30', 'Paseo de Gracia 89, Barcelona', '2025-01-20', 0);
INSERT INTO `Socio` VALUES ('33445566C', 'Sergio', 'Domínguez Vega', '1982-08-05', 'Puerto Deportivo s/n, Valencia', '2022-09-05', 0);
