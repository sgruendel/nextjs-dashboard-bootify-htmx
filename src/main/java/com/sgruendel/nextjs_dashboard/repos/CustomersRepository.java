package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Customers;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface CustomersRepository extends MongoRepository<Customers, UUID> {
}
