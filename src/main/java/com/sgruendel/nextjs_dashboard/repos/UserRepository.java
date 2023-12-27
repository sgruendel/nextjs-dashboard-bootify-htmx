package com.sgruendel.nextjs_dashboard.repos;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.sgruendel.nextjs_dashboard.domain.User;

public interface UserRepository extends MongoRepository<User, String> {

    boolean existsById(final String id);

    boolean existsByEmailIgnoreCase(final String email);

    Optional<User> findByEmailIgnoreCase(final String email);
}
