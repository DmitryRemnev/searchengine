package searchengine.services.utility;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

@Service
@RequiredArgsConstructor
public class UtilityServiceImpl implements UtilityService {
    private final SiteRepository siteRepository;

    @Override
    @Transactional
    public boolean isIndexingNow(String name) {
        Site site = siteRepository.findByName(name);
        if (site == null) return false;

        if (site.getStatus().equals(Status.INDEXED) || site.getStatus().equals(Status.FAILED)) {
            deleteAllData(name);
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public boolean isIndexingRun() {
        Iterable<Site> siteList = siteRepository.findAll();
        for (Site site : siteList) {
            if (site.getStatus().equals(Status.INDEXING)) {
                return true;
            }
        }
        return false;
    }

    private void deleteAllData(String name) {
        siteRepository.deleteByName(name);
    }
}
