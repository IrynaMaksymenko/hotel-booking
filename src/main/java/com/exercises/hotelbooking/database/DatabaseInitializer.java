package com.exercises.hotelbooking.database;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Session;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.Properties;

import static org.springframework.cassandra.core.keyspace.CreateIndexSpecification.createIndex;
import static org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification.createKeyspace;
import static org.springframework.cassandra.core.keyspace.CreateTableSpecification.createTable;

public class DatabaseInitializer {

    public static void main(String[] args) throws Exception {

        Properties applicationProperties = new Properties();
        applicationProperties.load(DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("application.properties"));
        final Integer port = Integer.valueOf(applicationProperties.getProperty("spring.data.cassandra.port"));
        final String contactPoint = applicationProperties.getProperty("spring.data.cassandra.contact-points");
        final String keyspaceName = applicationProperties.getProperty("spring.data.cassandra.keyspace-name");

        Cluster cluster = null;
        try {
            cluster = Cluster.builder()
                    .withPort(port)
                    .addContactPoint(contactPoint)
                    .build();
            final Session session = cluster.connect();
            final CassandraTemplate cassandraTemplate = new CassandraTemplate(session);
            createDatabaseIfNotExist(cassandraTemplate, keyspaceName);
        } finally {
            if (cluster != null) cluster.close();
        }
    }

    private static void createDatabaseIfNotExist(CassandraTemplate cassandraTemplate, String keyspaceName) {
        cassandraTemplate.execute(createKeyspace(keyspaceName).ifNotExists());
        cassandraTemplate.execute("USE " + keyspaceName);

        cassandraTemplate.execute(createTable("hotels").ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .column("city", DataType.text())
                .column("name", DataType.text())
                .column("added", DataType.timestamp())
        );
        cassandraTemplate.execute(createIndex("hotels_city").ifNotExists().tableName("hotels").columnName("city"));

        cassandraTemplate.execute(createTable("rooms").ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .column("hotelId", DataType.uuid())
                .column("roomNumber", DataType.cint())
                .column("roomClass", DataType.text())
        );
        cassandraTemplate.execute(createIndex("rooms_hotel").ifNotExists().tableName("rooms").columnName("hotelId"));
        cassandraTemplate.execute(createIndex("rooms_class").ifNotExists().tableName("rooms").columnName("roomClass"));

        cassandraTemplate.execute(createTable("guests").ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .column("firstName", DataType.text())
                .column("lastName", DataType.text())
                .column("phoneNumber", DataType.text())
                .column("email", DataType.text())
        );

        cassandraTemplate.execute(createTable("bookings").ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .column("roomId", DataType.text())
                .column("guestId", DataType.text())
                .column("start", DataType.timestamp())
                .column("end", DataType.timestamp())
        );
        cassandraTemplate.execute(createIndex("bookings_guest").ifNotExists().tableName("bookings").columnName("guestId"));
        cassandraTemplate.execute(createIndex("bookings_start").ifNotExists().tableName("bookings").columnName("start"));
        cassandraTemplate.execute(createIndex("bookings_end").ifNotExists().tableName("bookings").columnName("end"));
    }
}
