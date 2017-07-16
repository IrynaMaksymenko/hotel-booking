package com.exercises.hotelbooking.database;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.Session;
import org.springframework.cassandra.core.Ordering;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.util.Properties;

import static org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification.createKeyspace;
import static org.springframework.cassandra.core.keyspace.CreateTableSpecification.createTable;

public class DatabaseInitializer {

    public static void main(String[] args) throws Exception {

        Properties applicationProperties = new Properties();
        applicationProperties.load(DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("application.properties"));
        final String keyspaceName = applicationProperties.getProperty("spring.data.cassandra.keyspace-name");

        initialize(keyspaceName);
    }

    public static void initialize(String keyspaceName) throws Exception {
        Properties applicationProperties = new Properties();
        applicationProperties.load(DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("application.properties"));
        final Integer port = Integer.valueOf(applicationProperties.getProperty("spring.data.cassandra.port"));
        final String contactPoint = applicationProperties.getProperty("spring.data.cassandra.contact-points");

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
                .partitionKeyColumn("hotel_id", DataType.uuid())
                .column("city", DataType.text())
                .column("name", DataType.text())
                .column("address", DataType.text())
                .column("rating", DataType.cint())
        );

        cassandraTemplate.execute(createTable("hotels_by_city").ifNotExists()
                .partitionKeyColumn("city", DataType.text())
                .clusteredKeyColumn("hotel_id", DataType.uuid())
                .column("name", DataType.text())
                .column("address", DataType.text())
                .column("rating", DataType.cint())
        );

        cassandraTemplate.execute(createTable("rooms").ifNotExists()
                .partitionKeyColumn("hotel_id", DataType.uuid())
                .clusteredKeyColumn("room_id", DataType.uuid())
                .column("room_type", DataType.text())
                .column("facilities", DataType.list(DataType.text()))
        );

        cassandraTemplate.execute(createTable("guests").ifNotExists()
                .partitionKeyColumn("email", DataType.text())
                .clusteredKeyColumn("guest_id", DataType.uuid())
                .column("first_name", DataType.text())
                .column("last_name", DataType.text())
                .column("phone_number", DataType.text())
                .column("password_hash", DataType.text())
        );

        cassandraTemplate.execute(createTable("bookings_by_guest").ifNotExists()
                .partitionKeyColumn("guest_id", DataType.uuid())
                .clusteredKeyColumn("start", DataType.bigint(), Ordering.DESCENDING)
                .clusteredKeyColumn("booking_id", DataType.uuid())
                .column("end", DataType.bigint())
                .column("hotel_id", DataType.uuid())
                .column("room_id", DataType.uuid())
        );

        cassandraTemplate.execute(createTable("bookings_by_start").ifNotExists()
                .partitionKeyColumn("hotel_id", DataType.uuid())
                .clusteredKeyColumn("start", DataType.bigint(), Ordering.DESCENDING)
                .clusteredKeyColumn("booking_id", DataType.uuid())
                .column("end", DataType.bigint())
                .column("guest_id", DataType.uuid())
                .column("room_id", DataType.uuid())
        );

        cassandraTemplate.execute(createTable("bookings_by_end").ifNotExists()
                .partitionKeyColumn("hotel_id", DataType.uuid())
                .clusteredKeyColumn("end", DataType.bigint(), Ordering.DESCENDING)
                .clusteredKeyColumn("booking_id", DataType.uuid())
                .column("start", DataType.bigint())
                .column("guest_id", DataType.uuid())
                .column("room_id", DataType.uuid())
        );
    }
}
