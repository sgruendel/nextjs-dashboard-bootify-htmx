package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Customer;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.stereotype.Component;


@Component
public class CustomerListener extends AbstractMongoEventListener<Customer> {

    @Override
    public void onBeforeConvert(final BeforeConvertEvent<Customer> event) {
        if (event.getSource().getId() == null) {
            event.getSource().setId(UUID.randomUUID());
        }
    }

}
