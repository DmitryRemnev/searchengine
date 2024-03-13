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
        StringBuilder snippet = new StringBuilder();
        List<String> resultList = new ArrayList<>();
        String result;

        for (String queryWord : queryListWords) {

            for (Element element : elements) {
                result = findSnippet(element, queryWord, lemmaList);

                if (result != null) {
                    resultList.add(result);
                    break;
                }
            }
        }

        if (resultList.size() != queryListWords.size()) {
            return null;
        }

        for (String string : resultList) {
            snippet.append(string);
            snippet.append(" ");
        }

        return !snippet.toString().isBlank() ? snippet.toString().trim() : null;
    }

    private Elements extractElements(Page page) {
        String content = page.getContent();
        Document document = Jsoup.parse(content);
        return document.getAllElements();
    }

    private List<String> createQueryListWords(String query) {
        return Arrays.stream(query.split("\\s")).toList();
    }

    private String findSnippet(Element element, String queryWord, List<Lemma> lemmaList) {

        if (element.hasText()) {
            String text = element.text();

            String halfWord = getHalfWord(queryWord.toLowerCase());
            if (isPartialMatch(halfWord, text.toLowerCase())) {
                return findByPartialMatch(halfWord, text.toLowerCase(), lemmaList);
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

    private String findByPartialMatch(String halfWord, String text, List<Lemma> lemmaList) {
        List<String> sentences = divideByFiveWords(text);

        for (String sentence : sentences) {
            if (sentence.toLowerCase().contains(halfWord)) {
                List<String> words = Arrays.stream(sentence.split(" ")).toList();

                String searchWord = null;
                for (String word : words) {

                    if (word.contains(halfWord)) {
                        if (isLemmaPresent(lemmaList, lemmaService.getLemmaSet(word))) {
                            searchWord = word;
                        }
                    }
                }

                if (searchWord != null) {
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
}
