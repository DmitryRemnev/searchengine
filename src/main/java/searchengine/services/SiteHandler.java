package searchengine.services;

import org.springframework.beans.factory.annotation.Configurable;
import searchengine.dto.RecursiveTaskDto;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.util.Date;
import java.util.concurrent.ForkJoinPool;

@Configurable
public class SiteHandler implements Runnable {
    SiteRepository siteRepository;
    String url;
    RecursiveTaskDto dto;

    public SiteHandler(RecursiveTaskDto dto) {
        this.siteRepository = dto.getSiteRepository();
        this.url = dto.getUrl();
        this.dto = dto;
    }

    @Override
    public void run() {
        Site site = createSite();

        try {
            RecursiveTaskDto paramDto = RecursiveTaskDto.builder()
                    .siteRepository(siteRepository)
                    .pageRepository(dto.getPageRepository())
                    .site(site)
                    .build();
            new ForkJoinPool().invoke(new SiteRecursiveTask(new PageHandler(paramDto, url), paramDto));
            setIndexedStatus(site);
        } catch (Exception e) {
            setFailedStatus(site, e.getMessage());
        }
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
