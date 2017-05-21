package com.exercises.hotelbooking.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.cassandra.mapping.PrimaryKey;
import org.springframework.data.cassandra.mapping.Table;

import java.util.Date;
import java.util.UUID;

@Table("hotels")
public class Hotel {

    @PrimaryKey
    private @Getter @Setter UUID id;

    @NotBlank(message = "Hotel name must not be blank!")
    private @Getter @Setter String name;

    @NotBlank(message = "Hotel city must not be blank!")
    private @Getter @Setter String city;

    private @Getter @Setter Date added;

}
