package com.sgruendel.nextjs_dashboard.repos;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.lang.NonNull;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.util.WebUtils;

import lombok.Data;

public class InvoiceRepositoryImpl implements InvoiceRepositoryCustom {

  private final MongoOperations mongoOperations;

  // used to map aggregation sum results from MongoDB to Java
  @Data
  private static final class GroupedSum {
    String _id;
    long sum;
  }

  // used to map aggregation count results from MongoDB to Java
  @Data
  private static final class AggregationCount {
    long count;
  }

  public InvoiceRepositoryImpl(final MongoOperations mongoOperations) {

    this.mongoOperations = mongoOperations;
  }

  @Override
  public Map<String, Long> sumAmountGroupByStatus() {
    final AggregationResults<GroupedSum> results = mongoOperations.aggregate(Aggregation.newAggregation(
        Invoice.class,
        Aggregation.group("status").sum("amount").as("sum")), GroupedSum.class);

    final Map<String, Long> result = new HashMap<>();
    results.getMappedResults().forEach(groupedSum -> result.put(groupedSum._id, groupedSum.sum));
    return result;
  }

  @Override
  public List<Invoice> findAllMatchingSearch(final @NonNull String query, final @NonNull Locale locale,
      final int pageSize, final long pageNumber) {

    final AggregationResults<Invoice> results = mongoOperations.aggregate(
        Aggregation.newAggregation(
            Invoice.class,
            newFilteredInvoicesAggregationOperation(query, locale),
            Aggregation.unwind("$customers"),
            Aggregation.sort(Direction.DESC, "date"),
            Aggregation.skip(pageNumber * pageSize),
            Aggregation.limit(pageSize)),
        Invoice.class);
    return results.getMappedResults();
  }

  @Override
  public long countMatchingSearch(final @NonNull String query, final @NonNull Locale locale) {
    final AggregationResults<AggregationCount> results = mongoOperations.aggregate(
        Aggregation.newAggregation(
            Invoice.class,
            newFilteredInvoicesAggregationOperation(query, locale),
            Aggregation.unwind("$customers"),
            Aggregation.count().as("count")),
        AggregationCount.class);

    final AggregationCount count = results.getUniqueMappedResult();
    return count != null ? count.count : 0;
  }

  private AggregationOperation newFilteredInvoicesAggregationOperation(final @NonNull String query,
      final @NonNull Locale locale) {

    final String queryLower = query.toLowerCase(locale);

    // upper case query for status corresponding to Java enum names
    final String queryUpper = query.toUpperCase(locale);

    final String jsonOperation = """
        {
          $lookup: {
            from: '%s',
            localField: 'customer',
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
            as: 'customers',
          },
        }
        """.formatted(Customer.COLLECTION_NAME, WebUtils.MONGO_DATE_FORMAT, queryLower, queryLower, queryLower,
        queryLower, queryUpper);
    return new CustomProjectAggregationOperation(jsonOperation);
  }

}
