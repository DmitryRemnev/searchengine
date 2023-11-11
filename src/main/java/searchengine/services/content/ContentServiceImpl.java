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
        for (Page page : pageList) {
            String content = page.getContent();
            Map<String, Integer> map = lemmaService.collectLemmas(content);
            map.forEach((key, value) -> save(site, page, key, value));
        }
    }

    @Override
    public void save(Site site, Page page, String lemmaString, Integer rating) {
        Lemma lemmaEntity = lemmaRepository.findByLemma(lemmaString);
        if (lemmaEntity != null) {
            lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1);
        } else {
            lemmaEntity = new Lemma();
            lemmaEntity.setSite(site);
            lemmaEntity.setLemma(lemmaString);
            lemmaEntity.setFrequency(1);
        }
        lemmaEntity = lemmaRepository.save(lemmaEntity);

        Index index = new Index();
        index.setPage(page);
        index.setLemma(lemmaEntity);
        index.setRating(Float.valueOf(rating));
        indexRepository.save(index);
    }
}
