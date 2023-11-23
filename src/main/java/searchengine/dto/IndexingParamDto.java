package searchengine.dto;

import lombok.Builder;
import lombok.Data;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.content.ContentService;

@Data
@Builder
public class IndexingParamDto {
    SiteRepository siteRepository;
    PageRepository pageRepository;
    String url;
    String name;
    Site site;
    ContentService contentService;
}
