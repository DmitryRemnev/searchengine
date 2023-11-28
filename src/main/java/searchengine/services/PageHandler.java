package searchengine.services;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.constant.Constants;
import searchengine.dto.IndexingParamDto;
import searchengine.dto.RecursiveDataDto;
import searchengine.model.Page;

import java.util.ArrayList;
import java.util.List;

public class PageHandler {
    private final String url;
    private final IndexingParamDto dto;
    private final List<String> urlList = new ArrayList<>();
    private Document document;
    private Connection connect;

    public PageHandler(IndexingParamDto dto, String url) {
        this.url = url;
        this.dto = dto;
    }

    public RecursiveDataDto extractRecursiveData() {
        parsingUrl();
        fillingUrlList();
        return RecursiveDataDto.builder()
                .page(createPage(dto))
                .urlList(urlList)
                .build();
    }

    public void parsingUrl() {
        try {
            connect = Jsoup.connect(url)
                    .maxBodySize(0)
                    .userAgent(Constants.AGENT)
                    .referrer(Constants.REFERRER)
                    .ignoreHttpErrors(true);
            document = connect.ignoreContentType(true).get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillingUrlList() {
        Elements elements = document.select(Constants.CSS_QUERY);
        elements.forEach(element -> {
            String link = element.attr(Constants.ATTRIBUTE_KEY);

            if (isAdd(link)) {
                urlList.add(link);
            }
        });
    }

    public Page createPage(IndexingParamDto dto) {
        var page = new Page();
        page.setSite(dto.getSite());
        page.setPath(url);
        page.setCode(connect.response().statusCode());
        page.setContent(document.html());
        return page;
    }

    private boolean isAdd(String link) {
        return link.contains(url) && !link.equals(url);
    }
}
