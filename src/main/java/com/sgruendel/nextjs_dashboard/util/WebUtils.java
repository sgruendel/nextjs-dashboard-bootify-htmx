package com.sgruendel.nextjs_dashboard.util;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;

@Component
public class WebUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebUtils.class);

    public static final String MSG_SUCCESS = "MSG_SUCCESS";
    public static final String MSG_INFO = "MSG_INFO";
    public static final String MSG_ERROR = "MSG_ERROR";

    // format for MongoDB $dateToString to convert date object to format used by
    // DateTimeFormatter, see
    // https://www.mongodb.com/docs/manual/reference/operator/aggregation/dateToString/
    public static final String MONGO_DATE_FORMAT = "%d %b, %Y";

    private static final DecimalFormat CURRENCY_FORMATTER = new DecimalFormat("#,##0.00");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    private static MessageSource messageSource;
    private static LocaleResolver localeResolver;

    public WebUtils(final MessageSource messageSource, final LocaleResolver localeResolver) {
        WebUtils.messageSource = messageSource;
        WebUtils.localeResolver = localeResolver;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, code, localeResolver.resolveLocale(getRequest()));
    }

    public static boolean isRequiredField(final Object dto, final String fieldName)
            throws NoSuchFieldException, SecurityException {

        return dto.getClass().getDeclaredField(fieldName).getAnnotation(NotNull.class) != null;
    }

    public static String twMerge(final String classes, final String defaultClasses) {
        final String[] classesSplit = classes.split(" ");
        final List<String> utilityClasses = new LinkedList<>();
        final String[] defaultClassesSplit = defaultClasses.split(" ");
        StringBuilder merged = new StringBuilder(classes);
        for (String s : classesSplit) {
            utilityClasses.add(s.split("-")[0]);
        }

        for (int i = defaultClassesSplit.length - 1; i >= 0; i--) {
            if (!utilityClasses.contains(defaultClassesSplit[i].split("-")[0])) {
                merged.insert(0, defaultClassesSplit[i] + " ");
            }
        }

        LOGGER.info("twMerge ('{}', '{}'): {}", classes, defaultClasses, merged);
        return merged.toString();
    }

    public static String formatCurrency(final long amount) {
        return "$" + CURRENCY_FORMATTER.format(amount / 100.0);
    }

    public static String formatDate(final LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
}
