package com.exercises.hotelbooking.database.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.cassandra.mapping.Column;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Table("guests")
public class Guest {

    @PrimaryKey("guest_id")
    private @Getter @Setter UUID id;

    @Column("first_name")
    private @Getter @Setter String firstName;

    @Column("last_name")
    private @Getter @Setter String lastName;

    @Column("phone_number")
    private @Getter @Setter String phoneNumber;

    @Column("email")
    private @Getter @Setter String email;

}
