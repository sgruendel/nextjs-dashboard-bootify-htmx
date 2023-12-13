package com.sgruendel.nextjs_dashboard.repos;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.lang.NonNull;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import com.sgruendel.nextjs_dashboard.domain.CustomerWithTotals;
import com.sgruendel.nextjs_dashboard.domain.Invoice;
import com.sgruendel.nextjs_dashboard.model.Status;

public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

  private final MongoOperations mongoOperations;

  public CustomerRepositoryImpl(final MongoOperations mongoOperations) {

    this.mongoOperations = mongoOperations;
  }

  @Override
  public List<CustomerWithTotals> findAllMatchingSearch(final @NonNull String query, final @NonNull Locale locale) {

    final AggregationResults<CustomerWithTotals> results = mongoOperations.aggregate(
        Aggregation.newAggregation(
            Customer.class,
            newFilteredCustomersAggregationOperation(query, locale),
            Aggregation.unwind("$invoice"),
            newGroupedCustomersAggregationOperation(),
            newMergeAggregationOperation(),
            Aggregation.sort(Direction.ASC, "name")),
        CustomerWithTotals.class);
    return results.getMappedResults();
  }

  private AggregationOperation newFilteredCustomersAggregationOperation(final @NonNull String query,
      final @NonNull Locale locale) {

    final String queryLower = query.toLowerCase(locale);

    final String jsonOperation = """
        {
          $lookup: {
            from: '%s',
            localField: '_id',
            foreignField: 'customer',
            let: {
              customer_name: '$name',
              customer_email: '$email',
            },
            pipeline: [
              {
                $match: {
                  $expr: {
                    $or: [
                      { $gte: [{ $indexOfCP: ['$$customer_name', '%s'] }, 0] },
                      { $gte: [{ $indexOfCP: ['$$customer_email', '%s'] }, 0] },
                    ],
                  },
                },
              },
            ],
            as: 'invoice',
          },
        }
        """.formatted(Invoice.COLLECTION_NAME, queryLower, queryLower);
    return new CustomProjectAggregationOperation(jsonOperation);
  }

  private AggregationOperation newGroupedCustomersAggregationOperation() {

    final String jsonOperation = """
        {
          $group: {
            _id: {
              _id: '$_id',
              name: '$name',
              email: '$email',
              imageUrl: '$imageUrl',
            },
            total_invoices: { $count: {} },
            total_pending: {
              $sum: {
                $cond: [{ $eq: ['$invoice.status', '%s'] }, '$invoice.amount', 0],
              },
            },
            total_paid: {
              $sum: {
                $cond: [{ $eq: ['$invoice.status', '%s'] }, '$invoice.amount', 0],
              },
            },
          },
        },
        """.formatted(Status.PENDING.name(), Status.PAID.name());
    return new CustomProjectAggregationOperation(jsonOperation);
  }

  private AggregationOperation newMergeAggregationOperation() {

    final String jsonOperation = """
        {
          $replaceWith: {
            $mergeObjects: [
              '$_id',
              { totalInvoices: '$total_invoices', totalPending: '$total_pending', totalPaid: '$total_paid' },
            ],
          },
        },
        """;
    return new CustomProjectAggregationOperation(jsonOperation);
  }

}
