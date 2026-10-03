-- One database instance, one schema and one user per Bounded Context (ADR0002).
-- Each user owns exactly its schema and cannot read the others. Local development only.
REVOKE CREATE ON SCHEMA public FROM PUBLIC;

CREATE USER recipecatalog WITH PASSWORD 'recipecatalog';
CREATE SCHEMA recipecatalog AUTHORIZATION recipecatalog;
REVOKE ALL ON SCHEMA recipecatalog FROM PUBLIC;

CREATE USER mealplanning WITH PASSWORD 'mealplanning';
CREATE SCHEMA mealplanning AUTHORIZATION mealplanning;
REVOKE ALL ON SCHEMA mealplanning FROM PUBLIC;

CREATE USER mealpreparation WITH PASSWORD 'mealpreparation';
CREATE SCHEMA mealpreparation AUTHORIZATION mealpreparation;
REVOKE ALL ON SCHEMA mealpreparation FROM PUBLIC;

CREATE USER cookingassistance WITH PASSWORD 'cookingassistance';
CREATE SCHEMA cookingassistance AUTHORIZATION cookingassistance;
REVOKE ALL ON SCHEMA cookingassistance FROM PUBLIC;

CREATE USER grandmaavatar WITH PASSWORD 'grandmaavatar';
CREATE SCHEMA grandmaavatar AUTHORIZATION grandmaavatar;
REVOKE ALL ON SCHEMA grandmaavatar FROM PUBLIC;

CREATE USER notification WITH PASSWORD 'notification';
CREATE SCHEMA notification AUTHORIZATION notification;
REVOKE ALL ON SCHEMA notification FROM PUBLIC;

CREATE USER media WITH PASSWORD 'media';
CREATE SCHEMA media AUTHORIZATION media;
REVOKE ALL ON SCHEMA media FROM PUBLIC;

CREATE USER sharing WITH PASSWORD 'sharing';
CREATE SCHEMA sharing AUTHORIZATION sharing;
REVOKE ALL ON SCHEMA sharing FROM PUBLIC;

CREATE USER cookprofile WITH PASSWORD 'cookprofile';
CREATE SCHEMA cookprofile AUTHORIZATION cookprofile;
REVOKE ALL ON SCHEMA cookprofile FROM PUBLIC;

CREATE USER consentmanagement WITH PASSWORD 'consentmanagement';
CREATE SCHEMA consentmanagement AUTHORIZATION consentmanagement;
REVOKE ALL ON SCHEMA consentmanagement FROM PUBLIC;
