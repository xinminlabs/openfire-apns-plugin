# $Revision$
# $Date$

INSERT INTO ofVersion (name, version) VALUES ('openfire-apns', 0);

CREATE TABLE ofAPNS (
	JID VARCHAR(200) NOT NULL,
	devicetoken CHAR(64) NOT NULL,
  PRIMARY KEY (JID)
);
