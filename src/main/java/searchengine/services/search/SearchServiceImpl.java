package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.constant.Constants;
import searchengine.dto.search.SearchItemDto;
import searchengine.dto.search.SearchResponse;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.lemma.LemmaService;
import searchengine.services.search.item.SearchItemService;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final LemmaService lemmaService;
    private final SiteRepository siteRepository;
    private final LemmaRepository lemmaRepository;
    private final PageRepository pageRepository;
    private final SearchItemService searchItemService;

    @Override
    public SearchResponse search(String query, String url) {
        if (query.isBlank()) {
            return createBadResponse(Constants.EMPTY_QUERY);
        }

        Set<String> searchLemmaSet = lemmaService.getLemmaSet(query);

        List<Lemma> lemmaList = lemmaRepository.findLemmaList(searchLemmaSet);
        if (lemmaList.isEmpty()) {
            return createBadResponse(Constants.NOT_FOUND_LEMMA);
        }

        if (url != null) {
            Site site = siteRepository.findByUrl(url);
            filteringLemmasBySite(lemmaList, site);
        }
        long countQueryWords = countingQueryWords(query);

        List<SearchItemDto> itemDtoList = new ArrayList<>();

        Map<Site, List<Lemma>> siteListMap = lemmaList.stream().collect(groupingBy(Lemma::getSite));
        for (Map.Entry<Site, List<Lemma>> entry : siteListMap.entrySet()) {

            List<Lemma> lemmas = entry.getValue();
            if (lemmas.size() < countQueryWords) {
                continue;
            }

            lemmas.sort(Comparator.comparing(Lemma::getFrequency));
            Lemma rarestLemma = lemmas.get(0);
            List<Page> pageList = pageRepository.getByTheRarestLemmaAndSite(rarestLemma, entry.getKey());

            if (lemmaList.size() > 1) {
                filteringPages(lemmas, pageList);
            }

            itemDtoList.addAll(searchItemService.createSearchItemList(pageList, lemmas, query));
        }

        return createGoodResponse(itemDtoList);
    }

    private void filteringLemmasBySite(List<Lemma> lemmaList, Site site) {
        lemmaList.removeIf(lemma -> !lemma.getSite().equals(site));
    }

    private long countingQueryWords(String query) {
        return Arrays.stream(query.split("\\s")).count();
    }

    private void filteringPages(List<Lemma> lemmaList, List<Page> pageList) {
        lemmaList.remove(0); // Remove the first lemma from the list, since filtering by it does not make sense

        for (Lemma lemma : lemmaList) {
            for (Iterator<Page> pageIterator = pageList.iterator(); pageIterator.hasNext(); ) {
                List<Lemma> list = pageIterator.next()
                        .getIndexSet()
                        .stream()
                        .map(Index::getLemma)
                        .toList();

                if (!list.contains(lemma)) {
                    pageIterator.remove();
                }
            }
        }
    }

    private SearchResponse createBadResponse(String error) {
        return SearchResponse.builder()
                .result(false)
                .error(error)
                .build();
    }

    private SearchResponse createGoodResponse(List<SearchItemDto> itemDtoList) {
        return SearchResponse.builder()
                .result(true)
                .count(itemDtoList.size())
                .data(itemDtoList)
                .build();
    }
}
