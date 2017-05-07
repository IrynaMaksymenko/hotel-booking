package com.exercises.hotelbooking.controllers;

import com.exercises.hotelbooking.models.Hotel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import static java.util.Arrays.asList;

@RestController
public class HotelBookingController {

    private static final Logger log = LoggerFactory.getLogger(HotelBookingController.class);

    /**
     * Get all hotels of a current city.
     */
    @RequestMapping(value = "/hotels", method = RequestMethod.GET)
    public Collection<Hotel> getHotels(@RequestParam String city) {
        log.debug("Retrieving hotels in {}", city);
        final Hotel hotel = new Hotel();
        hotel.setCity(city);
        return asList(hotel);
    }

}
