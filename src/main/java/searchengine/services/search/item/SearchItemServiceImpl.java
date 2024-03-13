package searchengine.services.search.item;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchItemDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.repositories.IndexRepository;
import searchengine.services.search.snippet.SearchSnippetService;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchItemServiceImpl implements SearchItemService {
    private final IndexRepository indexRepository;
    private final SearchSnippetService snippetService;

    public List<SearchItemDto> createSearchItemList(List<Page> pageList, List<Lemma> lemmaList, String query) {
        List<SearchItemDto> itemDtoList = new ArrayList<>();

        Map<Page, Float> absoluteRelevanceMap = new HashMap<>();
        for (Page page : pageList) {
            absoluteRelevanceMap.put(page, calculateAbsoluteRelevance(page, lemmaList));
        }
        Float maxAbsoluteRelevance = calculateMaxAbsoluteRelevance(absoluteRelevanceMap);

        for (Map.Entry<Page, Float> entry : absoluteRelevanceMap.entrySet()) {
            float relevance = calculateRelevance(entry.getValue(), maxAbsoluteRelevance);
            Page page = entry.getKey();

            String snippet = snippetService.createSnippet(page, query, lemmaList);
            if (snippet == null) {
                continue;
            }

            SearchItemDto itemDto = createItemDto(page, relevance, snippet);

            itemDtoList.add(itemDto);
        }
        itemDtoList.sort(Comparator.comparing(SearchItemDto::getRelevance).reversed());

        return itemDtoList;
    }

    private SearchItemDto createItemDto(Page page, Float relevance, String snippet) {
        return SearchItemDto.builder()
                .site(page.getSite().getUrl())
                .siteName(page.getSite().getName())
                .uri(page.getPath())
                .title(extractTitle(page))
                .snippet(snippet)
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

    private float calculateRelevance(Float absoluteRelevance, Float maxAbsoluteRelevance) {
        return absoluteRelevance / maxAbsoluteRelevance;
    }
}
