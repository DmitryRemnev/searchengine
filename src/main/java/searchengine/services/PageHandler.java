package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.dto.RecursiveTaskDto;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class PageHandler {
    public static final String AGENT = "SearchBot";
    public static final String REFERRER = "http://www.google.com";
    public static final String CSS_QUERY = "a[href]";
    public static final String ATTRIBUTE_KEY = "abs:href";
    private final Site site;
    private final String url;
    private final List<String> urls = new ArrayList<>();
    private Document document;
    private Connection connect;
    SiteRepository siteRepository;
    PageRepository pageRepository;

    public PageHandler(RecursiveTaskDto dto, String url) {
        this.site = dto.getSite();
        this.url = url;
        this.siteRepository = dto.getSiteRepository();
        this.pageRepository = dto.getPageRepository();
    }

    public List<String> getUrls() {
        addToDataBase();
        return urls;
    }

    private void addToDataBase() {
        try {
            connect = Jsoup.connect(url)
                    .maxBodySize(0)
                    .userAgent(AGENT)
                    .referrer(REFERRER)
                    .ignoreHttpErrors(true);

            document = connect.ignoreContentType(true).get();

            addLine();
            updateTime(site);
            sortElement();

        } catch (Exception e) {
            addLine();
            updateTime(site);
            e.printStackTrace();
        }
    }

    private void addLine() {
        var page = new Page();
        page.setSite(site);
        page.setPath(url);
        page.setCode(connect.response().statusCode());
        page.setContent(document.html());
        pageRepository.save(page);
    }

    private void updateTime(Site site) {
        site.setStatusTime(new Date());
        siteRepository.save(site);
    }

    private void sortElement() {
        Elements elements = document.select(CSS_QUERY);
        elements.forEach(element -> {
            String link = element.attr(ATTRIBUTE_KEY);

            if (isAdd(link)) {
                urls.add(link);
            }
        });
    }

    private boolean isAdd(String link) {
        return link.contains(url) && !link.equals(url);
    }
}
