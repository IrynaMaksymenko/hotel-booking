package com.exercises.hotelbooking.database.models;

import lombok.*;
import org.springframework.cassandra.core.PrimaryKeyType;
import org.springframework.data.cassandra.mapping.*;

import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Table("guests")
public class Guest {

    @PrimaryKey
    private @Getter @Setter GuestKey key;

    @Column("first_name")
    private @Getter @Setter String firstName;

    @Column("last_name")
    private @Getter @Setter String lastName;

    @Column("phone_number")
    private @Getter @Setter String phoneNumber;

    @Column("password_hash")
    private @Getter @Setter String passwordHash;

    @AllArgsConstructor
    @EqualsAndHashCode
    @PrimaryKeyClass
    public static class GuestKey implements Serializable {

        @PrimaryKeyColumn(name = "email", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
        private @Getter @Setter String email;

        @PrimaryKeyColumn(name = "guest_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
        private @Getter @Setter UUID id;

    }


}
