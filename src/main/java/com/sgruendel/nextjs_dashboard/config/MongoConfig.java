package com.sgruendel.nextjs_dashboard.config;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.sgruendel.nextjs_dashboard.util.MongoOffsetDateTimeReader;
import com.sgruendel.nextjs_dashboard.util.MongoOffsetDateTimeWriter;

@Configuration
@EnableMongoRepositories("com.sgruendel.nextjs_dashboard.repos")
@EnableMongoAuditing(dateTimeProviderRef = "auditingDateTimeProvider")
public class MongoConfig {

    /*
     * @Bean
     * public MongoTransactionManager transactionManager(final MongoDatabaseFactory
     * databaseFactory) {
     * return new MongoTransactionManager(databaseFactory);
     * }
     */

    @Bean("auditingDateTimeProvider")
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(OffsetDateTime.now());
    }

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(final LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
                new MongoOffsetDateTimeWriter(),
                new MongoOffsetDateTimeReader()));
    }

}
