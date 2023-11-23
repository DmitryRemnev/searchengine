package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface PageRepository extends CrudRepository<Page, Integer> {
    @Query("SELECT p FROM page p WHERE p.code = 200 and p.site =:site")
    List<Page> getOkContent(Site site);

    Page findByPath(String path);

    @Query("SELECT p FROM page p INNER JOIN p.indexSet i INNER JOIN i.lemma l WHERE l = :lemma")
    List<Page> getByTheRarestLemma(Lemma lemma);

    @Query("SELECT p FROM page p INNER JOIN p.indexSet i INNER JOIN i.lemma l WHERE l = :lemma AND l.site = :site")
    List<Page> getByTheRarestLemmaAndSite(Lemma lemma, Site site);
}
