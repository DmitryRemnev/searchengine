package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.RecursiveTaskDto;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final UtilityService utilityService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final List<SiteHandler> siteHandlerList = new ArrayList<>();

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

            var handler = new SiteHandler(dto);
            siteHandlerList.add(handler);

            new Thread(handler).start();
        }

        return new IndexingResponse(true, null);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (utilityService.isIndexingRun()) {
            for (SiteHandler handler : siteHandlerList) {
                handler.stop();
            }

            return new IndexingResponse(true, null);
        }
        return new IndexingResponse(false, "Индексация не запущена");
    }
}
