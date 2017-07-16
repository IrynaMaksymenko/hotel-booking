package com.exercises.hotelbooking.security;

import com.exercises.hotelbooking.database.models.Guest;
import com.exercises.hotelbooking.services.HotelBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import static com.google.common.collect.Lists.newArrayList;

@Slf4j
@Component
public class DatabaseAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HotelBookingService service;

    @Override
    public Authentication authenticate(final Authentication auth) throws AuthenticationException {
        String email = String.valueOf(auth.getPrincipal());
        String password = String.valueOf(auth.getCredentials());

        final Guest guest = service.findGuest(email);
        if (guest == null) {
            log.warn("Guest {} not found", email);
            throw new BadCredentialsException("Guest not found");
        }
        if (passwordEncoder.matches(password, guest.getPasswordHash())) {
            return new UsernamePasswordAuthenticationToken(email, null,
                    newArrayList(new SimpleGrantedAuthority("ROLE_USER")));
        } else {
            log.warn("Entered invalid password for guest {}", email);
            throw new BadCredentialsException("Invalid password");
        }
    }

    @Override
    public boolean supports(final Class<?> aClass) {
        return aClass.isAssignableFrom(UsernamePasswordAuthenticationToken.class);
    }
}
