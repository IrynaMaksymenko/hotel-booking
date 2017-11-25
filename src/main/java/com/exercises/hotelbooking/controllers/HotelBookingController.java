package com.exercises.hotelbooking.controllers;

import com.exercises.hotelbooking.database.models.Guest;
import com.exercises.hotelbooking.database.models.Room;
import com.exercises.hotelbooking.entities.*;
import com.exercises.hotelbooking.services.HotelBookingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.validator.constraints.ParameterScriptAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.exercises.hotelbooking.services.Dates.*;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

@Api(tags = "Hotel Booking", description = " ")
@RestController
public class HotelBookingController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HotelBookingService service;

    @ApiOperation("Retrieve all known hotels by city")
    @RequestMapping(value = "/hotels", method = RequestMethod.GET)
    public HotelEntity getHotels(@RequestParam String city) {
        final HotelEntity result = new HotelEntity();
        result.setHotels(service.getHotelsByCity(city)
                .stream()
                .map(entity -> new HotelEntity.Hotel(
                        entity.getKey().getId(), entity.getName(),
                        entity.getKey().getCity(), entity.getAddress(),
                        entity.getRating()))
                .collect(Collectors.toList()));
        return result;
    }

    @ApiOperation("Add new hotel(s) to the system (requires admin privileges)")
    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/hotels", method = RequestMethod.POST)
    public HotelEntity addHotel(@Valid @RequestBody HotelEntity hotelEntity) {
        for (HotelEntity.Hotel hotel : hotelEntity.getHotels()) {
            final UUID hotelId = service.saveHotel(
                    hotel.getName(), hotel.getCity(), hotel.getAddress(), hotel.getRating());
            hotel.setId(hotelId);
        }
        return hotelEntity;
    }

    @ApiOperation("Add new room(s) to a hotel (requires admin privileges)")
    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/rooms", method = RequestMethod.POST)
    public RoomEntity addRoom(@Valid @RequestBody RoomEntity roomEntity) {
        for (RoomEntity.Room room : roomEntity.getRooms()) {
            final UUID roomId = service.saveRoom(room.getHotelId(), room.getRoomType(), room.getFacilities());
            room.setId(roomId);
        }
        return roomEntity;
    }

    @ApiOperation("Retrieve free rooms in a specific hotel for the given period")
    @RequestMapping(value = "/rooms", method = RequestMethod.GET)
    @ParameterScriptAssert(lang = "javascript", script = "arg1.isBefore(arg2)",
            message = "Booking start must be before end!")
    public RoomEntity getFreeRooms(@RequestParam UUID hotelId,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start,
                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        final List<Room> freeRooms = service.getFreeRoomsByHotelAndPeriod(hotelId, start, end);
        final RoomEntity result = new RoomEntity();
        result.setRooms(freeRooms.stream()
                .map(room -> new RoomEntity.Room(
                        room.getKey().getRoomId(), room.getKey().getHotelId(),
                        room.getRoomType(), room.getFacilities()))
                .collect(Collectors.toList()));
        return result;
    }

    @ApiOperation("Add a new guest(s), who is going to book a room")
    @RequestMapping(value = "/guests", method = RequestMethod.POST)
    public GuestEntity addGuest(@Valid @RequestBody GuestEntity guestEntity) {
        for (GuestEntity.Guest guest : guestEntity.getGuests()) {
            final UUID guestId = service.saveGuest(
                    guest.getFirstName(), guest.getLastName(), guest.getPhoneNumber(), guest.getEmail(),
                    passwordEncoder.encode(guest.getPassword()));
            guest.setId(guestId);
            guest.setPassword(null);
        }
        return guestEntity;
    }

    @ApiOperation("Retrieve all guests (requires admin privileges)")
    @Secured("ROLE_ADMIN")
    @RequestMapping(value = "/guests", method = RequestMethod.GET)
    public GuestEntity getGuests() {
        GuestEntity guestEntity = new GuestEntity();
        for (Guest guest : service.findGuests()) {
            final GuestEntity.Guest aGuest = new GuestEntity.Guest();
            aGuest.setId(guest.getKey().getId());
            aGuest.setEmail(guest.getKey().getEmail());
            aGuest.setFirstName(guest.getFirstName());
            aGuest.setLastName(guest.getLastName());
            guestEntity.getGuests().add(aGuest);
        }
        return guestEntity;
    }

    @ApiOperation("Guest has booked some room(s). Gets booked room(s) by the specific date and guest number")
    @RequestMapping(value = "/bookings", method = RequestMethod.GET)
    public BookingEntity getBookings(@RequestParam UUID guestId,
                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        BookingEntity result = new BookingEntity();
        result.setBookings(service.getBookings(guestId, date)
                .stream()
                .map(b -> {
                    final LocalDateTime startTime = toDateTime(b.getKey().getStart());
                    final LocalDateTime endTime = toDateTime(b.getEnd());
                    return new BookingEntity.Booking(b.getKey().getBookingId(),
                            b.getRoomId(), b.getHotelId(), b.getKey().getGuestId(),
                            startTime.toLocalDate(), startTime.toLocalTime(),
                            endTime.toLocalDate(), endTime.toLocalTime());
                })
                .collect(Collectors.toList()));
        return result;
    }

    @ApiOperation("Add a new room booking for a guest")
    @RequestMapping(value = "/bookings", method = RequestMethod.POST)
    public BookingEntity bookRoom(@Valid @RequestBody BookingEntity bookingEntity) {

        for (BookingEntity.Booking booking : bookingEntity.getBookings()) {
            final UUID bookingId = service.saveBooking(booking.getHotelId(), booking.getRoomId(),
                    booking.getGuestId(), booking.getStart(), booking.getEnd());
            booking.setId(bookingId);
            booking.setCheckInTime(CHECK_IN_TIME);
            booking.setCheckOutTime(CHECK_OUT_TIME);
        }
        return bookingEntity;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ErrorEntity handleIllegalArgumentException(IllegalArgumentException e, HttpServletResponse response) {
        response.setStatus(SC_BAD_REQUEST);
        return new ErrorEntity(SC_BAD_REQUEST, e.getMessage());
    }

}
