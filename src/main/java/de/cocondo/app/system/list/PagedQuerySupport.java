package de.cocondo.app.system.list;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Generic technical support for paged list endpoints.
 *
 * Responsibilities:
 * - validate paging arguments
 * - resolve sort direction consistently
 *
 * Important:
 * - Contains no domain-specific sorting keys
 * - Contains no DTO or repository logic
 */
@Component
public class PagedQuerySupport {

    public void validatePaging(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        if (size > 200) {
            throw new IllegalArgumentException("size must be <= 200");
        }
    }

    public Sort.Direction resolveSortDirection(String sortDir) {
        if (sortDir == null || sortDir.isBlank()) {
            return Sort.Direction.ASC;
        }

        return switch (sortDir.toLowerCase(Locale.ROOT)) {
            case "asc" -> Sort.Direction.ASC;
            case "desc" -> Sort.Direction.DESC;
            default -> throw new IllegalArgumentException("Unsupported sortDir: " + sortDir);
        };
    }
}