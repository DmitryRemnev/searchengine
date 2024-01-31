package searchengine.services.single;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.IndexingParamDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.PageHandler;
import searchengine.services.lemma.LemmaService;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PageSingleServiceImpl implements PageSingleService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private final LemmaRepository lemmaRepository;
    private final LemmaService lemmaService;

    @Override
    @Transactional
    public void indexingSinglePage(Page oldPpage) {
        IndexingParamDto dto = createDto(oldPpage);
        deletePldPageData(oldPpage);
        Page newPage = createNewPage(dto);
        contentProcessing(newPage);
    }

    private IndexingParamDto createDto(Page page) {
        return IndexingParamDto.builder()
                .url(page.getSite().getUrl() + page.getPath())
                .site(page.getSite())
                .siteRepository(siteRepository)
                .pageRepository(pageRepository)
                .build();
    }

    private void deletePldPageData(Page page) {
        Set<Index> indexSet = page.getIndexSet();
        List<Lemma> lemmaList = indexSet.stream()
                .map(Index::getLemma)
                .toList();
        indexRepository.deleteAll(indexSet);

        lemmaList.forEach(lemma -> {
            if (lemma.getFrequency() == 1) {
                lemmaRepository.delete(lemma);
            } else {
                lemma.setFrequency(lemma.getFrequency() - 1);
                lemmaRepository.save(lemma);
            }
        });

        pageRepository.delete(page);
    }

    private Page createNewPage(IndexingParamDto dto) {
        var pageHandler = new PageHandler(dto, dto.getUrl());
        pageHandler.parsingUrl();
        var newPage = pageHandler.createPage(dto);
        return pageRepository.save(newPage);
    }

    private void contentProcessing(Page page) {
        Site site = page.getSite();
        Map<String, Integer> map = lemmaService.collectLemmas(page.getContent());
        map.forEach((key, value) -> save(site, page, key, value));
    }

    private void save(Site site, Page page, String lemmaString, Integer rating) {
        Lemma lemmaEntity = lemmaRepository.findByLemmaAndSite(lemmaString, site);
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
