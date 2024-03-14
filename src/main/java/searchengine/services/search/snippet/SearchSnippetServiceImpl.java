package searchengine.services.search.snippet;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.services.lemma.LemmaService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchSnippetServiceImpl implements SearchSnippetService {
    private final LemmaService lemmaService;

    @Override
    public String createSnippet(Page page, String query, List<Lemma> lemmaList) {
        Elements elements = extractElements(page);
        List<String> queryListWords = createQueryListWords(query);
        List<String> resultList = new ArrayList<>();
        Optional<String> result;

        for (String queryWord : queryListWords) {
            String halfWord = getHalfWord(queryWord.toLowerCase());

            for (Element element : elements) {
                result = findSnippet(element, halfWord, lemmaList);

                if (result.isPresent()) {
                    resultList.add(result.get());
                    break;
                }
            }
        }

        if (resultList.isEmpty() || resultList.size() != queryListWords.size()) {
            return null;
        }

        return concatResultList(resultList);
    }

    private Elements extractElements(Page page) {
        String content = page.getContent();
        Document document = Jsoup.parse(content);
        return document.getAllElements();
    }

    private List<String> createQueryListWords(String query) {
        return Arrays.stream(query.split("\\s")).toList();
    }

    private Optional<String> findSnippet(Element element, String halfWord, List<Lemma> lemmaList) {
        if (element.hasText()) {
            String text = element.text();

            if (isPartialMatch(halfWord, text.toLowerCase())) {
                return findMatch(halfWord, text.toLowerCase(), lemmaList);
            }
        }

        return Optional.empty();
    }

    private List<String> divideByFiveWords(String text) {
        List<String> words = Arrays.stream(text.split(" ")).toList();
        List<String> sentences = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        words.forEach(word -> {
            builder.append(word).append(" ");

            if (builder.toString().split(" ").length == 5) {
                sentences.add(builder.toString().trim());
                builder.delete(0, builder.length());
            }
        });

        if (!builder.isEmpty()) {
            sentences.add(builder.toString().trim());
        }

        return sentences;
    }

    private boolean isPartialMatch(String halfWord, String text) {
        return (text.contains(halfWord));
    }

    private Optional<String> findMatch(String halfWord, String text, List<Lemma> lemmaList) {
        List<String> sentences = divideByFiveWords(text);

        for (String sentence : sentences) {
            if (sentence.toLowerCase().contains(halfWord)) {
                List<String> words = Arrays.stream(sentence.split(" ")).toList();

                Optional<String> searchWord = findWord(words, halfWord, lemmaList);
                if (searchWord.isPresent()) {
                    return markWord(words, searchWord.get());
                }
            }
        }

        return Optional.empty();
    }

    private boolean isLemmaPresent(List<Lemma> lemmaList, Set<String> lemmaSet) {
        return lemmaList.stream().map(Lemma::getLemma).anyMatch(lemmaSet::contains);
    }

    private String getHalfWord(String word) {
        return word.substring(0, word.length() / 2);
    }

    private Optional<String> findWord(List<String> words, String halfWord, List<Lemma> lemmaList) {
        return words.stream()
                .filter(word -> word.contains(halfWord))
                .filter(word -> isLemmaPresent(lemmaList, lemmaService.getLemmaSet(word)))
                .findFirst();
    }

    private Optional<String> markWord(List<String> words, String searchWord) {
        String result = words.stream()
                .map(word -> word.equals(searchWord) ? "<mark>" + word + "</mark>" : word)
                .collect(Collectors.joining(" "))
                .trim();

        return Optional.of(result);
    }

    private String concatResultList(List<String> resultList) {
        return String.join(" ", resultList).trim();
    }
}
