package de.cocondo.app.system.dto;

import lombok.Data;

import java.util.List;

/**
 * Generic paged API response wrapper.
 *
 * Purpose:
 * - stable UI-facing list contract
 * - avoids leaking Spring Page internals into REST contracts
 * - reusable across domains
 */
@Data
public class PagedResponseDTO<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}