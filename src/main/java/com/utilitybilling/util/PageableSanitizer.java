package com.utilitybilling.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates and normalizes Spring Data {@link Pageable} sort parameters.
 * Prevents invalid Swagger/client sort values (e.g. sort=["user"]) from breaking queries.
 */
public final class PageableSanitizer {

    private static final Map<String, String> USER_ALIASES = Map.of(
            "fullName", "fullNames",
            "phone", "phoneNumber"
    );

    private static final Set<String> USER_SORT_FIELDS = Set.of(
            "id", "firstName", "lastName", "fullNames", "email",
            "phoneCountryCode", "phoneNumber", "status",
            "passwordExpired", "accountLocked", "createdAt", "updatedAt"
    );

    private PageableSanitizer() {}

    public static Pageable forUser(Pageable pageable) {
        return sanitize(pageable, USER_SORT_FIELDS, USER_ALIASES, "id", Sort.Direction.ASC);
    }

    public static Pageable sanitize(Pageable pageable,
                                    Set<String> allowedFields,
                                    Map<String, String> aliases,
                                    String defaultField,
                                    Sort.Direction defaultDirection) {
        List<Sort.Order> orders = new ArrayList<>();

        if (pageable.getSort().isSorted()) {
            for (Sort.Order order : pageable.getSort()) {
                String property = normalizeProperty(order.getProperty());
                if (property.isBlank()) {
                    continue;
                }
                if (aliases != null && aliases.containsKey(property)) {
                    property = aliases.get(property);
                }
                if (allowedFields.contains(property)) {
                    orders.add(new Sort.Order(order.getDirection(), property));
                }
            }
        }

        Sort sort = orders.isEmpty()
                ? Sort.by(defaultDirection, defaultField)
                : Sort.by(orders);

        int page = Math.max(0, pageable.getPageNumber());
        int size = pageable.getPageSize() <= 0 ? 20 : Math.min(pageable.getPageSize(), 100);

        return PageRequest.of(page, size, sort);
    }

    private static String normalizeProperty(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("'", "");
        // Swagger sometimes sends the literal token "user" for unrelated endpoints
        if ("user".equalsIgnoreCase(cleaned)) {
            return "";
        }
        return cleaned;
    }
}
