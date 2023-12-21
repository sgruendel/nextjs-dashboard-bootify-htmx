package com.sgruendel.nextjs_dashboard.repos;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.sgruendel.nextjs_dashboard.domain.Customer;

public interface CustomerRepository extends MongoRepository<Customer, String>, CustomerRepositoryCustom {

    boolean existsById(final String id);

    boolean existsByEmailIgnoreCase(final String email);

    Customer findByEmailIgnoreCase(final String email);

    List<Customer> findAllByOrderByNameAsc(final Pageable pageable);
}
