package searchengine.services.single;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.dto.RecursiveTaskDto;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.services.PageHandler;
import searchengine.services.content.ContentService;
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
    private final ContentService contentService;

    @Override
    @Transactional
    public void indexingSinglePage(Page page) {
        RecursiveTaskDto dto = createDto(page);
        deletePageData(page);
        var pageHandler = new PageHandler(dto, dto.getUrl());
        pageHandler.addToDataBase(dto);
        contentProcessing(dto);
    }

    private RecursiveTaskDto createDto(Page page) {
        return RecursiveTaskDto.builder()
                .url(page.getPath())
                .site(page.getSite())
                .siteRepository(siteRepository)
                .pageRepository(pageRepository)
                .build();
    }

    private void deletePageData(Page page) {
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

    private void contentProcessing(RecursiveTaskDto dto) {
        Page page = pageRepository.findByPath(dto.getUrl());
        Site site = page.getSite();
        Map<String, Integer> map = lemmaService.collectLemmas(page.getContent());
        map.forEach((key, value) -> contentService.save(site, page, key, value));
    }
}
