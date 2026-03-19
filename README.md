# SimpleShop Backend

This is the backend service for SimpleShop, built with Spring Boot and an Oracle Database.

## Prerequisites

Before you begin, ensure you have the following installed on your system:
- **Java Development Kit (JDK)** (Version 23 was used to run this project)
- **Apache Maven** (or you can use the included Maven wrapper if available)
- **Oracle Database XE** (18c, 21c, or similar) running locally on port `1521`

## Database Setup

This application connects to an Oracle Pluggable Database (PDB) named `xepdb1`. You need to ensure the database is running, the PDB is open, and the application user exists.

1. **Connect to your Oracle Database** as `SYSDBA` using SQL*Plus or a similar tool:
   ```bash
   sqlplus sys as sysdba
   ```

2. **Open the Pluggable Database (PDB)** (if it's not already open):
   ```sql
   ALTER PLUGGABLE DATABASE xepdb1 OPEN;
   ```
   *(Note: The `ORA-12514` error often occurs when the listener is running, but the specific PDB like `xepdb1` is not open and hasn't registered its service).*

3. **Create the Database User**:
   Switch your session to the PDB and create the application user:
   ```sql
   ALTER SESSION SET CONTAINER = xepdb1;
   
   CREATE USER shop_user IDENTIFIED BY shop123;
   GRANT CONNECT, RESOURCE TO shop_user;
   ALTER USER shop_user QUOTA UNLIMITED ON USERS;
   ```

## Application Configuration

The default configuration is located in `src/main/resources/application.properties`. 

By default, the application expects the following database setup:
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/xepdb1
spring.datasource.username=shop_user
spring.datasource.password=shop123
```
If your Oracle database uses a different port, service name, or credentials, make sure to update these properties.

The server is configured to run on port **8087**.

## Running the Application

1. Open a terminal and navigate to the root directory of the project (`ishop-backend`).
2. Run the application using Maven:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

The application will start and the Tomcat server will initialize on `http://localhost:8087`.

## API Documentation

This project uses SpringDoc OpenAPI to automatically generate API documentation. Once the application is running, you can interact with the API endpoints via:

- **Swagger UI:** [http://localhost:8087/swagger-ui.html](http://localhost:8087/swagger-ui.html)
- **OpenAPI JSON:** [http://localhost:8087/v3/api-docs](http://localhost:8087/v3/api-docs)

## Troubleshooting

- **ORA-12514: TNS:listener does not currently know of service requested...**: 
  This means your Oracle Listener is up, but the `xepdb1` service is not available. Ensure that your Oracle Database is fully started and the `xepdb1` pluggable database is open. You may need to run `ALTER PLUGGABLE DATABASE xepdb1 OPEN;` as SYSDBA.
- **Port 8087 is already in use**: 
  If another process is using port 8087, change the `server.port` property in `application.properties` to an available port.
