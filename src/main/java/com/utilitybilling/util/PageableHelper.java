package com.utilitybilling.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Builds {@link Pageable} from a single 1-based page number (Swagger-friendly).
 */
public final class PageableHelper {

    public static final int DEFAULT_SIZE = 20;
    public static final int AUDIT_SIZE = 50;

    private PageableHelper() {}

    public static Pageable ofPage(int page, String sortField, Sort.Direction direction) {
        return ofPage(page, DEFAULT_SIZE, sortField, direction);
    }

    public static Pageable ofPage(int page, int size, String sortField, Sort.Direction direction) {
        return PageRequest.of(Math.max(0, page - 1), size, Sort.by(direction, sortField));
    }
}
