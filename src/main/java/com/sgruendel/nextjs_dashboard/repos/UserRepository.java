package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.User;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, String> {

    boolean existsByIdIgnoreCase(final String id);

    boolean existsByEmailIgnoreCase(final String email);

    User findByEmailIgnoreCase(final String email);
}
