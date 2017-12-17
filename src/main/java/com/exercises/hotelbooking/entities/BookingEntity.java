package com.exercises.hotelbooking.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

public class BookingEntity {

    @NotEmpty
    private @Getter @Setter List<Booking> bookings = new ArrayList<>();

    @AllArgsConstructor
    @NoArgsConstructor
    @ScriptAssert(lang = "javascript", script = "_this.start.isBefore(_this.end) " +
            "&& _this.start.isAfter(java.time.LocalDate.now())",
            message = "Booking start must be in the future and before end!")
    public static class Booking {

        @Null(message = "Room id is read only")
        private @Getter @Setter UUID id;

        @NotNull(message = "Room id must not be blank!")
        private @Getter @Setter UUID roomId;

        @NotNull(message = "Hotel id must not be blank!")
        private @Getter @Setter UUID hotelId;

        @NotNull(message = "Guest id must not be blank!")
        private @Getter @Setter UUID guestId;

        @NotNull(message = "Booking start must not be blank!")
        @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy")
        @ApiModelProperty(example = "13-12-2017")
        private @Getter @Setter LocalDate start;

        @Null(message = "Check in time is read only")
        @JsonFormat(shape = STRING, pattern = "HH:mm")
        @ApiModelProperty(example = "14:00")
        private @Getter @Setter LocalTime checkInTime;

        @NotNull(message = "Booking end must not be blank!")
        @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy")
        @ApiModelProperty(example = "15-12-2017")
        private @Getter @Setter LocalDate end;

        @Null(message = "Check out time is read only")
        @JsonFormat(shape = STRING, pattern = "HH:mm")
        @ApiModelProperty(example = "12:00")
        private @Getter @Setter LocalTime checkOutTime;

    }

}
