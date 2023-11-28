package searchengine.services;

import searchengine.constant.Constants;
import searchengine.dto.IndexingParamDto;
import searchengine.dto.RecursiveDataDto;
import searchengine.model.Page;

import java.util.*;
import java.util.concurrent.RecursiveTask;

public class SiteRecursiveTask extends RecursiveTask<List<Page>> {
    private final IndexingParamDto paramDto;
    private final PageHandler pageHandler;
    private final Set<String> urlSet;

    public SiteRecursiveTask(PageHandler pageHandler, IndexingParamDto paramDto) {
        this.pageHandler = pageHandler;
        this.paramDto = paramDto;
        urlSet = Collections.synchronizedSet(new HashSet<>());
    }

    public SiteRecursiveTask(PageHandler pageHandler, Set<String> urlSet, IndexingParamDto paramDto) {
        this.pageHandler = pageHandler;
        this.urlSet = urlSet;
        this.paramDto = paramDto;
    }

    @Override
    protected List<Page> compute() {
        List<Page> pageList = new ArrayList<>();
        List<SiteRecursiveTask> taskList = new ArrayList<>();

        RecursiveDataDto recursiveDataDto = pageHandler.extractRecursiveData();
        pageList.add(recursiveDataDto.getPage());

        for (String url : recursiveDataDto.getUrlList()) {

            synchronized (urlSet) {
                if (isNotAdd(url, urlSet)) {
                    continue;
                }
                urlSet.add(url);
            }

            var pageHandler = new PageHandler(paramDto, url);
            var task = new SiteRecursiveTask(pageHandler, urlSet, paramDto);
            task.fork();
            taskList.add(task);
        }

        for (SiteRecursiveTask task : taskList) {
            pageList.addAll(task.join());
        }

        return pageList;
    }

    private boolean isNotAdd(String url, Set<String> allUrls) {
        return url.matches(Constants.REG_TYPES_FILES)
                || url.contains("#")
                || url.contains("?")
                || allUrls.contains(url);
    }
}
