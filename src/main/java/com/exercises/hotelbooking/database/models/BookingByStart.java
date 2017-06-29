package com.exercises.hotelbooking.database.models;

import lombok.*;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Table("bookings_by_start")
public class BookingByStart {

    @PrimaryKey
    private @Getter @Setter BookingByStartKey key;

    private @Getter @Setter Long end;

    @Column("room_id")
    private @Getter @Setter UUID roomId;

    @Column("guest_id")
    private @Getter @Setter UUID guestId;

    @AllArgsConstructor
    @EqualsAndHashCode
    @PrimaryKeyClass
    public static class BookingByStartKey implements Serializable {

        @PrimaryKeyColumn(name = "hotel_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private @Getter @Setter UUID hotelId;

        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private @Getter @Setter Long start;

        @PrimaryKeyColumn(name = "booking_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        private @Getter @Setter UUID bookingId;

    }

}
