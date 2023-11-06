package searchengine.dto;

import lombok.Builder;
import lombok.Data;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

@Data
@Builder
public class RecursiveTaskDto {
    SiteRepository siteRepository;
    PageRepository pageRepository;
    String url;
    String name;
    Site site;
}
