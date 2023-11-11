package searchengine.services.content;

import searchengine.model.Page;
import searchengine.model.Site;

public interface ContentService {
    void contentProcessing(Site site);

    void save(Site site, Page page, String lemmaString, Integer rating);
}
