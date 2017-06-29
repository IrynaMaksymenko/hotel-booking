package com.exercises.hotelbooking.database.models;

import lombok.*;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.mapping.Table;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Table("hotels_by_city")
public class HotelByCity {

    @PrimaryKey
    private @Getter @Setter HotelByCityKey key;

    private @Getter @Setter String name;

    private @Getter @Setter String address;

    private @Getter @Setter int rating;

    @AllArgsConstructor
    @EqualsAndHashCode
    @PrimaryKeyClass
    public static class HotelByCityKey implements Serializable {

        @PrimaryKeyColumn(name = "city", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private @Getter @Setter String city;

        @PrimaryKeyColumn(name = "hotel_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        private @Getter @Setter UUID id;

    }

}
