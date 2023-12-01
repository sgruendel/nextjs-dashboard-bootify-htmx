package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Invoice;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface InvoiceRepository extends MongoRepository<Invoice, ObjectId> {

    Page<Invoice> findAllByOrderByDateDesc(Pageable pageable);
}
