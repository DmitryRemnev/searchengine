package searchengine.services.search.item;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchItemDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchItemServiceImpl implements SearchItemService {
    private final IndexRepository indexRepository;

    public List<SearchItemDto> createSearchItemList(List<Page> pageList, List<Lemma> lemmaList, String query) {
        List<SearchItemDto> itemDtoList = new ArrayList<>();

        Map<Page, Float> absoluteRelevanceMap = new HashMap<>();
        for (Page page : pageList) {
            absoluteRelevanceMap.put(page, calculateAbsoluteRelevance(page, lemmaList));
        }
        Float maxAbsoluteRelevance = calculateMaxAbsoluteRelevance(absoluteRelevanceMap);

        for (Map.Entry<Page, Float> entry : absoluteRelevanceMap.entrySet()) {
            float relevance = calculateRelevance(entry.getValue(), maxAbsoluteRelevance);
            SearchItemDto itemDto = createItemDto(entry.getKey(), relevance, query);

            itemDtoList.add(itemDto);
        }
        itemDtoList.sort(Comparator.comparing(SearchItemDto::getRelevance).reversed());

        return itemDtoList;
    }

    private SearchItemDto createItemDto(Page page, Float relevance, String query) {
        return SearchItemDto.builder()
                .site(page.getSite().getUrl())
                .siteName(page.getSite().getName())
                .uri(page.getPath())
                .title(extractTitle(page))
                .snippet(createSnippet(page, query))
                .relevance(relevance)
                .build();
    }

    private String extractTitle(Page page) {
        String content = page.getContent();
        Document document = Jsoup.parse(content);
        return document.title();
    }

    private Float calculateAbsoluteRelevance(Page page, List<Lemma> lemmaList) {
        return lemmaList.stream()
                .map(lemma -> indexRepository.findByPageAndLemma(page, lemma))
                .map(Index::getRating)
                .reduce(0.0f, Float::sum);
    }

    private Float calculateMaxAbsoluteRelevance(Map<Page, Float> map) {
        return map.values().stream().reduce(0.0f, Float::max);
    }

    private String createSnippet(Page page, String query) {
        Elements elements = extractElements(page);

        for (Element element : elements) {
            if (element.hasText()) {
                if (element.text().contains(query)) {
                    return extractSentence(element, query);
                }
            }
        }

        return null;
    }

    private Elements extractElements(Page page) {
        String content = page.getContent();
        Document document = Jsoup.parse(content);
        return document.getAllElements();
    }

    private String extractSentence(Element element, String query) {
        List<String> sentences = Arrays
                .stream(element.text().split("\\."))
                .toList();

        for (String sentence : sentences) {
            if (sentence.toLowerCase().contains(query.toLowerCase())) {
                return markQuery(sentence, query);
            }
        }

        return null;
    }

    private String markQuery(String sentence, String query) {
        return sentence.replace(query, "<mark>" + query + "</mark>");
    }

    private float calculateRelevance(Float absoluteRelevance, Float maxAbsoluteRelevance) {
        return absoluteRelevance / maxAbsoluteRelevance;
    }
}
