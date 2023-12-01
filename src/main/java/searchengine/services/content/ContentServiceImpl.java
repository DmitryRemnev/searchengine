package searchengine.services.content;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.services.lemma.LemmaService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ContentServiceImpl implements ContentService {
    private final PageRepository pageRepository;
    private final LemmaService lemmaService;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @Override
    public void contentProcessing(Site site) {
        List<Page> pageList = pageRepository.getOkContent(site);
        List<Index> indexList = new ArrayList<>();

        for (Page page : pageList) {
            String content = page.getContent();
            Map<String, Integer> map = lemmaService.collectLemmas(content);

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                Lemma lemma = createOrUpdateLemma(site, entry.getKey());
                Index index = createIndex(page, lemma, entry.getValue());
                indexList.add(index);
            }
        }

        indexRepository.saveAll(indexList);
    }

    private Lemma createOrUpdateLemma(Site site, String lemmaString) {
        Lemma lemmaEntity = lemmaRepository.findByLemmaAndSite(lemmaString, site);
        if (lemmaEntity != null) {
            lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
        } else {
            lemmaEntity = new Lemma();
            lemmaEntity.setSite(site);
            lemmaEntity.setLemma(lemmaString);
            lemmaEntity.setFrequency(1);
        }
        return lemmaRepository.save(lemmaEntity);
    }

    private Index createIndex(Page page, Lemma lemma, Integer rating) {
        Index index = new Index();
        index.setPage(page);
        index.setLemma(lemma);
        index.setRating(Float.valueOf(rating));
        return index;
    }
}
