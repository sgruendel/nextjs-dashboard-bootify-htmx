package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.User;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface UserRepository extends MongoRepository<User, UUID> {
}
