package searchengine.services.statistics;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(getTotal());
        data.setDetailed(getDetailed());
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    private TotalStatistics getTotal() {
        TotalStatistics total = new TotalStatistics();
        total.setSites(siteRepository.count());
        total.setPages(pageRepository.count());
        total.setLemmas(lemmaRepository.count());
        total.setIndexing(isIndexing());
        return total;
    }

    private boolean isIndexing() {
        List<Site> siteList = siteRepository.getAll();
        return siteList.stream()
                .anyMatch(site -> site.getStatus().equals(Status.INDEXING));
    }

    private List<DetailedStatisticsItem> getDetailed() {
        List<DetailedStatisticsItem> detailedList = new ArrayList<>();

        List<Site> siteList = siteRepository.getAll();
        siteList.forEach(site -> {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setUrl(site.getUrl());
            item.setName(site.getName());
            item.setStatus(site.getStatus().getName());
            item.setStatusTime(site.getStatusTime().getTime());
            item.setPages(site.getPageList().size());
            item.setLemmas(lemmaRepository.countBySite(site));
            if (site.getStatus().equals(Status.FAILED)) {
                item.setError(site.getLastError());
            }
            detailedList.add(item);
        });

        return detailedList;
    }
}
