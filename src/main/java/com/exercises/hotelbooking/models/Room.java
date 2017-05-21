package com.exercises.hotelbooking.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Table("rooms")
public class Room {

    @PrimaryKey
    @JsonIgnore
    private @Getter @Setter UUID id;

    @NotNull(message = "Hotel id must not be blank!")
    private @Getter @Setter UUID hotelId;

    @NotNull(message = "Room number must not be blank!")
    private @Getter @Setter Integer roomNumber;

    @NotBlank(message = "Room class must not be blank!")
    private @Getter @Setter String roomClass;

}
