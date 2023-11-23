package searchengine.dto.search;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResponse {
    private boolean result;
    private int count;
    private List<SearchItemDto> data;
    private String error;
}
