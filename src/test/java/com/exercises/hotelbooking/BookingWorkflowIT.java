package com.exercises.hotelbooking;

import com.exercises.hotelbooking.entities.BookingEntity;
import com.exercises.hotelbooking.entities.GuestEntity;
import com.exercises.hotelbooking.entities.HotelEntity;
import com.exercises.hotelbooking.entities.RoomEntity;
import org.apache.tomcat.util.codec.binary.Base64;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@FixMethodOrder(NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingWorkflowIT {

    private static final String EMAIL = "test@exercise.com";
    private static final String PASSWORD = "test";

    @Autowired
    private TestRestTemplate restTemplate;

    private ClientHttpRequestFactory defaultRequestFactory;

    // TODO: clean up database before and after test, use test keyspace?
    @Before
    public void setUp() {
        defaultRequestFactory = restTemplate.getRestTemplate().getRequestFactory();
    }

    @Test
    public void adminWorkflowTest() {
        login("admin", "admin");

        // ADD NEW HOTEL
        final UUID hotelId = adminCanAddNewHotel();

        // ADD ROOMS
        adminCanAddRooms(hotelId);

        logout();
    }

    @Test
    public void userWorkflowTest() {

        // ADD GUEST
        final UUID guestId = guestCanBeAdded(EMAIL, PASSWORD);

        login(EMAIL, PASSWORD);

        // FIND HOTEL IN LVIV
        final UUID hotelId = userCanFindAHotel();

        // GET FREE ROOMS
        final List<UUID> freeRooms = getFreeRooms(hotelId);
        assertThat(freeRooms, hasSize(greaterThanOrEqualTo(2)));

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

        logout();
    }

    private void login(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(Charset.forName("US-ASCII")) );
        String authHeader = "Basic " + new String( encodedAuth );
        restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory() {
            @Override
            public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
                final ClientHttpRequest clientHttpRequest = super.createRequest(uri, httpMethod);
                clientHttpRequest.getHeaders().add("Authorization", authHeader);
                return clientHttpRequest;
            }
        });
    }

    private void logout() {
        restTemplate.getRestTemplate().setRequestFactory(defaultRequestFactory);
    }

    private UUID userCanFindAHotel() {
        ResponseEntity<HotelEntity> hotelsResponse = restTemplate.getForEntity("/hotels?city=Lviv", HotelEntity.class);
        assertThat(hotelsResponse.getStatusCode(), is(HttpStatus.OK));

        return hotelsResponse.getBody().getHotels().get(0).getId();
    }

    private UUID adminCanAddNewHotel() {
        ResponseEntity<HotelEntity> hotelsResponse;
        final HotelEntity request = new HotelEntity();
        final HotelEntity.Hotel newHotel = new HotelEntity.Hotel();
        newHotel.setCity("Lviv");
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
        return hotelsResponse.getBody().getHotels().get(0).getId();
    }

    private void adminCanAddRooms(UUID hotelId) {
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

    private UUID guestCanBeAdded(String email, String password) {
        final GuestEntity request = new GuestEntity();
        final GuestEntity.Guest guest = new GuestEntity.Guest();
        guest.setFirstName("Irina");
        guest.setLastName("Maximenko");
        guest.setPhoneNumber("0631234567");
        guest.setEmail(email);
        guest.setPassword(password);
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
