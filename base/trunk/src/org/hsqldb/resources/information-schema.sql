-- author Fred Toussi (fredt@users dot sourceforge.net) version 1.9.0
/*system_procedures*/
SELECT ROUTINE_CATALOG AS PROCEDURE_CAT, ROUTINE_SCHEMA AS PROCEDURE_SCHEM,
ROUTINE_NAME AS PROCEDURE_NAME, 0, 0, 0,
CAST( NULL AS VARCHAR(256)) AS REMARKS,
CASE WHEN ROUTINE_TYPE = 'PROCEDURE' THEN 1 ELSE 2 END CASE AS PROCEDURE_TYPE,
SPECIFIC_NAME FROM INFORMATION_SCHEMA.ROUTINES

/*data_type_privileges*/
SELECT TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME,
'TABLE', DTD_IDENTIFIER
FROM COLUMNS
UNION
SELECT DOMAIN_CATALOG, DOMAIN_SCHEMA, DOMAIN_NAME,
'DOMAIN', DTD_IDENTIFIER
FROM DOMAINS
UNION
SELECT SPECIFIC_CATALOG, SPECIFIC_SCHEMA, SPECIFIC_NAME,
'ROUTINE', DTD_IDENTIFIER
FROM PARAMETERS
UNION
SELECT SPECIFIC_CATALOG, SPECIFIC_SCHEMA, SPECIFIC_NAME,
'ROUTINE', DTD_IDENTIFIER
FROM ROUTINES
WHERE DTD_IDENTIFIER IS NOT NULL
UNION
SELECT USER_DEFINED_TYPE_CATALOG, USER_DEFINED_TYPE_SCHEMA,
USER_DEFINED_TYPE_NAME, 'USER-DEFINED TYPE', SOURCE_DTD_IDENTIFIER
FROM USER_DEFINED_TYPES
WHERE SOURCE_DTD_IDENTIFIER IS NOT NULL
UNION
SELECT USER_DEFINED_TYPE_CATALOG, USER_DEFINED_TYPE_SCHEMA,
USER_DEFINED_TYPE_NAME, 'USER-DEFINED TYPE', REF_DTD_IDENTIFIER
FROM USER_DEFINED_TYPES
WHERE REF_DTD_IDENTIFIER IS NOT NULL;
/*sql_features*/
VALUES
('B011', 'Embedded Ada', '', '', 'NO', CAST(NULL AS CHARACTER), ''),
('B012', 'Embedded C', '', '', 'NO', NULL, ''),
('B013', 'Embedded COBOL', '', '', 'NO', NULL, ''),
('B014', 'Embedded Fortran', '', '', 'NO', NULL, ''),
('B015', 'Embedded MUMPS', '', '', 'NO', NULL, ''),
('B016', 'Embedded Pascal', '', '', 'NO', NULL, ''),
('B017', 'Embedded PL/I', '', '', 'NO', NULL, ''),
('B021', 'Direct SQL', '', '', 'YES', NULL, ''),
('B031', 'Basic dynamic SQL', '', '', 'NO', NULL, ''),
('B032', 'Extended dynamic SQL', '', '', 'NO', NULL, ''),
('B032', 'Extended dynamic SQL', '01', 'describe input statement', 'NO', NULL, ''),
('B033', 'Untyped SQL-invoked function arguments', '', '', 'NO', NULL, ''),
('B034', 'Dynamic specification of cursor attributes', '', '', 'NO', NULL, ''),
('B041', 'Extensions to embedded SQL exception declarations', '', '', 'NO', NULL, ''),
('B051', 'Enhanced execution rights', '', '', 'NO', NULL, ''),
('B111', 'Module language Ada', '', '', 'NO', NULL, ''),
('B112', 'Module language C', '', '', 'NO', NULL, ''),
('B113', 'Module language COBOL', '', '', 'NO', NULL, ''),
('B114', 'Module language Fortran', '', '', 'NO', NULL, ''),
('B115', 'Module language MUMPS', '', '', 'NO', NULL, ''),
('B116', 'Module language Pascal', '', '', 'NO', NULL, ''),
('B117', 'Module language PL/I', '', '', 'NO', NULL, ''),
('B121', 'Routine language Ada', '', '', 'NO', NULL, ''),
('B122', 'Routine language C', '', '', 'NO', NULL, ''),
('B123', 'Routine language COBOL', '', '', 'NO', NULL, ''),
('B124', 'Routine language Fortran', '', '', 'NO', NULL, ''),
('B125', 'Routine language MUMPS', '', '', 'NO', NULL, ''),
('B126', 'Routine language Pascal', '', '', 'NO', NULL, ''),
('B127', 'Routine language PL/I', '', '', 'NO', NULL, ''),
('B128', 'Routine language SQL', '', '', 'YES', NULL, 'only schema-contained routines'),
('C011', 'Call-Level Interface', '', '', 'YES', NULL, 'via JDBC'),
('E011', 'Numeric data types', '', '', 'YES', NULL, ''),
('E011', 'Numeric data types', '01', 'INTEGER and SMALLINT data types', 'YES', NULL, ''),
('E011', 'Numeric data types', '02', 'REAL, DOUBLE PRECISION, and FLOAT data types', 'YES', NULL, ''),
('E011', 'Numeric data types', '03', 'DECIMAL and NUMERIC data types', 'YES', NULL, ''),
('E011', 'Numeric data types', '04', 'Arithmetic operators', 'YES', NULL, ''),
('E011', 'Numeric data types', '05', 'Numeric comparison', 'YES', NULL, ''),
('E011', 'Numeric data types', '06', 'Implicit casting among the numeric data types', 'YES', NULL, ''),
('E021', 'Character data types', '', '', 'YES', NULL, ''),
('E021', 'Character string types', '01', 'CHARACTER data type', 'YES', NULL, ''),
('E021', 'Character string types', '02', 'CHARACTER VARYING data type', 'YES', NULL, ''),
('E021', 'Character string types', '03', 'Character literals', 'YES', NULL, ''),
('E021', 'Character string types', '04', 'CHARACTER_LENGTH function', 'YES', NULL, ''),
('E021', 'Character string types', '05', 'OCTET_LENGTH function', 'YES', NULL, ''),
('E021', 'Character string types', '06', 'SUBSTRING function', 'YES', NULL, ''),
('E021', 'Character string types', '07', 'Character concatenation', 'YES', NULL, ''),
('E021', 'Character string types', '08', 'UPPER and LOWER functions', 'YES', NULL, ''),
('E021', 'Character string types', '09', 'TRIM function', 'YES', NULL, ''),
('E021', 'Character string types', '10', 'Implicit casting among the character string types', 'YES', NULL, ''),
('E021', 'Character string types', '11', 'POSITION function', 'YES', NULL, ''),
('E021', 'Character string types', '12', 'Character comparison', 'YES', NULL, ''),
('E031', 'Identifiers', '', '', 'YES', NULL, ''),
('E031', 'Identifiers', '01', 'Delimited identifiers', 'YES', NULL, ''),
('E031', 'Identifiers', '02', 'Lower case identifiers', 'YES', NULL, ''),
('E031', 'Identifiers', '03', 'Trailing underscore', 'YES', NULL, ''),
('E051', 'Basic query specification', '', '', 'YES', NULL, ''),
('E051', 'Basic query specification', '01', 'SELECT DISTINCT', 'YES', NULL, ''),
('E051', 'Basic query specification', '02', 'GROUP BY clause', 'YES', NULL, ''),
('E051', 'Basic query specification', '04', 'GROUP BY can contain columns not in <select list>', 'YES', NULL, ''),
('E051', 'Basic query specification', '05', 'Select list items can be renamed', 'YES', NULL, ''),
('E051', 'Basic query specification', '06', 'HAVING clause', 'YES', NULL, ''),
('E051', 'Basic query specification', '07', 'Qualified * in select list', 'YES', NULL, ''),
('E051', 'Basic query specification', '08', 'Correlation names in the FROM clause', 'YES', NULL, ''),
('E051', 'Basic query specification', '09', 'Rename columns in the FROM clause', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '', '', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '01', 'Comparison predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '02', 'BETWEEN predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '03', 'IN predicate with list of values', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '04', 'LIKE predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '05', 'LIKE predicate ESCAPE clause', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '06', 'NULL predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '07', 'Quantified comparison predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '08', 'EXISTS predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '09', 'Subqueries in comparison predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '11', 'Subqueries in IN predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '12', 'Subqueries in quantified comparison predicate', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '13', 'Correlated subqueries', 'YES', NULL, ''),
('E061', 'Basic predicates and search conditions', '14', 'Search condition', 'YES', NULL, ''),
('E071', 'Basic query expressions', '', '', 'YES', NULL, ''),
('E071', 'Basic query expressions', '01', 'UNION DISTINCT table operator', 'YES', NULL, ''),
('E071', 'Basic query expressions', '02', 'UNION ALL table operator', 'YES', NULL, ''),
('E071', 'Basic query expressions', '03', 'EXCEPT DISTINCT table operator', 'YES', NULL, ''),
('E071', 'Basic query expressions', '05', 'Columns combined via table operators need not have exactly the same data type', 'YES', NULL, ''),
('E071', 'Basic query expressions', '06', 'Table operators in subqueries', 'YES', NULL, ''),
('E081', 'Basic Privileges', '', '', 'YES', NULL, ''),
('E081', 'Basic Privileges', '01', 'SELECT privilege', 'YES', NULL, ''),
('E081', 'Basic Privileges', '02', 'DELETE privilege', 'YES', NULL, ''),
('E081', 'Basic Privileges', '03', 'INSERT privilege at the table level', 'YES', NULL, ''),
('E081', 'Basic Privileges', '04', 'UPDATE privilege at the table level', 'YES', NULL, ''),
('E081', 'Basic Privileges', '05', 'UPDATE privilege at the column level', 'YES', NULL, ''),
('E081', 'Basic Privileges', '06', 'REFERENCES privilege at the table level', 'YES', NULL, ''),
('E081', 'Basic Privileges', '07', 'REFERENCES privilege at the column level', 'YES', NULL, ''),
('E081', 'Basic Privileges', '08', 'WITH GRANT OPTION', 'YES', NULL, ''),
('E081', 'Basic Privileges', '09', 'USAGE privilege', 'YES', NULL, ''),
('E081', 'Basic Privileges', '10', 'EXECUTE privilege', 'YES', NULL, ''),
('E091', 'Set functions', '', '', 'YES', NULL, ''),
('E091', 'Set functions', '01', 'AVG', 'YES', NULL, ''),
('E091', 'Set functions', '02', 'COUNT', 'YES', NULL, ''),
('E091', 'Set functions', '03', 'MAX', 'YES', NULL, ''),
('E091', 'Set functions', '04', 'MIN', 'YES', NULL, ''),
('E091', 'Set functions', '05', 'SUM', 'YES', NULL, ''),
('E091', 'Set functions', '06', 'ALL quantifier', 'YES', NULL, ''),
('E091', 'Set functions', '07', 'DISTINCT quantifier', 'YES', NULL, ''),
('E101', 'Basic data manipulation', '', '', 'YES', NULL, ''),
('E101', 'Basic data manipulation', '01', 'INSERT statement', 'YES', NULL, ''),
('E101', 'Basic data manipulation', '03', 'Searched UPDATE statement', 'YES', NULL, ''),
('E101', 'Basic data manipulation', '04', 'Searched DELETE statement', 'YES', NULL, ''),
('E111', 'Single row SELECT statement', '', '', 'YES', NULL, ''),
('E121', 'Basic cursor support', '', '', 'NO', NULL, 'yes via JDBC'),
('E121', 'Basic cursor support', '01', 'DECLARE CURSOR', 'NO', NULL, 'yes via JDBC'),
('E121', 'Basic cursor support', '02', 'ORDER BY columns need not be in select list', 'YES', NULL, ''),
('E121', 'Basic cursor support', '03', 'Value expressions in ORDER BY clause', 'YES', NULL, ''),
('E121', 'Basic cursor support', '04', 'OPEN statement', 'NO', NULL, 'yes via JDBC'),
('E121', 'Basic cursor support', '06', 'Positioned UPDATE statement', 'NO', NULL, 'yes via JDBC'),
('E121', 'Basic cursor support', '07', 'Positioned DELETE statement', 'NO', NULL, 'yes via JDBC'),
('E121', 'Basic cursor support', '08', 'CLOSE statement', 'NO', NULL, 'yes via JDBC'),
('E121', 'Basic cursor support', '10', 'FETCH statement implicit NEXT', 'NO', NULL, 'yes via JDBC'),
('E121', 'Basic cursor support', '17', 'WITH HOLD cursors', 'NO', NULL, 'yes via JDBC'),
('E131', 'Null value support (nulls in lieu of values)', '', '', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '', '', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '01', 'NOT NULL constraints', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '02', 'UNIQUE constraints of NOT NULL columns', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '03', 'PRIMARY KEY constraints', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '04', 'Basic FOREIGN KEY constraint with the NO ACTION default for both referential delete action and referential update action', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '06', 'CHECK constraints', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '07', 'Column defaults', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '08', 'NOT NULL inferred on PRIMARY KEY', 'YES', NULL, ''),
('E141', 'Basic integrity constraints', '10', 'Names in a foreign key can be specified in any order', 'YES', NULL, ''),
('E151', 'Transaction support', '', '', 'YES', NULL, ''),
('E151', 'Transaction support', '01', 'COMMIT statement', 'YES', NULL, ''),
('E151', 'Transaction support', '02', 'ROLLBACK statement', 'YES', NULL, ''),
('E152', 'Basic SET TRANSACTION statement', '', '', 'YES', NULL, ''),
('E152', 'Basic SET TRANSACTION statement', '01', 'SET TRANSACTION statement: ISOLATION LEVEL SERIALIZABLE clause', 'YES', NULL, ''),
('E152', 'Basic SET TRANSACTION statement', '02', 'SET TRANSACTION statement: READ ONLY and READ WRITE clauses', 'YES', NULL, ''),
('E153', 'Updatable queries with subqueries', '', '', 'YES', NULL, ''),
('E161', 'SQL comments using leading double minus', '', '', 'YES', NULL, ''),
('E171', 'SQLSTATE support', '', '', 'YES', NULL, ''),
('E182', 'Module language', '', '', 'YES', NULL, 'only schema contained routines'),
('F021', 'Basic information schema', '', '', 'YES', NULL, ''),
('F021', 'Basic information schema', '01', 'COLUMNS view', 'YES', NULL, ''),
('F021', 'Basic information schema', '02', 'TABLES view', 'YES', NULL, ''),
('T655', 'Cyclically dependent routines', '', '', 'NO', NULL, ''),
('F021', 'Basic information schema', '03', 'VIEWS view', 'YES', NULL, ''),
('F021', 'Basic information schema', '04', 'TABLE_CONSTRAINTS view', 'YES', NULL, ''),
('F021', 'Basic information schema', '05', 'REFERENTIAL_CONSTRAINTS view', 'YES', NULL, ''),
('F021', 'Basic information schema', '06', 'CHECK_CONSTRAINTS view', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '', '', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '01', 'CREATE TABLE statement to create persistent base tables', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '02', 'CREATE VIEW statement', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '03', 'GRANT statement', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '04', 'ALTER TABLE statement: ADD COLUMN clause', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '13', 'DROP TABLE statement: RESTRICT clause', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '16', 'DROP VIEW statement: RESTRICT clause', 'YES', NULL, ''),
('F031', 'Basic schema manipulation', '19', 'REVOKE statement: RESTRICT clause', 'YES', NULL, ''),
('F032', 'CASCADE drop behavior', '', '', 'YES', NULL, ''),
('F033', 'ALTER TABLE statement: DROP COLUMN clause', '', '', 'YES', NULL, ''),
('F034', 'Extended REVOKE statement', '', '', 'YES', NULL, ''),
('F034', 'Extended REVOKE statement', '01', 'REVOKE statement performed by other than the owner of a schema object', 'YES', NULL, ''),
('F034', 'Extended REVOKE statement', '02', 'REVOKE statement: GRANT OPTION FOR clause', 'YES', NULL, ''),
('F034', 'Extended REVOKE statement', '03', 'REVOKE statement to revoke a privilege that the grantee has WITH GRANT OPTION', 'YES', NULL, ''),
('F041', 'Basic joined table', '', '', 'YES', NULL, ''),
('F041', 'Basic joined table', '01', 'Inner join (but not necessarily the INNER keyword)', 'YES', NULL, ''),
('F041', 'Basic joined table', '02', 'INNER keyword', 'YES', NULL, ''),
('F041', 'Basic joined table', '03', 'LEFT OUTER JOIN', 'YES', NULL, ''),
('F041', 'Basic joined table', '04', 'RIGHT OUTER JOIN', 'YES', NULL, ''),
('F041', 'Basic joined table', '05', 'Outer joins can be nested', 'YES', NULL, ''),
('F041', 'Basic joined table', '07', 'The inner table in a left or right outer join can also be used in an inner join', 'YES', NULL, ''),
('F041', 'Basic joined table', '08', 'All comparison operators are supported (rather than just =)', 'YES', NULL, ''),
('F051', 'Basic date and time', '', '', 'YES', NULL, ''),
('F051', 'Basic date and time', '01', 'DATE data type (including support of DATE literal)', 'YES', NULL, ''),
('F051', 'Basic date and time', '02', 'TIME data type (including support of TIME literal) with fractional seconds precision of at least 0', 'YES', NULL, ''),
('F051', 'Basic date and time', '03', 'TIMESTAMP data type (including support of TIMESTAMP literal) with fractional seconds precision of at least 0 and 6', 'YES', NULL, ''),
('F051', 'Basic date and time', '04', 'Comparison predicate on DATE, TIME, and TIMESTAMP data types', 'YES', NULL, ''),
('F051', 'Basic date and time', '05', 'Explicit CAST between datetime types and character string types', 'YES', NULL, ''),
('F051', 'Basic date and time', '06', 'CURRENT_DATE', 'YES', NULL, ''),
('F051', 'Basic date and time', '07', 'LOCALTIME', 'YES', NULL, ''),
('F051', 'Basic date and time', '08', 'LOCALTIMESTAMP', 'YES', NULL, ''),
('F052', 'Intervals and datetime arithmetic', '', '', 'YES', NULL, ''),
('F053', 'OVERLAPS predicate', '', '', 'YES', NULL, ''),
('F081', 'UNION and EXCEPT in views', '', '', 'YES', NULL, ''),
('F111', 'Isolation levels other than SERIALIZABLE', '', '', 'YES', NULL, ''),
('F111', 'Isolation levels other than SERIALIZABLE', '01', 'READ UNCOMMITTED isolation level', 'YES', NULL, ''),
('F111', 'Isolation levels other than SERIALIZABLE', '02', 'READ COMMITTED isolation level', 'YES', NULL, ''),
('F111', 'Isolation levels other than SERIALIZABLE', '03', 'REPEATABLE READ isolation level', 'YES', NULL, ''),
('F121', 'Basic diagnostics management', '', '', 'NO', NULL, ''),
('F121', 'Basic diagnostics management', '01', 'GET DIAGNOSTICS statement', 'NO', NULL, ''),
('F121', 'Basic diagnostics management', '02', 'SET TRANSACTION statement: DIAGNOSTICS SIZE clause', 'NO', NULL, ''),
('F131', 'Grouped operations', '', '', 'YES', NULL, ''),
('F131', 'Grouped operations', '01', 'WHERE, GROUP BY, and HAVING clauses supported in queries with grouped views', 'YES', NULL, ''),
('F131', 'Grouped operations', '02', 'Multiple tables supported in queries with grouped views', 'YES', NULL, ''),
('F131', 'Grouped operations', '03', 'Set functions supported in queries with grouped views', 'YES', NULL, ''),
('F131', 'Grouped operations', '04', 'Subqueries with GROUP BY and HAVING clauses and grouped views', 'YES', NULL, ''),
('F131', 'Grouped operations', '05', 'Single row SELECT with GROUP BY and HAVING clauses and grouped views', 'YES', NULL, ''),
('F171', 'Multiple schemas per user', '', '', 'YES', NULL, ''),
('F181', 'Multiple module support', '', '', 'NO', NULL, ''),
('F191', 'Referential delete actions', '', '', 'YES', NULL, ''),
('F201', 'CAST function', '', '', 'YES', NULL, ''),
('F221', 'Explicit defaults', '', '', 'YES', NULL, ''),
('F222', 'INSERT statement: DEFAULT VALUES clause', '', '', 'YES', NULL, ''),
('F231', 'Privilege tables', '', '', 'YES', NULL, ''),
('F231', 'Privilege tables', '01', 'TABLE_PRIVILEGES view', 'YES', NULL, ''),
('F231', 'Privilege tables', '02', 'COLUMN_PRIVILEGES view', 'YES', NULL, ''),
('F231', 'Privilege tables', '03', 'USAGE_PRIVILEGES view', 'YES', NULL, ''),
('F251', 'Domain support', '', '', 'YES', NULL, ''),
('F261', 'CASE expression', '', '', 'YES', NULL, ''),
('F261', 'CASE expression', '01', 'Simple CASE', 'YES', NULL, ''),
('F261', 'CASE expression', '02', 'Searched CASE', 'YES', NULL, ''),
('F261', 'CASE expression', '03', 'NULLIF', 'YES', NULL, ''),
('F261', 'CASE expression', '04', 'COALESCE', 'YES', NULL, ''),
('F262', 'Extended CASE expression', '', '', 'YES', NULL, ''),
('F263', 'Comma-separated predicates in simple CASE expression', '', '', 'YES', NULL, ''),
('F271', 'Compound character literals', '', '', 'YES', NULL, ''),
('F281', 'LIKE enhancements', '', '', 'YES', NULL, ''),
('F291', 'UNIQUE predicate', '', '', 'YES', NULL, ''),
('F301', 'CORRESPONDING in query expressions', '', '', 'YES', NULL, ''),
('F302', 'INTERSECT table operator', '', '', 'YES', NULL, ''),
('F302', 'INTERSECT table operator', '01', 'INTERSECT DISTINCT table operator', 'YES', NULL, ''),
('F302', 'INTERSECT table operator', '02', 'INTERSECT ALL table operator', 'YES', NULL, ''),
('F304', 'EXCEPT ALL table operator', '', '', 'YES', NULL, ''),
('F311', 'Schema definition statement', '', '', 'YES', NULL, ''),
('F311', 'Schema definition statement', '01', 'CREATE SCHEMA', 'YES', NULL, ''),
('F311', 'Schema definition statement', '02', 'CREATE TABLE for persistent base tables', 'YES', NULL, ''),
('F311', 'Schema definition statement', '03', 'CREATE VIEW', 'YES', NULL, ''),
('F311', 'Schema definition statement', '04', 'CREATE VIEW: WITH CHECK OPTION', 'YES', NULL, ''),
('F311', 'Schema definition statement', '05', 'GRANT statement', 'YES', NULL, ''),
('F312', 'MERGE statement', '', '', 'YES', NULL, ''),
('F321', 'User authorization', '', '', 'YES', NULL, ''),
('F341', 'Usage tables', '', '', 'YES', NULL, ''),
('F361', 'Subprogram support', '', '', 'YES', NULL, ''),
('F381', 'Extended schema manipulation', '', '', 'YES', NULL, ''),
('F381', 'Extended schema manipulation', '01', 'ALTER TABLE statement: ALTER COLUMN clause', 'YES', NULL, ''),
('F381', 'Extended schema manipulation', '02', 'ALTER TABLE statement: ADD CONSTRAINT clause', 'YES', NULL, ''),
('F381', 'Extended schema manipulation', '03', 'ALTER TABLE statement: DROP CONSTRAINT clause', 'YES', NULL, ''),
('F391', 'Long identifiers', '', '', 'YES', NULL, ''),
('F392', 'Unicode escapes in identifiers', '', '', 'YES', NULL, ''),
('F393', 'Unicode escapes in literals', '', '', 'YES', NULL, ''),
('F401', 'Extended joined table', '', '', 'YES', NULL, ''),
('F401', 'Extended joined table', '01', 'NATURAL JOIN', 'YES', NULL, ''),
('F401', 'Extended joined table', '02', 'FULL OUTER JOIN', 'YES', NULL, ''),
('F401', 'Extended joined table', '04', 'CROSS JOIN', 'YES', NULL, ''),
('F402', 'Named column joins for LOBs, arrays, and multisets', '', '', 'YES', NULL, ''),
('F411', 'Time zone specification', '', '', 'YES', NULL, ''),
('F421', 'National character', '', '', 'YES', NULL, ''),
('F431', 'Read-only scrollable cursors', '', '', 'NO', NULL, 'yes via JDBC'),
('F431', 'Read-only scrollable cursors', '01', 'FETCH with explicit NEXT', 'NO', NULL, 'yes via JDBC'),
('F431', 'Read-only scrollable cursors', '02', 'FETCH FIRST', 'NO', NULL, 'yes via JDBC'),
('F431', 'Read-only scrollable cursors', '03', 'FETCH LAST', 'NO', NULL, 'yes via JDBC'),
('F431', 'Read-only scrollable cursors', '04', 'FETCH PRIOR', 'NO', NULL, 'yes via JDBC'),
('F431', 'Read-only scrollable cursors', '05', 'FETCH ABSOLUTE', 'NO', NULL, 'yes via JDBC'),
('F431', 'Read-only scrollable cursors', '06', 'FETCH RELATIVE', 'NO', NULL, 'yes via JDBC'),
('F441', 'Extended set function support', '', '', 'YES', NULL, ''),
('F442', 'Mixed column references in set functions', '', '', 'YES', NULL, ''),
('F451', 'Character set definition', '', '', 'YES', NULL, ''),
('F461', 'Named character sets', '', '', 'YES', NULL, ''),
('F471', 'Scalar subquery values', '', '', 'YES', NULL, ''),
('F481', 'Expanded NULL predicate', '', '', 'YES', NULL, ''),
('F491', 'Constraint management', '', '', 'YES', NULL, ''),
('F501', 'Features and conformance views', '', '', 'YES', NULL, ''),
('F501', 'Features and conformance views', '01', 'SQL_FEATURES view', 'YES', NULL, ''),
('F501', 'Features and conformance views', '02', 'SQL_SIZING view', 'YES', NULL, ''),
('F501', 'Features and conformance views', '03', 'SQL_LANGUAGES view', 'YES', NULL, ''),
('F502', 'Enhanced documentation tables', '', '', 'YES', NULL, ''),
('F502', 'Enhanced documentation tables', '01', 'SQL_SIZING_PROFILES view', 'YES', NULL, ''),
('F502', 'Enhanced documentation tables', '02', 'SQL_IMPLEMENTATION_INFO view', 'YES', NULL, ''),
('F502', 'Enhanced documentation tables', '03', 'SQL_PACKAGES view', 'YES', NULL, ''),
('F521', 'Assertions', '', '', 'NO', NULL, ''),
('F531', 'Temporary tables', '', '', 'YES', NULL, ''),
('F555', 'Enhanced seconds precision', '', '', 'YES', NULL, ''),
('F561', 'Full value expressions', '', '', 'YES', NULL, ''),
('F571', 'Truth value tests', '', '', 'YES', NULL, ''),
('F591', 'Derived tables', '', '', 'YES', NULL, ''),
('F611', 'Indicator data types', '', '', 'NO', NULL, ''),
('F641', 'Row and table constructors', '', '', 'YES', NULL, ''),
('F651', 'Catalog name qualifiers', '', '', 'YES', NULL, ''),
('F661', 'Simple tables', '', '', 'YES', NULL, ''),
('F671', 'Subqueries in CHECK', '', '', 'NO', NULL, ''),
('F672', 'Retrospective check constraints', '', '', 'YES', NULL, ''),
('F691', 'Collation and translation', '', '', 'NO', NULL, 'only one collation per database can be selected'),
('F692', 'Enhanced collation support', '', '', 'NO', NULL, ''),
('F693', 'SQL-session and client module collations', '', '', 'NO', NULL, ''),
('F695', 'Translation support', '', '', 'NO', NULL, ''),
('F696', 'Additional translation documentation', '', '', 'NO', NULL, ''),
('F701', 'Referential update actions', '', '', 'YES', NULL, ''),
('F711', 'ALTER domain', '', '', 'YES', NULL, ''),
('F721', 'Deferrable constraints', '', '', 'NO', NULL, ''),
('F731', 'INSERT column privileges', '', '', 'YES', NULL, ''),
('F741', 'Referential MATCH types', '', '', 'YES', NULL, 'MATCH FULL and MATCH SIMPLE supported but not MATCH PARTIAL'),
('F751', 'View CHECK enhancements', '', '', 'YES', NULL, ''),
('F761', 'Session management', '', '', 'NO', NULL, ''),
('F771', 'Connection management', '', '', 'NO', NULL, ''),
('F762', 'CURRENT_CATALOG', '','',  'YES', NULL, ''),
('F763', 'CURRENT_SCHEMA', '','',  'YES', NULL, ''),
('F781', 'Self-referencing operations', '', '', 'NO', NULL, ''),
('F791', 'Insensitive cursors', '', '', 'NO', NULL, 'yes via JDBC'),
('F801', 'Full set function', '', '', 'YES', NULL, ''),
('F811', 'Extended flagging', '', '', 'NO', NULL, ''),
('F812', 'Basic flagging', '', '', 'NO', NULL, ''),
('F813', 'Extended flagging', '', '', 'NO', NULL, ''),
('F821', 'Local table references', '', '', 'YES', NULL, ''),
('F831', 'Full cursor update', '', '', 'NO', NULL, 'yes via JDBC'),
('F831', 'Full cursor update', '01', 'Updatable scrollable cursors', 'NO', NULL, 'yes via JDBC'),
('F831', 'Full cursor update', '02', 'Updatable ordered cursors', 'NO', NULL, 'yes via JDBC'),
('F850', 'Top-level <order by clause> in <query expression>', '', '', 'YES', NULL, ''),
('F851', '<order by clause> in subqueries', '', '', 'YES', NULL, ''),
('F852', 'Top-level <order by clause> in views', '', '', 'YES', NULL, ''),
('F855', 'Nested <order by clause> in <query expression>', '', '', 'YES', NULL, ''),
('F856', 'Nested <fetch first clause> in <query expression>', '', '', 'YES', NULL, ''),
('F857', 'Top-level <fetch first clause> in <query expression>', '', '', 'YES', NULL, ''),
('F858', '<fetch first clause> in subqueries', '', '', 'YES', NULL, ''),
('F859', 'Top-level <fetch first clause> in views', '', '', 'YES', NULL, ''),
('J621', 'external Java routines', '', '', 'YES', NULL, ''),
('P001', 'Stored modules', '', '', 'NO', NULL, ''),
('P002', 'Computational completeness', '', '', 'YES', NULL, ''),
('P003', 'Information Schema views', '', '', 'YES', NULL, ''),
('P004', 'extended CASE statement', '', '', 'YES', NULL, ''),
('P006', 'Multiple assignment', '', '', 'YES', NULL, ''),
('P007', 'Enhanced diagnostics management', '', '', 'NO', NULL, ''),
('P008', 'Comma-separated predicates in simple CASE statement', '', '', 'YES', NULL, ''),
('S011', 'Distinct data types', '', '', 'YES', NULL, ''),
('S011', 'Distinct data types', '01', 'USER_DEFINED_TYPES view', 'YES', NULL, ''),
('S023', 'Basic structured types', '', '', 'NO', NULL, ''),
('S024', 'Enhanced structured types', '', '', 'NO', NULL, ''),
('S025', 'Final structured types', '', '', 'NO', NULL, ''),
('S026', 'Self-referencing structured types', '', '', 'NO', NULL, ''),
('S027', 'Create method by specific method name', '', '', 'NO', NULL, ''),
('S028', 'Permutable UDT options list', '', '', 'NO', NULL, ''),
('S041', 'Basic reference types', '', '', 'NO', NULL, ''),
('S043', 'Enhanced reference types', '', '', 'NO', NULL, ''),
('S051', 'Create table of type', '', '', 'NO', NULL, ''),
('S071', 'SQL paths in function and type name resolution', '', '', 'YES', NULL, ''),
('S081', 'Subtables', '', '', 'NO', NULL, ''),
('S091', 'Basic array support', '', '', 'YES', NULL, ''),
('S091', 'Basic array support', '01', 'Arrays of built-in data types', 'YES', NULL, ''),
('S091', 'Basic array support', '02', 'Arrays of distinct types', 'YES', NULL, ''),
('S091', 'Basic array support', '03', 'Array expressions', 'YES', NULL, ''),
('S092', 'Arrays of user-defined types', '', '', 'NO', NULL, ''),
('S094', 'Arrays of reference types', '', '', 'NO', NULL, ''),
('S095', 'Array constructors by query', '', '', 'YES', NULL, ''),
('S096', 'Optional array bounds', '', '', 'YES', NULL, ''),
('S097', 'Array element assignment', '', '', 'YES', NULL, ''),
('S111', 'ONLY in query expressions', '', '', 'NO', NULL, ''),
('S151', 'Type predicate', '', '', 'NO', NULL, ''),
('S161', 'Subtype treatment', '', '', 'NO', NULL, ''),
('S162', 'Subtype treatment for references', '', '', 'NO', NULL, ''),
('S201', 'SQL-invoked routines on arrays', '', '', 'YES', NULL, ''),
('S201', 'SQL-invoked routines on arrays', '01', 'Array parameters', 'YES', NULL, ''),
('S201', 'SQL-invoked routines on arrays', '02', 'Array as result type of functions', 'YES', NULL, ''),
('S202', 'SQL-invoked routines on multisets', '', '', 'NO', NULL, ''),
('S211', 'User-defined cast functions', '', '', 'NO', NULL, ''),
('S231', 'Structured type locators', '', '', 'NO', NULL, ''),
('S232', 'Array locators', '', '', 'NO', NULL, ''),
('S233', 'Multiset locators', '', '', 'NO', NULL, ''),
('S241', 'Transform functions', '', '', 'NO', NULL, ''),
('S242', 'Alter transform statement', '', '', 'NO', NULL, ''),
('S251', 'User-defined orderings', '', '', 'NO', NULL, ''),
('S261', 'Specific type method', '', '', 'NO', NULL, ''),
('S271', 'Basic multiset support', '', '', 'NO', NULL, ''),
('S272', 'Multisets of user-defined types', '', '', 'NO', NULL, ''),
('S274', 'Multisets of reference types', '', '', 'NO', NULL, ''),
('S275', 'Advanced multiset support', '', '', 'NO', NULL, ''),
('S281', 'Nested collection types', '', '', 'NO', NULL, ''),
('S291', 'Unique constraint on entire row', '', '', 'NO', NULL, ''),
('T011', 'Timestamp in Information Schema', '', '', 'YES', NULL, ''),
('T021', 'BINARY and VARBINARY data types', '', '', 'YES', NULL, ''),
('T022', 'Advanced BINARY and VARBINARY data type support', '', '', 'YES', NULL, ''),
('T023', 'Compound binary literals', '', '', 'YES', NULL, ''),
('T024', 'Spaces in binary literals', '', '', 'YES', NULL, ''),
('T031', 'BOOLEAN data type', '', '', 'YES', NULL, ''),
('T041', 'Basic LOB data type support', '', '', 'YES', NULL, ''),
('T041', 'Basic LOB data type support', '01', 'BLOB data type', 'YES', NULL, ''),
('T041', 'Basic LOB data type support', '02', 'CLOB data type', 'YES', NULL, ''),
('T041', 'Basic LOB data type support', '03', 'POSITION, LENGTH, LOWER, TRIM, UPPER, and SUBSTRING functions for LOB data types', 'YES', NULL, ''),
('T041', 'Basic LOB data type support', '04', 'Concatenation of LOB data types', 'YES', NULL, ''),
('T041', 'Basic LOB data type support', '05', 'LOB locator: non-holdable', 'YES', NULL, 'yes via JDBC'),
('T042', 'Extended LOB data type support', '', '', 'YES', NULL, ''),
('T051', 'Row types', '', '', 'NO', NULL, ''),
('T052', 'MAX and MIN for row types', '', '', 'NO', NULL, ''),
('T053', 'Explicit aliases for all-fields reference', '', '', 'NO', NULL, ''),
('T061', 'UCS support', '', '', 'YES', NULL, ''),
('T071', 'BIGINT data type', '', '', 'YES', NULL, ''),
('T111', 'Updatable joins, unions, and columns', '', '', 'NO', NULL, 'only updatable columns'),
('T121', 'WITH (excluding RECURSIVE) in query expression', '', '', 'YES', NULL, ''),
('T122', 'WITH (excluding RECURSIVE) in subquery', '', '', 'YES', NULL, ''),
('T131', 'Recursive query', '', '', 'NO', NULL, ''),
('T132', 'Recursive query in subquery', '', '', 'NO', NULL, ''),
('T141', 'SIMILAR predicate', '', '', 'NO', NULL, ''),
('T151', 'DISTINCT predicate', '', '', 'YES', NULL, ''),
('T152', 'DISTINCT predicate with negation', '', '', 'YES', NULL, ''),
('T171', 'LIKE clause in table definition', '', '', 'YES', NULL, ''),
('T172', 'AS subquery clause in table definition', '', '', 'YES', NULL, ''),
('T173', 'Extended LIKE clause in table definition', '', '', 'YES', NULL, ''),
('T174', 'Identity columns', '', '', 'YES', NULL, ''),
('T175', 'Generated columns', '', '', 'YES', NULL, ''),
('T176', 'Sequence generator support', '', '', 'YES', NULL, ''),
('T191', 'Referential action RESTRICT', '', '', 'YES', NULL, ''),
('T201', 'Comparable data types for referential constraints', '', '', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '', '', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '01', 'Triggers activated on UPDATE, INSERT, or DELETE of one base table', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '02', 'BEFORE triggers', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '03', 'AFTER triggers', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '04', 'FOR EACH ROW triggers', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '05', 'Ability to specify a search condition that must be true before the trigger is invoked', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '06', 'Support for run-time rules for the interaction of triggers and constraints', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '07', 'TRIGGER privilege', 'YES', NULL, ''),
('T211', 'Basic trigger capability', '08', 'Multiple triggers for the same event are executed in the order in which they were created in the catalog', 'YES', NULL, ''),
('T212', 'Enhanced trigger capability', '', '', 'YES', NULL, ''),
('T231', 'Sensitive cursors', '', '', 'YES', NULL, ''),
('T241', 'START TRANSACTION statement', '', '', 'YES', NULL, ''),
('T251', 'SET TRANSACTION statement: LOCAL option', '', '', 'NO', NULL, ''),
('T261', 'Chained transactions', '', '', 'YES', NULL, ''),
('T271', 'Savepoints', '', '', 'YES', NULL, ''),
('T272', 'Enhanced savepoint management', '', '', 'YES', NULL, ''),
('T281', 'SELECT privilege with column granularity', '', '', 'YES', NULL, ''),
('T285', 'Enhanced derived column names', '', '', 'YES', NULL, ''),
('T301', 'Functional dependencies', '', '', 'YES', NULL, ''),
('T312', 'OVERLAY function', '', '', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '', '', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '01', 'User-defined functions with no overloading', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '02', 'User-defined stored procedures with no overloading', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '03', 'Function invocation', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '04', 'CALL statement', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '05', 'RETURN statement', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '06', 'ROUTINES view', 'YES', NULL, ''),
('T321', 'Basic SQL-invoked routines', '07', 'PARAMETERS view', 'YES', NULL, ''),
('T322', 'Overloading of SQL-invoked functions and procedures', '', '', 'YES', NULL, ''),
('T323', 'Explicit security for external routines', '', '', 'NO', NULL, ''),
('T324', 'Explicit security for SQL routines', '', '', 'YES', NULL, 'only DEFINER'),
('T325', 'Qualified SQL parameter references', '', '', 'NO', NULL, ''),
('T326', 'Table functions', '', '', 'YES', NULL, ''),
('T331', 'Basic roles', '', '', 'YES', NULL, ''),
('T332', 'Extended roles', '', '', 'NO', NULL, ''),
('T351', 'Bracketed SQL comments (/*...*/ comments)', '', '', 'YES', NULL, ''),
('T401', 'INSERT into a cursor', '', '', 'NO', NULL, 'yes via JDBC'),
('T411', 'UPDATE statement: SET ROW option', '', '', 'NO', NULL, ''),
('T431', 'Extended grouping capabilities', '', '', 'NO', NULL, ''),
('T432', 'Nested and concatenated GROUPING SETS', '', '', 'NO', NULL, ''),
('T433', 'Multiargument GROUPING function', '', '', 'NO', NULL, ''),
('T434', 'GROUP BY DISINCT', '', '', 'NO', NULL, ''),
('T441', 'ABS and MOD functions', '', '', 'YES', NULL, ''),
('T461', 'Symmetric BETWEEN predicate', '', '', 'YES', NULL, ''),
('T471', 'Result sets return value', '', '', 'YES', NULL, ''),
('T491', 'LATERAL derived table', '', '', 'YES', NULL, ''),
('T501', 'Enhanced EXISTS predicate', '', '', 'YES', NULL, ''),
('T511', 'Transaction counts', '', '', 'NO', NULL, ''),
('T541', 'Updatable table references', '', '', 'NO', NULL, ''),
('T551', 'Optional key words for default syntax', '', '', 'YES', NULL, ''),
('T561', 'Holdable locators', '', '', 'NO', NULL, ''),
('T571', 'Array-returning external SQL-invoked functions', '', '', 'YES', NULL, ''),
('T572', 'Multiset-returning external SQL-invoked functions', '', '', 'NO', NULL, ''),
('T581', 'Regular expression substring function', '', '', 'YES', NULL, ''),
('T591', 'UNIQUE constraints of possibly null columns', '', '', 'YES', NULL, ''),
('T601', 'Local cursor references', '', '', 'NO', NULL, ''),
('T611', 'Elementary OLAP operations', '', '', 'NO', NULL, ''),
('T612', 'Advanced OLAP operations', '', '', 'NO', NULL, ''),
('T613', 'Sampling', '', '', 'NO', NULL, ''),
('T621', 'Enhanced numeric functions', '', '', 'NO', NULL, ''),
('T631', 'IN predicate with one list element', '', '', 'YES', NULL, ''),
('T641', 'Multiple column assignment', '', '', 'YES', NULL, ''),
('T651', 'SQL-schema statements in SQL routines', '', '', 'NO', NULL, ''),
('T652', 'SQL-dynamic statements in SQL routines', '', '', 'NO', NULL, ''),
('T653', 'SQL-schema statements in external routines', '', '', 'NO', NULL, ''),
('T654', 'SQL-dynamic statements in external routines', '', '', 'YES', NULL, '');
/*sql_packages*/
VALUES
( 'PKG001', 'Enhanced datetime facilities','YES', CAST(NULL AS CHARACTER), '' ),
( 'PKG002', 'Enhanced integrity management','YES', NULL, '' ),
( 'PKG004', 'PSM', 'YES', NULL, 'only schema contained routines' ),
( 'PKG006', 'Basic object support', 'NO', NULL, '' ),
( 'PKG007', 'Enhanced object support','NO', NULL, '' ),
( 'PKG008', 'Active database', 'YES', NULL, '' ),
( 'PKG010', 'OLAP', 'NO', NULL, '');
/*sql_parts*/
VALUES
( 'ISO9075-1', 'Framework','YES', CAST(NULL AS CHARACTER), '' ),
( 'ISO9075-2', 'Foundation','YES', NULL, '' ),
( 'ISO9075-3', 'Call-level interface','YES', NULL, '' ),
( 'ISO9075-4', 'Persistent Stored Modules', 'YES', NULL, '' ),
( 'ISO9075-9', 'Management of External Data', 'NO', NULL, '' ),
( 'ISO9075-10', 'Object Language Bindings,','NO', NULL, '' ),
( 'ISO9075-11', 'Information and Definition Schemas', 'YES', NULL, '' ),
( 'ISO9075-13', 'Routines & Types Using the Java Programming', 'YES', NULL, ''),
( 'ISO9075-14', 'XML-Related Specifications', 'NO', NULL, '');
/*sql_sizing*/
VALUES
( 34, 'MAXIMUM CATALOG NAME LENGTH', 128,'length in characters' ),
( 30, 'MAXIMUM COLUMN NAME LENGTH',128, NULL ),
( 97, 'MAXIMUM COLUMNS IN GROUP BY', 0, 'limited by memory only' ),
( 99, 'MAXIMUM COLUMNS IN ORDER BY', 0, 'limited by memory only' ),
( 100, 'MAXIMUM COLUMNS IN SELECT', 0,'limited by memory only' ),
( 101, 'MAXIMUM COLUMNS IN TABLE', 0, 'limited by memory only' ),
( 1, 'MAXIMUM CONCURRENT ACTIVITIES', 0, 'limited by memory only'),
( 31, 'MAXIMUM CURSOR NAME LENGTH', 128, NULL),
( 0, 'MAXIMUM DRIVER CONNECTIONS', 0, 'limited by memory only' ),
( 10005, 'MAXIMUM IDENTIFIER LENGTH', 128, NULL),
( 32, 'MAXIMUM SCHEMA NAME LENGTH', 128, NULL),
( 20000, 'MAXIMUM STATEMENT OCTETS', 0, 'limited by memory only'),
( 20001, 'MAXIMUM STATEMENT OCTETS DATA', 0, 'limited by memory only'),
( 20002, 'MAXIMUM STATEMENT OCTETS SCHEMA',0, 'limited by memory only'),
( 35, 'MAXIMUM TABLE NAME LENGTH', 128, NULL),
( 106, 'MAXIMUM TABLES IN SELECT', 0, 'limited by memory only'),
( 107, 'MAXIMUM USER NAME LENGTH', 128, NULL ),
( 25000, 'MAXIMUM CURRENT DEFAULT TRANSFORM GROUP LENGTH', NULL, NULL),
( 25001, 'MAXIMUM CURRENT TRANSFORM GROUP LENGTH',NULL, NULL),
( 25002, 'MAXIMUM CURRENT PATH LENGTH', NULL, NULL),
( 25003, 'MAXIMUM CURRENT ROLE LENGTH', 128, NULL),
( 25004, 'MAXIMUM SESSION USER LENGTH', 128, NULL),
( 25005, 'MAXIMUM SYSTEM USER LENGTH', 128, NULL);

