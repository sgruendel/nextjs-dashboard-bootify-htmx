package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Customer;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CustomerRepository extends MongoRepository<Customer, String>, CustomerRepositoryCustom {

    boolean existsByIdIgnoreCase(String id);

    boolean existsByEmailIgnoreCase(String email);

    Customer findByEmailIgnoreCase(final String email);

    List<Customer> findAllByOrderByNameAsc(final Pageable pageable);
}
