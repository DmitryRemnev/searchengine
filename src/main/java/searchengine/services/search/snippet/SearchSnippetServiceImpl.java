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

@Service
@RequiredArgsConstructor
public class SearchSnippetServiceImpl implements SearchSnippetService {
    private final LemmaService lemmaService;

    @Override
    public String createSnippet(Page page, String query, List<Lemma> lemmaList) {
        Elements elements = extractElements(page);
        List<String> queryListWords = createQueryListWords(query);
        List<String> resultList = new ArrayList<>();
        String result;

        for (String queryWord : queryListWords) {
            String halfWord = getHalfWord(queryWord.toLowerCase());

            for (Element element : elements) {
                result = findSnippet(element, halfWord, lemmaList);

                if (result != null) {
                    resultList.add(result);
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

    private String findSnippet(Element element, String halfWord, List<Lemma> lemmaList) {
        if (element.hasText()) {
            String text = element.text();

            if (isPartialMatch(halfWord, text.toLowerCase())) {
                return findMatch(halfWord, text.toLowerCase(), lemmaList);
            }
        }

        return null;
    }

    private List<String> divideByFiveWords(String text) {
        List<String> words = Arrays.stream(text.split(" ")).toList();
        List<String> sentences = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        int i = 1;
        for (Iterator<String> iterator = words.iterator(); iterator.hasNext(); ) {
            builder.append(iterator.next());
            builder.append(" ");
            i++;

            if (!iterator.hasNext()) {
                sentences.add(builder.toString().trim());
            }

            if (i == 6) {
                sentences.add(builder.toString().trim());
                builder.delete(0, builder.length());
                i = 1;
            }
        }

        return sentences;
    }

    private boolean isPartialMatch(String halfWord, String text) {
        return (text.contains(halfWord));
    }

    private String findMatch(String halfWord, String text, List<Lemma> lemmaList) {
        List<String> sentences = divideByFiveWords(text);

        for (String sentence : sentences) {
            if (sentence.toLowerCase().contains(halfWord)) {
                List<String> words = Arrays.stream(sentence.split(" ")).toList();

                String searchWord = findWord(words, halfWord, lemmaList);
                if (searchWord != null) {
                    return markWord(words, searchWord);
                }
            }
        }

        return null;
    }

    private boolean isLemmaPresent(List<Lemma> lemmaList, Set<String> lemmaSet) {
        return lemmaList.stream().map(Lemma::getLemma).anyMatch(lemmaSet::contains);
    }

    private String getHalfWord(String word) {
        return word.substring(0, word.length() / 2);
    }

    private String findWord(List<String> words, String halfWord, List<Lemma> lemmaList) {
        for (String word : words) {

            if (word.contains(halfWord)) {
                if (isLemmaPresent(lemmaList, lemmaService.getLemmaSet(word))) {
                    return word;
                }
            }
        }

        return null;
    }

    private String markWord(List<String> words, String searchWord) {
        StringBuilder builder = new StringBuilder();

        for (String word : words) {
            if (word.equals(searchWord)) {
                builder.append("<mark>").append(word).append("</mark>");
                builder.append(" ");
            } else {
                builder.append(word);
                builder.append(" ");
            }
        }

        return builder.toString().trim();
    }

    private String concatResultList(List<String> resultList) {
        return String.join(" ", resultList).trim();
    }
}
