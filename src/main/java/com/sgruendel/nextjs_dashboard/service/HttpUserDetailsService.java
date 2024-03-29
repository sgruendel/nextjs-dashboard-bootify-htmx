package com.sgruendel.nextjs_dashboard.service;

import com.sgruendel.nextjs_dashboard.model.UserDTO;
import com.sgruendel.nextjs_dashboard.util.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class HttpUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUserDetailsService.class);

    private final UserService userService;

    public HttpUserDetailsService(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) {
        try {
            final UserDTO user = userService.findByEmailIgnoreCase(username);
            return User.withUsername(username).password(user.getPassword()).roles("USER").build();
        } catch (NotFoundException e) {
            LOGGER.warn("user not found: {}", username);
            throw new UsernameNotFoundException("User '" + username + "' not found");
        }
    }

}