package com.exercises.hotelbooking.database.models;

import lombok.*;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Table("rooms")
public class Room {

    @PrimaryKey
    private @Getter @Setter RoomKey key;

    @Column("room_type")
    private @Getter @Setter String roomType;

    private @Getter @Setter List<String> facilities;

    @AllArgsConstructor
    @EqualsAndHashCode
    @PrimaryKeyClass
    public static class RoomKey implements Serializable {

        @PrimaryKeyColumn(name = "hotel_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private @Getter @Setter UUID hotelId;

        @PrimaryKeyColumn(name = "room_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        private @Getter @Setter UUID roomId;

    }

}
