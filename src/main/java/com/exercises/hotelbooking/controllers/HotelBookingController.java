package com.exercises.hotelbooking.controllers;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import com.exercises.hotelbooking.models.Booking;
import com.exercises.hotelbooking.models.Guest;
import com.exercises.hotelbooking.models.Hotel;
import com.exercises.hotelbooking.models.Room;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.datastax.driver.core.querybuilder.QueryBuilder.select;
import static java.util.Comparator.comparing;

@Slf4j
@RestController
public class HotelBookingController {

    @Autowired
    private CassandraTemplate cassandraTemplate;

    /**
     * Get all hotels of a current city.
     */
    @RequestMapping(value = "/hotels", method = RequestMethod.GET)
    public Collection<Hotel> getHotels(@RequestParam String city) {
        log.debug("Retrieving hotels in {}", city);
        return cassandraTemplate.select(
                select().from("hotels")
                        .where(eq("city", city))
                        .toString(), Hotel.class)
                .stream()
                .sorted(comparing(Hotel::getName))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new hotel to the system.
     */
    @RequestMapping(value = "/hotels", method = RequestMethod.POST)
    public void addHotel(@Valid @RequestBody Hotel hotel) {
        log.debug("Adding a new hotel in {}", hotel.getCity());
        hotel.setId(UUIDs.timeBased());
        hotel.setAdded(new Date());
        cassandraTemplate.insert(hotel);
    }

    /**
     * Adds a new room to a hotel.
     */
    @RequestMapping(value = "/rooms", method = RequestMethod.POST)
    public void addRoom(@Valid @RequestBody Room room) {
        room.setId(UUIDs.timeBased());
        cassandraTemplate.insert(room);
    }

    /**
     * Gets free rooms in a specific hotel for the current period.
     */
    @RequestMapping(value = "/rooms", method = RequestMethod.GET)
    @ParameterScriptAssert(lang = "javascript", script = "arg0.before(arg1)",
            message = "Booking start must be before end!")
    public List<Room> getFreeRooms(@RequestParam UUID hotelId, @RequestParam Date start, @RequestParam Date end) {
        // TODO: fix selects, improve database schema
        final List<Room> hotelRooms = cassandraTemplate.select(
                select().from("rooms")
                        .where(QueryBuilder.eq("hotelId", hotelId)).toString(), Room.class);
        final List<UUID> bookedRoomIds = cassandraTemplate.select(
                select("roomId").from("bookings")
                        .where(QueryBuilder.gte("start", start))
                        .and(QueryBuilder.lt("end", end)).toString(), UUID.class);
        return hotelRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .collect(Collectors.toList());
    }

    /**
     * Adds a new guest, who is going to book a room.
     */
    @RequestMapping(value = "/guests", method = RequestMethod.POST)
    public void addGuest(@Valid @RequestBody Guest guest) {
        guest.setId(UUIDs.timeBased());
        cassandraTemplate.insert(guest);
    }

    /**
     * Guest has booked some room / rooms. Gets booked room / rooms by the specific date and guest number.
     */
    @RequestMapping(value = "/bookings", method = RequestMethod.GET)
    public void getBookings(@RequestParam UUID guestId, @RequestParam Date date) {
        cassandraTemplate.select(
                select().from("bookings")
                        .where(eq("guestId", guestId))
                        .and(QueryBuilder.gte("start", date))
                        .and(QueryBuilder.lt("end", date)).toString(), Booking.class);
    }

    /**
     * Adds a new room booking for a guest.
     */
    @RequestMapping(value = "/bookings", method = RequestMethod.POST)
    public void bookRoom(@Valid @RequestBody Booking booking) {
        booking.setId(UUIDs.timeBased());
        cassandraTemplate.insert(booking);
    }
}
