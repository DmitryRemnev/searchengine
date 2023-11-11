package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

import java.util.List;

@Repository
public interface SiteRepository extends CrudRepository<Site, Integer> {
    Site findByName(String name);

    void deleteByName(String name);

    @Query("SELECT s FROM site s")
    List<Site> getAll();
}
