package com.andy.iamapi.infrastructure.adapter.rest.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * DTO genérico para respuestas paginadas.
 *
 * Envuelve una lista de elementos con metadata de paginación.
 * Compatible con cualquier tipo de elemento (User, Role, etc.)
 *
 * @param <T> Tipo de los elementos en la página
 */
public record PageResponse<T>(
        List<T> content, // Lista de elementos de la página actual
        PageMetadata page // Metadata de paginación
) {
    /**
     * Metadata de paginación.
     *
     * Información sobre el estado actual de la paginación:
     * - size: Elementos por página solicitados
     * - number: Número de página actual (0-indexed)
     * - totalElements: Total de elementos en todas las páginas
     * - totalPages: Total de páginas disponibles
     */
    public record PageMetadata (
            int size, // Tamaño de página (elementos por página)
            int number, // Número de página actual (0-indexed)
            long totalElements, // Total de elementos en la BD
            int totalPages // Total de páginas
    ) {}

    /**
     * Factory method para crear PageResponse desde Spring Data Page.
     *
     * Convierte un objeto Page<T> de Spring Data a nuestro DTO.
     * Útil para mantener la separación entre la capa de infraestructura
     * y la API REST.
     *
     * @param page Objeto Page de Spring Data
     * @param <T> Tipo de los elementos
     * @return PageResponse con los datos de la página
     */
    public static <T> PageResponse<T> fromPage(Page<T> page) {
        return new PageResponse<T>(
                page.getContent(),
                new PageMetadata(
                        page.getSize(),
                        page.getNumber(),
                        page.getTotalElements(),
                        page.getTotalPages()
                )
        );
    }
}
