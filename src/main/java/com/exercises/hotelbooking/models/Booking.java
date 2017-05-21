package com.exercises.hotelbooking.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.ScriptAssert;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.UUID;

@ScriptAssert(lang = "javascript", script = "_this.start.before(_this.end)",
        message = "Booking start must be before end!")
@Table("bookings")
public class Booking {

    @PrimaryKey
    private @Getter @Setter UUID id;

    @NotNull(message = "Room id must not be blank!")
    private @Getter @Setter UUID roomId;

    @NotNull(message = "Guest id must not be blank!")
    private @Getter @Setter UUID guestId;

    @NotNull(message = "Booking start must not be blank!")
    @Future(message = "Booking start must be in the future!")
    private @Getter @Setter Date start;

    @NotNull(message = "Booking end must not be blank!")
    @Future(message = "Booking end must be in the future!")
    private @Getter @Setter Date end;

}
