package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Revenue;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface RevenueRepository extends MongoRepository<Revenue, String> {

    boolean existsByMonthIgnoreCase(String month);

}
