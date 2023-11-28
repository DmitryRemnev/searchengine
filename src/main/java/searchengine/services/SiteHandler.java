package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import searchengine.constant.Constants;
import searchengine.dto.IndexingParamDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

@Slf4j
public class SiteHandler implements Runnable {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final String url;
    private final IndexingParamDto dto;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public SiteHandler(IndexingParamDto dto) {
        this.siteRepository = dto.getSiteRepository();
        this.pageRepository = dto.getPageRepository();
        this.url = dto.getUrl();
        this.dto = dto;
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        Site site = createSite();
        try {
            IndexingParamDto paramDto = createDto(site);
            var pageHandler = new PageHandler(paramDto, url);
            var siteRecursiveTask = new SiteRecursiveTask(pageHandler, paramDto);
            List<Page> pageList = forkJoinPool.invoke(siteRecursiveTask);
            pageRepository.saveAll(pageList);
            contentProcessing(site);
            setIndexedStatus(site);

        } catch (CancellationException e) {
            setFailedStatus(site, Constants.CANCEL);

        } catch (Exception e) {
            setFailedStatus(site, e.getMessage());
        }

        long end = System.currentTimeMillis();
        log.info(dto.getName() + " : " + (end - start) + " mill");
    }

    public void stop() {
        forkJoinPool.shutdownNow();
    }

    private IndexingParamDto createDto(Site site) {
        return IndexingParamDto.builder()
                .siteRepository(siteRepository)
                .pageRepository(dto.getPageRepository())
                .site(site)
                .build();
    }

    private Site createSite() {
        var site = new Site();
        site.setStatus(Status.INDEXING);
        site.setStatusTime(new Date());
        site.setUrl(dto.getUrl());
        site.setName(dto.getName());
        return siteRepository.save(site);
    }

    private void setFailedStatus(Site site, String error) {
        site.setStatus(Status.FAILED);
        site.setStatusTime(new Date());
        site.setLastError(error);
        siteRepository.save(site);
    }

    private void setIndexedStatus(Site site) {
        site.setStatus(Status.INDEXED);
        site.setStatusTime(new Date());
        siteRepository.save(site);
    }

    private void contentProcessing(Site site) {
        dto.getContentService().contentProcessing(site);
    }
}
