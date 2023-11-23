package searchengine.services.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
            return createBadResponse("Задан пустой поисковый запрос");
        }

        Set<String> searchLemmaSet = lemmaService.getLemmaSet(query);
        List<Lemma> orderingLemmaList;
        List<Page> pageList;

        if (url == null) {
            orderingLemmaList = lemmaRepository.getOrderingLemmaList(searchLemmaSet);
            if (orderingLemmaList.isEmpty()) {
                return createBadResponse("Не найдено ни одной леммы");
            }
            Lemma rarestLemma = orderingLemmaList.get(0);
            pageList = pageRepository.getByTheRarestLemma(rarestLemma);

        } else {
            Site site = siteRepository.findByUrl(url);
            orderingLemmaList = lemmaRepository.getOrderingLemmaListBySite(searchLemmaSet, site);
            if (orderingLemmaList.isEmpty()) {
                return createBadResponse("Не найдено ни одной леммы");
            }
            Lemma rarestLemma = orderingLemmaList.get(0);
            pageList = pageRepository.getByTheRarestLemmaAndSite(rarestLemma, site);
        }

        if (orderingLemmaList.size() > 1) {
            filteringPages(orderingLemmaList, pageList);
        }

        List<SearchItemDto> itemDtoList = searchItemService.createSearchItemList(pageList, orderingLemmaList, query);
        return createGoodResponse(itemDtoList);
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
