<h1 align="center"> health-center </h1> <br>

## Table of Contents

- [Introduction](#introduction)
- [Technologies](#technologies)
- [Requirements](#requirements)
- [Quick Start](#quick-start)
- [Contact](#contact)



## Introduction
This application serves to manage schedules in health centre.
App contains two microservices:
  - booking-system-app - base application to create/update/delete patients, doctors and schedules
  - confirmation-boot - get booked schedules from booking-system-app via Kafka and send confirmation to patient via email



## Technologies
- Java & Spring Boot
- Kafka - for asynchronus communication between microservices
- Spring Security and Keycloak - to authenticate patients and doctors
- MySQL + Hibernate - managment of database
- Docker - containerization for easy deployment


## Requirements
The application runs in a docker container.

### Docker
* [Docker](https://www.docker.com/get-docker)


## Quick Start
Depends of project is needed:
1. Use project path: "cd booking-system-app" or "cd confirmation-boot"
2. Generate .jar file using command "mvn clean package -DskipTests" 
3. When it is done for both projects then switch path to "health_center" using "cd health_center"
4. Start docker-compose with command "docker-compose up --build -d"
5. Keycloak import should happen automatically. In case when it doesn't work then please:
    - open keycloak:8080
    - log in :
      * username: admin
      * password: admin
    - create new Realm and the use function to import backup file - file you can find in booking-system-app/keyloack-backup/export, instruction:
            https://medium.com/@ramanamuttana/export-and-import-of-realm-from-keycloak-131deb118b72
    - import users:
            https://docs.expertflow.com/cx/4.6/admin-guide-for-bulk-user-upload-to-keycloak
6. Save settings
7. Keycloak is ready to authenticate
8. Test application using swagger ui: http://localhost:8081/swagger-ui/index.html#

## Contact

Szymon Sztukowski szsztukowski1@gmail.com

https://github.com/szysztu/health-center

