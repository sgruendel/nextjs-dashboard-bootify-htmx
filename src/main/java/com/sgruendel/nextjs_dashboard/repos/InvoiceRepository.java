package com.sgruendel.nextjs_dashboard.repos;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.Invoice;

public interface InvoiceRepository extends MongoRepository<Invoice, String>, InvoiceRepositoryCustom {

    Invoice findFirstByCustomer(final Customer customer);

    boolean existsById(final String id);

    List<Invoice> findAllByOrderByDateDesc(final Pageable pageable);

    List<Invoice> findFirst5ByOrderByDateDesc();
}
