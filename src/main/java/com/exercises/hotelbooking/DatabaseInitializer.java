package com.exercises.hotelbooking;

import com.datastax.driver.core.DataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Component;

import static org.springframework.cassandra.core.keyspace.CreateIndexSpecification.createIndex;
import static org.springframework.cassandra.core.keyspace.CreateKeyspaceSpecification.createKeyspace;
import static org.springframework.cassandra.core.keyspace.CreateTableSpecification.createTable;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    @Value("${keyspace.name}")
    private String keyspaceName;

    @Autowired
    private CassandraTemplate cassandraTemplate;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        cassandraTemplate.execute(createKeyspace(keyspaceName).ifNotExists());
        cassandraTemplate.execute("USE " + keyspaceName);
        cassandraTemplate.execute(createTable("hotels").ifNotExists()
                .partitionKeyColumn("id", DataType.uuid())
                .column("city", DataType.text())
                .column("name", DataType.text())
                .column("added", DataType.timestamp())
        );
        cassandraTemplate.execute(createIndex("hotels_city").ifNotExists().tableName("hotels").columnName("city"));
    }
}
