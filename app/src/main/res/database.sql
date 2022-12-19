CREATE TABLE geopointTable (
  geoID INTEGER PRIMARY KEY AUTOINCREMENT
  , siteID INTEGER
  , siteName TEXT
  , objectType TEXT
  , latitude REAL
  , longitude REAL
  , altitude REAL
  , accuracy REAL
  , time TEXT
);

CREATE TABLE feederTable (
  feederID INTEGER PRIMARY KEY AUTOINCREMENT
  , feederName TEXT
  , ratedCapacity INTEGER
);

CREATE TABLE siteTable (
  siteID INTEGER PRIMARY KEY AUTOINCREMENT
  , feederID INTEGER
  , siteName TEXT
);

CREATE TABLE transformerTable (
  transformerID INTEGER PRIMARY KEY AUTOINCREMENT
  , siteID INTEGER
  , ratedCapacity INTEGER
  , powerFactor REAL
  , voltage TEXT
);

CREATE TABLE poleTable (
  poleID INTEGER PRIMARY KEY AUTOINCREMENT
  , siteID INTEGER
  , transformerID INTEGER
  , prevPoleID INTEGER
);

CREATE TABLE homeTable (
  homeID INTEGER PRIMARY KEY AUTOINCREMENT
  , siteID INTEGER
  , poleID INTEGER
  , consumerID TEXT
  , surveyorName TEXT
  , consumerName TEXT
  , fathersName TEXT
  , mobile INTEGER
  , lastPayment REAL
  , preWill TEXT
  , hoursAvail INTEGER
  , monthlyBill REAL
  , category TEXT
  , voltage TEXT
  , load INTEGER
  , theft TEXT
  , probFace TEXT
  , notes TEXT
);
