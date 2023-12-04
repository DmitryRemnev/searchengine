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

import java.util.*;

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
        Map<Lemma, Integer> lemmaIntegerMap = new HashMap<>();

        for (Page page : pageList) {
            String content = page.getContent();
            Map<String, Integer> map = lemmaService.collectLemmas(content);

            for (Map.Entry<String, Integer> entry : map.entrySet()) {
                Lemma lemma = createLemma(site, entry.getKey());

                if (lemmaIntegerMap.containsKey(lemma)) {
                    lemmaIntegerMap.put(lemma, lemmaIntegerMap.get(lemma) + 1);
                    Lemma finalLemma = lemma;
                    lemma = lemmaIntegerMap.keySet().stream()
                            .filter(i -> i.equals(finalLemma))
                            .findFirst()
                            .orElse(null);
                } else {
                    lemmaIntegerMap.put(lemma, 1);
                }

//                Lemma lemma = createOrUpdateLemma(site, entry.getKey());
                Index index = createIndex(page, lemma, entry.getValue());
                indexList.add(index);
            }
        }

        for (Map.Entry<Lemma, Integer> entry : lemmaIntegerMap.entrySet()) {
            Lemma lemma = entry.getKey();
            lemma.setFrequency(entry.getValue());
        }
        lemmaRepository.saveAll(lemmaIntegerMap.keySet());

        indexRepository.saveAll(indexList);
    }

    private Lemma createLemma(Site site, String lemmaString) {
        Lemma lemma = new Lemma();
        lemma.setSite(site);
        lemma.setLemma(lemmaString);
        return lemma;
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
