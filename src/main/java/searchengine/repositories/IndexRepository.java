package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;

@Repository
public interface IndexRepository extends CrudRepository<Index, Integer> {
    Index findByPageAndLemma(Page page, Lemma lemma);
}
