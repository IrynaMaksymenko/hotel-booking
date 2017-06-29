package com.exercises.hotelbooking.entities;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomEntity {

    private @Getter @Setter List<Room> rooms = new ArrayList<>();

    @AllArgsConstructor
    @NoArgsConstructor
    public static class Room {

        @Null(message = "Room id is read only")
        private @Getter @Setter UUID id;

        @NotNull(message = "Hotel id must not be blank!")
        private @Getter @Setter UUID hotelId;

        @NotBlank(message = "Room type must not be blank!")
        private @Getter @Setter String roomType;

        private @Getter @Setter List<String> facilities;

    }

}
