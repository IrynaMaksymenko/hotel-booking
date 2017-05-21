package com.exercises.hotelbooking.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import javax.validation.constraints.Pattern;
import java.util.UUID;

@Table("guests")
public class Guest {

    @PrimaryKey
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

}
