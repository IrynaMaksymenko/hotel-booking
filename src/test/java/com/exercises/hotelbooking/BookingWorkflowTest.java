package com.exercises.hotelbooking;

import com.exercises.hotelbooking.entities.BookingEntity;
import com.exercises.hotelbooking.entities.GuestEntity;
import com.exercises.hotelbooking.entities.HotelEntity;
import com.exercises.hotelbooking.entities.RoomEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingWorkflowTest {

    @Autowired
    private TestRestTemplate restTemplate;

    // TODO: clean up database before and after test, use test keyspace?
    @Test
    public void workflowTest() {

        // FIND HOTEL IN LVIV
        final UUID hotelId = hotelCanBeAddedAndFound();

        // ADD ROOMS
        roomsCanBeAdded(hotelId);

        // GET FREE ROOMS
        final List<UUID> freeRooms = getFreeRooms(hotelId);
        assertThat(freeRooms, hasSize(greaterThanOrEqualTo(2)));

        // ADD GUEST
        final UUID guestId = guestCanBeAdded();

        // ADD BOOKING
        final UUID bookedRoomId = freeRooms.get(0);
        final UUID bookingId = roomCanBeBooked(hotelId, bookedRoomId, guestId);

        // CAN NOT ADD BOOKING FOR SAME ROOM
        bookOccupiedRoomFails(hotelId, bookedRoomId, guestId);

        // FIND GUEST BOOKING
        bookingCanBeFoundByGuest(guestId, bookingId);

        // GET FREE ROOMS
        final List<UUID> leftRooms = getFreeRooms(hotelId);
        assertThat(leftRooms, hasSize(freeRooms.size() - 1));
    }

    private UUID hotelCanBeAddedAndFound() {
        ResponseEntity<HotelEntity> hotelsResponse = restTemplate.getForEntity("/hotels?city=Lviv", HotelEntity.class);
        assertThat(hotelsResponse.getStatusCode(), is(HttpStatus.OK));

        if (hotelsResponse.getBody().getHotels().isEmpty()) {
            // ADD NEW HOTEL
            final HotelEntity request = new HotelEntity();
            final HotelEntity.Hotel newHotel = new HotelEntity.Hotel();
            newHotel.setName("Astoria");
            newHotel.setAddress("Horodotska Str");
            newHotel.setRating(4);
            request.setHotels(newArrayList(newHotel));
            ResponseEntity<HotelEntity> addHotelResponse =
                    restTemplate.postForEntity("/hotels", request, HotelEntity.class);
            assertThat(addHotelResponse.getStatusCode(), is(HttpStatus.OK));
            assertThat(addHotelResponse.getBody().getHotels(), hasItem(hasProperty("id", notNullValue())));

            hotelsResponse = restTemplate.getForEntity("/hotels?city=Lviv", HotelEntity.class);
            assertThat(hotelsResponse.getStatusCode(), is(HttpStatus.OK));
            assertThat(hotelsResponse.getBody().getHotels(), hasSize(greaterThanOrEqualTo(1)));
        }

        return hotelsResponse.getBody().getHotels().get(0).getId();
    }

    private void roomsCanBeAdded(UUID hotelId) {
        // ADD NEW ROOMS
        final RoomEntity request = new RoomEntity();
        final RoomEntity.Room room1 = new RoomEntity.Room();
        room1.setHotelId(hotelId);
        room1.setRoomType("Standard");
        room1.setFacilities(newArrayList("fridge", "king size bed"));
        final RoomEntity.Room room2 = new RoomEntity.Room();
        room2.setHotelId(hotelId);
        room2.setRoomType("Lux");
        room2.setFacilities(newArrayList("fridge", "king size bed", "condition", "balcony"));
        request.setRooms(newArrayList(room1, room2));

        ResponseEntity<RoomEntity> addRoomsResponse =
                restTemplate.postForEntity("/rooms", request, RoomEntity.class);
        assertThat(addRoomsResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(addRoomsResponse.getBody().getRooms(), hasSize(greaterThanOrEqualTo(2)));
    }

    private List<UUID> getFreeRooms(UUID hotelId) {
        ResponseEntity<RoomEntity> freeRoomsResponse = restTemplate.getForEntity(
                "/rooms?hotelId={hotel}&start=2017-07-01&end=2017-07-03", RoomEntity.class, hotelId);
        assertThat(freeRoomsResponse.getStatusCode(), is(HttpStatus.OK));

        return freeRoomsResponse.getBody().getRooms().stream()
                .map(RoomEntity.Room::getId)
                .collect(Collectors.toList());
    }

    private UUID guestCanBeAdded() {
        final GuestEntity request = new GuestEntity();
        final GuestEntity.Guest guest = new GuestEntity.Guest();
        guest.setFirstName("Irina");
        guest.setLastName("Maximenko");
        guest.setPhoneNumber("0631234567");
        guest.setEmail("test@exercise.com");
        request.setGuests(newArrayList(guest));

        ResponseEntity<GuestEntity> response = restTemplate.postForEntity("/guests", request, GuestEntity.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getGuests(), hasSize(1));

        return response.getBody().getGuests().get(0).getId();
    }

    private UUID roomCanBeBooked(UUID hotelId, UUID roomId, UUID guestId) {
        final BookingEntity request = new BookingEntity();
        final BookingEntity.Booking booking = new BookingEntity.Booking();
        booking.setHotelId(hotelId);
        booking.setRoomId(roomId);
        booking.setGuestId(guestId);
        booking.setStart(LocalDate.parse("2017-07-01"));
        booking.setEnd(LocalDate.parse("2017-07-03"));
        request.setBookings(newArrayList(booking));

        ResponseEntity<BookingEntity> response = restTemplate.postForEntity("/bookings", request, BookingEntity.class);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getBookings(), hasSize(1));

        return response.getBody().getBookings().get(0).getId();
    }

    private void bookOccupiedRoomFails(UUID hotelId, UUID bookedRoomId, UUID guestId) {
        final BookingEntity request = new BookingEntity();
        final BookingEntity.Booking booking = new BookingEntity.Booking();
        booking.setHotelId(hotelId);
        booking.setRoomId(bookedRoomId);
        booking.setGuestId(guestId);
        booking.setStart(LocalDate.parse("2017-07-01"));
        booking.setEnd(LocalDate.parse("2017-07-03"));
        request.setBookings(newArrayList(booking));

        ResponseEntity<BookingEntity> response = restTemplate.postForEntity("/bookings", request, BookingEntity.class);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    private void bookingCanBeFoundByGuest(UUID guestId, UUID bookingId) {
        ResponseEntity<BookingEntity> response = restTemplate.getForEntity(
                "/bookings?guestId={guest}&date=2017-07-01", BookingEntity.class, guestId);
        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody().getBookings(), hasSize(1));
        assertThat(response.getBody().getBookings().get(0).getId(), is(bookingId));
    }

    @TestConfiguration
    static class Config {

        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
            return new RestTemplateBuilder().requestFactory(SimpleClientHttpRequestFactory.class);
        }

    }

}
