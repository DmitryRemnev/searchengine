package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;
import java.util.Set;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma, Integer> {

    Lemma findByLemmaAndSite(String lemma, Site site);

    @Query("SELECT l FROM lemma l WHERE l.lemma IN :lemmaSet")
    List<Lemma> findLemmaList(Set<String> lemmaSet);

    @Query("SELECT COUNT(l) FROM lemma l WHERE l.site = :site")
    long countBySite(Site site);
}
