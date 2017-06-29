# hotel-booking

Spring Boot application that allow to manage hotel bookings.

To create keyspace and all required tables execute 
`mvn clean install -Pcreate-db`.

To drop keyspace with all tables execute 
`mvn clean install -Pdrop-db`.

To launch application execute 
`mvn spring-boot:run`.

To run integration test following steps of simple application workflow execute
`mvn clean install -Pintegration-test`