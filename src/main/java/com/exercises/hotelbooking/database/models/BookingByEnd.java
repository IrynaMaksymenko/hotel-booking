package com.exercises.hotelbooking.database.models;

import lombok.*;
import org.springframework.cassandra.core.Ordering;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Table("bookings_by_end")
public class BookingByEnd {

    @PrimaryKey
    private @Getter @Setter BookingByEndKey key;

    private @Getter @Setter Long start;

    @Column("room_id")
    private @Getter @Setter UUID roomId;

    @Column("guest_id")
    private @Getter @Setter UUID guestId;

    @AllArgsConstructor
    @EqualsAndHashCode
    @PrimaryKeyClass
    public static class BookingByEndKey implements Serializable {

        @PrimaryKeyColumn(name = "hotel_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private @Getter @Setter UUID hotelId;

        @PrimaryKeyColumn(ordinal = 1, type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
        private @Getter @Setter Long end;

        @PrimaryKeyColumn(name = "booking_id", ordinal = 2, type = PrimaryKeyType.CLUSTERED)
        private @Getter @Setter UUID bookingId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            BookingByEndKey that = (BookingByEndKey) o;

            if (!hotelId.equals(that.hotelId)) return false;
            if (!end.equals(that.end)) return false;
            return bookingId.equals(that.bookingId);

        }

        @Override
        public int hashCode() {
            int result = hotelId.hashCode();
            result = 31 * result + end.hashCode();
            result = 31 * result + bookingId.hashCode();
            return result;
        }
    }

}
