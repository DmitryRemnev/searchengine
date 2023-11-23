package searchengine.services.lemma;

import java.util.Map;
import java.util.Set;

public interface LemmaService {
    Map<String, Integer> collectLemmas(String text);

    Set<String> getLemmaSet(String text);
}
