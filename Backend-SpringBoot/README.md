# Backend Setup for MarineTracker

This setup explains how to run the Backend of the MarineTracker application. 

## Prerequisites

The backend requires both Kafka and PostgreSQL to be running. Please follow the setup instructions for each:
- Kafka: [Kafka Setup README](../Kafka/README.md)
- PostgreSQL: Setup and Start via Docker


## How to Run

### 1. Start Kafka Services

First, ensure Kafka is running and Producer is publishing vessel messages to topic, 
by following the instructions in the [Kafka Setup README](../Kafka/README.md).

### 2. Set Up and Start PostgreSQL Database via Docker

```bash
# Pull PostgreSQL+PostGIS Docker image
docker pull postgis/postgis:17-3.4

# Run PostgreSQL container
docker run --name postgres_container -e POSTGRES_PASSWORD=mysecretpassword -e POSTGRES_USER=postgres -e POSTGRES_DB=postgres -d -p 5432:5432 postgis/postgis:17-3.4

# If container exists but is stopped, start it
docker start postgres_container
```

### 3. Visualize PostgreSQL Database with pgAdmin

For better database management, it's recommended to use pgAdmin 4:

1. Download and install [pgAdmin 4 Desktop](https://www.pgadmin.org/download/)
2. Add a new server in pgAdmin:
    - Right-click Servers > Create > Server
    - General tab:
        - Name: Docker Postgres (or any name)
    - Connection tab:
        - Host name/address: localhost
        - Port: 5432
        - Maintenance DB: postgres
        - Username: postgres
        - Password: mysecretpassword
    - Check "Save Password"
    - Click Save

You should now see a connection to your PostgreSQL database, 
and be able to view the schema/tables of it as well as run SQL queries on it for testing.

### 4. Configure PostgreSQL Database on Spring Boot application.properties

Ensure your `application.properties` file contains:

```properties
#...
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=mysecretpassword
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
#...
```

### 5. Run the Spring Boot Application

Right click on [BackendSpringBootApplication.java](./src/main/java/com/di/marinetracker/backendspringboot/BackendSpringBootApplication.java)
and click `Run`.


## Check if Online

Once the application is running, you can access the backend API at:
https://localhost:8443/


## Notes

- The `spring.jpa.hibernate.ddl-auto=create-drop` setting will recreate the database schema on each startup. May change to `update` in case of deployment.
- For deployment use, consider setting up database persistence using Docker volumes.
