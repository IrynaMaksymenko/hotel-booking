package com.exercises.hotelbooking.database.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

@AllArgsConstructor
@Table("hotels")
public class Hotel {

    @PrimaryKey(value = "hotel_id")
    private @Getter @Setter UUID id;

    private @Getter @Setter String name;

    private @Getter @Setter String city;

    private @Getter @Setter String address;

    private @Getter @Setter int rating;

}
