package com.sgruendel.nextjs_dashboard.repos;

import com.sgruendel.nextjs_dashboard.domain.Invoice;

import java.util.List;
import java.util.Locale;

import org.springframework.lang.NonNull;

public interface InvoiceRepositoryCustom {

    /**
     * Find all invoices matching search term {@code query}, returning a subset
     * representing a specific page. Search term is
     * converted to lower case and searched for in:
     * <ul>
     * <li>customer name</li>
     * <li>customer email</li>
     * <li>invoice amount</li>
     * <li>invoice state</li>
     * <li>invoice date</li>
     * </ul>
     *
     * @param query      search term
     * @param locale     Locale used to convert search term to lower case
     * @param pageSize   page size of the page to be returned, must be greater than
     *                   {@code 0} .
     * @param pageNumber page number, {@code 0} is first page
     * @return List of invoices matching search term, max. {@code pageSize} in size
     */
    List<Invoice> findAllMatchingSearch(final String query, final Locale locale, final int pageSize,
            final long pageNumber);

    /**
     * Count all invoices matching search term {@code query}. Search is performed as
     * described in {@link #findAllMatchingSearch(String, Locale, int, long)}.
     *
     * @param query  search term
     * @param locale Locale used to convert search term to lower case
     * @return Number of invoices matching search term
     */
    long countMatchingSearch(final @NonNull String query, final Locale locale);
}
