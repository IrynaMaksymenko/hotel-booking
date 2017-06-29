package com.exercises.hotelbooking.services;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.datastax.driver.core.utils.UUIDs;
import com.exercises.hotelbooking.database.models.*;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.*;
import static com.exercises.hotelbooking.services.Dates.*;

@Slf4j
@Service
public class HotelBookingService {

    @Autowired
    private CassandraTemplate cassandraTemplate;

    public UUID saveHotel(String name, String city, String address, int rating) {
        log.debug("Adding a new hotel in {}", city);
        final UUID hotelId = UUIDs.random();
        cassandraTemplate.insert(new Hotel(hotelId, name, city, address, rating));
        cassandraTemplate.insert(new HotelByCity(new HotelByCity.HotelByCityKey(city, hotelId), name, address, rating));
        return hotelId;
    }

    public UUID saveRoom(UUID hotelId, String roomType, List<String> facilities) {
        log.debug("Adding a new room in hotel {}", hotelId);
        final UUID roomId = UUIDs.random();
        cassandraTemplate.insert(new Room(new Room.RoomKey(hotelId, roomId), roomType, facilities));
        return roomId;
    }

    public UUID saveGuest(String firstName, String lastName, String phoneNumber, String email) {
        log.debug("Adding a new guest {} {}", firstName, lastName);
        final UUID guestId = UUIDs.random();
        cassandraTemplate.insert(new Guest(guestId, firstName, lastName, phoneNumber, email));
        return guestId;
    }

    public List<HotelByCity> getHotelsByCity(String city) {
        log.debug("Retrieving hotels in {}", city);
        return cassandraTemplate.select(
                select().from("hotels_by_city")
                        .where(eq("city", city))
                        .toString(), HotelByCity.class);
    }

    public List<Room> getFreeRoomsByHotelAndPeriod(UUID hotelId, LocalDate start, LocalDate end) {
        log.debug("Retrieving free rooms in {} for period [{},{}]", hotelId, start, end);

        final long startTime = toMillis(start, CHECK_IN_TIME);
        final long endTime = toMillis(end, CHECK_OUT_TIME);

        final List<Room> hotelRooms = cassandraTemplate.select(
                select().from("rooms")
                        .where(QueryBuilder.eq("hotel_id", hotelId)).toString(), Room.class);

        final Select selectByStart = select("room_id").from("bookings_by_start");
        selectByStart.where(eq("hotel_id", hotelId))
                .and(lt("start", endTime))
                // skip too old bookings
                .and(gt("start", toMillis(start.minusMonths(1), CHECK_IN_TIME)));
        final List<UUID> bookedRoomsByStart = cassandraTemplate.queryForList(selectByStart, UUID.class);

        final Select selectByEnd = select("room_id").from("bookings_by_end");
        selectByEnd.where(eq("hotel_id", hotelId))
                .and(gt("end", startTime))
                // skip bookings in far future
                .and(lt("end", toMillis(end.plusMonths(1), CHECK_OUT_TIME)));
        final List<UUID> bookedRoomsByEnd = cassandraTemplate.queryForList(selectByEnd, UUID.class);

        final Sets.SetView<UUID> bookedRoomIds = Sets.intersection(
                Sets.newHashSet(bookedRoomsByStart), Sets.newHashSet(bookedRoomsByEnd));

        return hotelRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getKey().getRoomId()))
                .collect(Collectors.toList());
    }

    public List<BookingByGuest> getBookings(UUID guestId, LocalDate date) {
        log.debug("Retrieving bookings for guest {}", guestId);
        final long dateTime = toMillis(date, CHECK_IN_TIME);
        return cassandraTemplate.select(
                select().from("bookings_by_guest")
                        .where(eq("guest_id", guestId))
                        .and(QueryBuilder.eq("start", dateTime)).toString(), BookingByGuest.class);
    }

    public UUID saveBooking(UUID hotelId, UUID roomId, UUID guestId, LocalDate start, LocalDate end) {
        final List<UUID> freeRooms = getFreeRoomsByHotelAndPeriod(hotelId, start, end).stream()
                .map(room -> room.getKey().getRoomId())
                .collect(Collectors.toList());

        if (!freeRooms.contains(roomId)) {
            throw new IllegalArgumentException("Room is already booked!");
        }

        log.debug("Booking room {} in hotel {} for period [{},{}]", roomId, hotelId, start, end);

        final long startTime = toMillis(start, CHECK_IN_TIME);
        final long endTime = toMillis(end, CHECK_OUT_TIME);

        final UUID bookingId = UUIDs.random();

        cassandraTemplate.insert(new BookingByGuest(
                new BookingByGuest.BookingByGuestKey(guestId, startTime, bookingId),
                endTime, roomId, hotelId));
        cassandraTemplate.insert(new BookingByStart(
                new BookingByStart.BookingByStartKey(hotelId, startTime, bookingId),
                endTime, roomId, guestId));
        cassandraTemplate.insert(new BookingByEnd(
                new BookingByEnd.BookingByEndKey(hotelId, endTime, bookingId),
                startTime, roomId, guestId));

        return bookingId;
    }

}
