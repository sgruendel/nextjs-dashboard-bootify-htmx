package com.sgruendel.nextjs_dashboard.repos;

import java.util.List;
import java.util.Locale;

import org.springframework.lang.NonNull;

import com.sgruendel.nextjs_dashboard.domain.CustomerWithTotals;

public interface CustomerRepositoryCustom {

    /**
     * Find all customers with invoice totals matching search term {@code query}.
     * Search term is converted to lower case and searched for in:
     * <ul>
     * <li>customer name</li>
     * <li>customer email</li>
     * </ul>
     *
     * @param query  search term
     * @param locale Locale used to convert search term to lower case
     * @return List of customers with invoice totals matching search term
     */
    List<CustomerWithTotals> findAllMatchingSearch(final @NonNull String query, final @NonNull Locale locale);

}
