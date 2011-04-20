DROP TABLE Users;
DROP TABLE Memberships;
DROP TABLE Messages;
DROP TABLE Rtt;

CREATE TABLE Users (
	username VARCHAR(64) NOT NULL,
	salt VARCHAR(128) NOT NULL,
	encrypted_password VARCHAR(256) NOT NULL,
	PRIMARY KEY (username)
);

CREATE TABLE Memberships (
	username VARCHAR(64) NOT NULL,
	gname VARCHAR(64) NOT NULL,
	PRIMARY KEY (username, gname),
	FOREIGN KEY (username) REFERENCES Users (username)
		ON DELETE CASCADE
);

CREATE TABLE Messages (
	recipient VARCHAR(64) NOT NULL,
	sender VARCHAR(64) NOT NULL,
	sqn INT NOT NULL,
	timestamp TIME NOT NULL,
	destination VARCHAR(64),
	message VARCHAR(1024),
	PRIMARY KEY (recipient, sender, sqn),
	FOREIGN KEY (recipient) REFERENCES Users (username)
		ON DELETE CASCADE,
	FOREIGN KEY (sender) REFERENCES Users (username)
		ON DELETE NO ACTION
);

CREATE TABLE Rtt (
	id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
	username VARCHAR(64) DEFAULT NULL,
	timestamp TIME DEFAULT NULL,
	rtt DOUBLE NOT NULL
);