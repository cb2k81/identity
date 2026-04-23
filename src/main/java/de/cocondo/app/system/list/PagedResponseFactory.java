package de.cocondo.app.system.list;

import de.cocondo.app.system.dto.PagedResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Generic factory for converting Spring Data Page results
 * into stable API-facing PagedResponseDTO contracts.
 *
 * Responsibilities:
 * - map page content into DTO items
 * - copy pagination metadata
 *
 * Important:
 * - Mapping function is supplied by the caller
 * - Contains no domain-specific mapping logic
 */
@Component
public class PagedResponseFactory {

    public <S, T> PagedResponseDTO<T> fromPage(Page<S> page, Function<S, T> mapper) {

        PagedResponseDTO<T> response = new PagedResponseDTO<>();
        response.setItems(page.getContent().stream().map(mapper).toList());
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());

        return response;
    }
}