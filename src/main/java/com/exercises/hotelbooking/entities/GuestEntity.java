package com.exercises.hotelbooking.entities;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuestEntity {

    private @Getter @Setter List<Guest> guests = new ArrayList<>();

    public static class Guest {

        @Null(message = "Guest id is read only")
        private @Getter @Setter UUID id;

        @NotBlank(message = "Guest first name must not be blank!")
        private @Getter @Setter String firstName;

        @NotBlank(message = "Guest last name must not be blank!")
        private @Getter @Setter String lastName;

        @NotBlank(message = "Guest phone number must not be blank!")
        @Pattern(regexp="(^$|[0-9]{10})", message = "Phone number must be numeric")
        private @Getter @Setter String phoneNumber;

        @Email
        private @Getter @Setter String email;

        @NotBlank(message = "Guest password must not be blank!")
        private @Getter @Setter String password;

    }

}
