package searchengine.services.lemma;

import java.util.Map;

public interface LemmaService {
    Map<String, Integer> collectLemmas(String text);
}
