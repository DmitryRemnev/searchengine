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

    Lemma findByLemma(String lemma);

    @Query("SELECT l FROM lemma l WHERE l.lemma IN :lemmaSet AND l.frequency < 60 ORDER BY l.frequency")
    List<Lemma> getOrderingLemmaList(Set<String> lemmaSet);

    @Query("SELECT l FROM lemma l WHERE l.site = :site AND l.lemma IN :lemmaSet AND l.frequency < 60 ORDER BY l.frequency")
    List<Lemma> getOrderingLemmaListBySite(Set<String> lemmaSet, Site site);
}
