package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Invoices;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface InvoicesRepository extends MongoRepository<Invoices, UUID> {
}
