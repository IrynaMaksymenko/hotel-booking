package com.exercises.hotelbooking.entities;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HotelEntity {

    @NotEmpty
    private @Getter @Setter List<Hotel> hotels = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hotel {

        @Null(message = "Hotel id is read only")
        private @Getter @Setter UUID id;

        @NotBlank(message = "Hotel name must not be blank!")
        private @Getter @Setter String name;

        @NotBlank(message = "Hotel city must not be blank!")
        private @Getter @Setter String city;

        @NotBlank(message = "Hotel address must not be blank!")
        private @Getter @Setter String address;

        @Range(message = "Rating should be in range 1..5", min = 1, max = 5)
        private @Getter @Setter int rating;

    }

}
