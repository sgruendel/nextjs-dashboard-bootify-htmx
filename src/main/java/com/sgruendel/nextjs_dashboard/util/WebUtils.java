package com.sgruendel.nextjs_dashboard.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.SneakyThrows;

import java.util.List;
import java.text.DecimalFormat;
import java.util.LinkedList;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;


@Component
public class WebUtils {

    public static final String MSG_SUCCESS = "MSG_SUCCESS";
    public static final String MSG_INFO = "MSG_INFO";
    public static final String MSG_ERROR = "MSG_ERROR";

    private static final DecimalFormat formatter = new DecimalFormat("#,##0.00");

    private static MessageSource messageSource;
    private static LocaleResolver localeResolver;

    public WebUtils(final MessageSource messageSource, final LocaleResolver localeResolver) {
        WebUtils.messageSource = messageSource;
        WebUtils.localeResolver = localeResolver;
    }

    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
    }

    public static String getMessage(final String code, final Object... args) {
        return messageSource.getMessage(code, args, code, localeResolver.resolveLocale(getRequest()));
    }

    @SneakyThrows
    public static boolean isRequiredField(final Object dto, final String fieldName) {
        return dto.getClass().getDeclaredField(fieldName).getAnnotation(NotNull.class) != null;
    }

    public static String twMerge(final String classes, final String defaultClasses) {
        final String[] classesSplit = classes.split(" ");
        final List<String> utilityClasses = new LinkedList<>();
        final String[] defaultClassesSplit = defaultClasses.split(" ");
        String merged = classes;
        for (int i = 0; i < classesSplit.length; i++) {
            utilityClasses.add(classesSplit[i].split("-")[0]);
        }

        for (int i = defaultClassesSplit.length - 1; i >= 0; i--) {
            if (!utilityClasses.contains(defaultClassesSplit[i].split("-")[0])) {
                merged = defaultClassesSplit[i] + " " + merged;
            }
        }

        return merged;
    }

    public static String formatCurrency(final int amount) {
        return "$" + formatter.format(amount / 100.0);
    }

}
