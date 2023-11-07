package searchengine.services;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.RecursiveTaskDto;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.util.Date;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@Configurable
public class SiteHandler implements Runnable {
    SiteRepository siteRepository;
    String url;
    RecursiveTaskDto dto;
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Site site;

    public SiteHandler(RecursiveTaskDto dto) {
        this.siteRepository = dto.getSiteRepository();
        this.url = dto.getUrl();
        this.dto = dto;
    }

    @Override
    @Transactional
    public void run() {
        site = createSite();
        try {
            RecursiveTaskDto paramDto = RecursiveTaskDto.builder()
                    .siteRepository(siteRepository)
                    .pageRepository(dto.getPageRepository())
                    .site(site)
                    .build();
            forkJoinPool.invoke(new SiteRecursiveTask(new PageHandler(paramDto, url), paramDto));
            setIndexedStatus(site);
        } catch (CancellationException e) {
            setFailedStatus(site, "Индексация остановлена пользователем");
        } catch (Exception e) {
            setFailedStatus(site, e.getMessage());
        }
    }

    public void stop() {
        forkJoinPool.shutdownNow();
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
}
