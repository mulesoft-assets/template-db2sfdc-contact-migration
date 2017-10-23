CREATE TABLE `contact` (
  `ID` int(10) NOT NULL AUTO_INCREMENT,
  `AccountName` varchar(200) DEFAULT NULL,
  `FirstName` varchar(100) DEFAULT NULL,
  `LastName` varchar(200) DEFAULT NULL,
  `Phone` varchar(200) DEFAULT NULL,
  `SalesforceId` varchar(200) DEFAULT '',
  `LastModifiedById` varchar(200) DEFAULT 'mule@localhost',
  `Name` varchar(200) DEFAULT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `LastModifiedDate` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `ID` (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=10829 DEFAULT CHARSET=utf8;