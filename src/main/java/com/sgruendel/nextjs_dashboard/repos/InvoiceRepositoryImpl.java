package com.sgruendel.nextjs_dashboard.repos;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.lang.NonNull;

import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.util.CustomProjectAggregationOperation;
import com.sgruendel.nextjs_dashboard.util.WebUtils;

import lombok.Data;

public class InvoiceRepositoryImpl implements InvoiceRepositoryCustom {

    private final MongoOperations mongoOperations;

    // used to map aggregation count results from MongoDB to Java
    @Data
    private static final class AggregationCount {
        long count;
    }

    public InvoiceRepositoryImpl(MongoOperations mongoOperations) {

        this.mongoOperations = mongoOperations;
    }

    @Override
    public List<Invoice> findAllMatchingSearch(final @NonNull String query, final Locale locale, final int pageSize, final long pageNumber) {

        final AggregationResults<Invoice> results = mongoOperations.aggregate(
                Aggregation.newAggregation(
                        Invoice.class,
                        newFilteredInvoicesAggregationOperation(query, locale),
                        Aggregation.unwind("$customer"),
                        Aggregation.sort(Direction.DESC, "date"),
                        Aggregation.skip(pageNumber * pageSize),
                        Aggregation.limit(pageSize)),
                Invoice.class);
        return results.getMappedResults();
    }

    @Override
    public long countMatchingSearch(final @NonNull String query, final Locale locale) {
        final AggregationResults<AggregationCount> results = mongoOperations.aggregate(
                Aggregation.newAggregation(
                        Invoice.class,
                        newFilteredInvoicesAggregationOperation(query, locale),
                        Aggregation.unwind("$customer"),
                        Aggregation.count().as("count")),
                AggregationCount.class);

        final AggregationCount count = results.getUniqueMappedResult();
        return count != null ? count.count : 0;
    }

    private AggregationOperation newFilteredInvoicesAggregationOperation(final @NonNull String query,
            final Locale locale) {

        final String queryLower = query.toLowerCase(locale);
        final String jsonOperation = """
                {
                  $lookup: {
                    from: 'customers',
                    localField: 'customer_id',
                    foreignField: '_id',
                    let: {
                      invoice_amount: { $toString: '$amount' },
                      invoice_date: { $dateToString: { format: '%s', date: '$date' } },
                      invoice_status: '$status',
                    },
                    pipeline: [
                      {
                        $match: {
                          $expr: {
                            $or: [
                              { $gte: [{ $indexOfCP: [{ $toLower: '$name' }, '%s'] }, 0] },
                              { $gte: [{ $indexOfCP: [{ $toLower: '$email' }, '%s'] }, 0] },
                              { $gte: [{ $indexOfCP: ['$$invoice_amount', '%s'] }, 0] },
                              { $gte: [{ $indexOfCP: [{ $toLower: '$$invoice_date' }, '%s'] }, 0] },
                              { $gte: [{ $indexOfCP: ['$$invoice_status', '%s'] }, 0] },
                            ],
                          },
                        },
                      },
                    ],
                    as: 'customer',
                  },
                }
                """.formatted(WebUtils.MONGO_DATE_FORMAT, queryLower, queryLower, queryLower, queryLower, queryLower);
        return new CustomProjectAggregationOperation(jsonOperation);
    }

}
