package com.exercises.hotelbooking.database.models;

import lombok.*;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Table("bookings_by_guest")
public class BookingByGuest {

    @PrimaryKey
    private @Getter @Setter BookingByGuestKey key;

    private @Getter @Setter Long end;

    @Column("room_id")
    private @Getter @Setter UUID roomId;

    @Column("hotel_id")
    private @Getter @Setter UUID hotelId;

    @AllArgsConstructor
    @EqualsAndHashCode
    @PrimaryKeyClass
    public static class BookingByGuestKey implements Serializable {

        @PrimaryKeyColumn(name = "guest_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private @Getter @Setter UUID guestId;

        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private @Getter @Setter Long start;

        @PrimaryKeyColumn(name = "booking_id", ordinal = 3, type = PrimaryKeyType.CLUSTERED)
        private @Getter @Setter UUID bookingId;

    }

}
