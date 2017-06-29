package com.exercises.hotelbooking.database;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.springframework.data.cassandra.core.CassandraTemplate;

import java.io.IOException;
import java.util.Properties;

import static org.springframework.cassandra.core.keyspace.DropKeyspaceSpecification.dropKeyspace;

public class DatabaseDestructor {

    public static void main(String[] args) throws Exception {
        Properties applicationProperties = new Properties();
        applicationProperties.load(DatabaseInitializer.class.getClassLoader()
                .getResourceAsStream("application.properties"));
        final String keyspaceName = applicationProperties.getProperty("spring.data.cassandra.keyspace-name");

        destroy(keyspaceName);
    }

    public static void destroy(String keyspaceName) throws IOException {
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
            cassandraTemplate.execute(dropKeyspace(keyspaceName).ifExists());
        } finally {
            if (cluster != null) cluster.close();
        }
    }
}
