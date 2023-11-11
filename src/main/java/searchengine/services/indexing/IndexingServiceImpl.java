package searchengine.services.indexing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.constant.Constants;
import searchengine.dto.RecursiveTaskDto;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.Page;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.SiteHandler;
import searchengine.services.content.ContentService;
import searchengine.services.single.PageSingleService;
import searchengine.services.utility.UtilityService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private final SitesList sites;
    private final UtilityService utilityService;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final ContentService contentService;
    private final PageSingleService pageSingleService;
    private final List<SiteHandler> siteHandlerList = new ArrayList<>();

    @Override
    public IndexingResponse startIndexing() {

        for (Site site : sites.getSites()) {
            if (utilityService.isIndexingNow(site.getName())) {
                return new IndexingResponse(false, Constants.ALREADY_LAUNCHED);
            }

            RecursiveTaskDto dto = createDto(site.getUrl(), site.getName());
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
        return new IndexingResponse(false, Constants.NOT_RUNNING);
    }

    @Override
    public IndexingResponse indexPage(String pageUrl) {
        Page page = pageRepository.findByPath(pageUrl);
        if (page == null) return new IndexingResponse(false, Constants.OUTSIDE);

        pageSingleService.indexingSinglePage(page);
        return new IndexingResponse(true, null);
    }

    private RecursiveTaskDto createDto(String url, String name) {
        return RecursiveTaskDto.builder()
                .siteRepository(siteRepository)
                .pageRepository(pageRepository)
                .url(url)
                .name(name)
                .contentService(contentService)
                .build();
    }
}
