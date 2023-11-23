package searchengine.services.search.item;

import searchengine.dto.search.SearchItemDto;
import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

public interface SearchItemService {
    List<SearchItemDto> createSearchItemList(List<Page> pageList, List<Lemma> lemmaList, String query);
}
