package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.RecursiveTaskDto;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final UtilityService utilityService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    @Override
    public IndexingResponse startIndexing() {
        for (Site site : sites.getSites()) {
            if (utilityService.isIndexingNow(site.getName())) {
                return new IndexingResponse(false, "Индексация уже запущена");
            }

            RecursiveTaskDto dto = RecursiveTaskDto.builder()
                    .siteRepository(siteRepository)
                    .pageRepository(pageRepository)
                    .url(site.getUrl())
                    .name(site.getName())
                    .build();
            new Thread(new SiteHandler(dto)).start();
        }

        return new IndexingResponse(true, null);
    }
}
