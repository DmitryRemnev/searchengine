package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.constant.Constants;
import searchengine.dto.RecursiveTaskDto;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PageHandler {
    private final String url;
    private final RecursiveTaskDto dto;
    private final List<String> urls = new ArrayList<>();
    private Document document;
    private Connection connect;

    public PageHandler(RecursiveTaskDto dto, String url) {
        this.url = url;
        this.dto = dto;
    }

    public List<String> getUrls() {
        addToDataBase(dto);
        return urls;
    }

    public void addToDataBase(RecursiveTaskDto dto) {
        try {
            connect = Jsoup.connect(url)
                    .maxBodySize(0)
                    .userAgent(Constants.AGENT)
                    .referrer(Constants.REFERRER)
                    .ignoreHttpErrors(true);
            document = connect.ignoreContentType(true).get();

            addLine(dto);
            updateTime(dto);
            sortElement();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addLine(RecursiveTaskDto dto) {
        var page = new Page();
        page.setSite(dto.getSite());
        page.setPath(url);
        page.setCode(connect.response().statusCode());
        page.setContent(document.html());
        dto.getPageRepository().save(page);
    }

    private void updateTime(RecursiveTaskDto dto) {
        Site site = dto.getSite();
        site.setStatusTime(new Date());
        dto.getSiteRepository().save(site);
    }

    private void sortElement() {
        Elements elements = document.select(Constants.CSS_QUERY);
        elements.forEach(element -> {
            String link = element.attr(Constants.ATTRIBUTE_KEY);

            if (isAdd(link)) {
                urls.add(link);
            }
        });
    }

    private boolean isAdd(String link) {
        return link.contains(url) && !link.equals(url);
    }
}
