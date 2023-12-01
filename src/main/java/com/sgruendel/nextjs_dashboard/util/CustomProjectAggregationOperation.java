package com.sgruendel.nextjs_dashboard.util;

import org.bson.Document;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;

// see https://stackoverflow.com/questions/51107626/spring-data-mongodb-lookup-with-pipeline-aggregation
public class CustomProjectAggregationOperation implements AggregationOperation {

    private final String jsonOperation;

    public CustomProjectAggregationOperation(String jsonOperation) {
        this.jsonOperation = jsonOperation;
    }

    @Override
    public Document toDocument(AggregationOperationContext aggregationOperationContext) {
        return aggregationOperationContext.getMappedObject(Document.parse(jsonOperation));
    }
}