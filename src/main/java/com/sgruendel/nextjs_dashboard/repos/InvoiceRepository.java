package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Invoice;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface InvoiceRepository extends MongoRepository<Invoice, UUID> {
}
