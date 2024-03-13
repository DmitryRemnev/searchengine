package searchengine.services.search.snippet;

import searchengine.model.Lemma;
import searchengine.model.Page;

import java.util.List;

public interface SearchSnippetService {

    String createSnippet(Page page, String query, List<Lemma> lemmaList);
}
