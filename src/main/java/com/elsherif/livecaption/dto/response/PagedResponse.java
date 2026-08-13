package com.elsherif.livecaption.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private Integer page;
    private Integer totalPages;
    private Long totalElements;

    public Boolean getHasNext() {
        return page != null && totalPages != null && page < totalPages;
    }

    public Boolean getHasPrevious() {
        return page != null && page > 1;
    }
}
