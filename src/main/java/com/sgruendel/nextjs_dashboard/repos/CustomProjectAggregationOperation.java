package com.sgruendel.nextjs_dashboard.repos;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

// see https://stackoverflow.com/questions/51107626/spring-data-mongodb-lookup-with-pipeline-aggregation
public class CustomProjectAggregationOperation implements AggregationOperation {

    private final Document document;

    public CustomProjectAggregationOperation(final @NonNull String jsonOperation) {
        this.document = Document.parse(jsonOperation);
    }

    @Override
    @SuppressWarnings("all")
    public Document toDocument(final AggregationOperationContext context) {
        // Not necessary to return anything as we override toPipelineStages():
        return null;
    }

    @Override
    public @NonNull List<Document> toPipelineStages(final @NonNull AggregationOperationContext context) {
        return Collections.singletonList(context.getMappedObject(document));
    }

    @Override
    public @NonNull String getOperator() {
        return document.keySet().iterator().next();
    }
}