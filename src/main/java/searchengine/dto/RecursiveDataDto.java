package searchengine.dto;

import lombok.Builder;
import lombok.Data;
import searchengine.model.Page;

import java.util.List;

@Data
@Builder
public class RecursiveDataDto {
    Page page;
    List<String> urlList;
}
