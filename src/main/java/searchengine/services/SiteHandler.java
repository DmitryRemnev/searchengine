package searchengine.services;

import searchengine.constant.Constants;
import searchengine.dto.IndexingParamDto;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;

public class SiteHandler implements Runnable {
    private final SiteRepository siteRepository;
    private final String url;
    private final IndexingParamDto dto;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public SiteHandler(IndexingParamDto dto) {
        this.siteRepository = dto.getSiteRepository();
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
            forkJoinPool.invoke(siteRecursiveTask);
            setIndexedStatus(site);
            contentProcessing(site);

            long end = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + " : " + (end - start) + " mill");

        } catch (CancellationException e) {
            setFailedStatus(site, Constants.CANCEL);
            contentProcessing(site);

        } catch (Exception e) {
            setFailedStatus(site, e.getMessage());
            contentProcessing(site);
        }
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
