package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface CustomerRepository extends MongoRepository<Customer, UUID> {

    Customer findByEmail(String email);
}
