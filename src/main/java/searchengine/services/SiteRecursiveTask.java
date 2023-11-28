package searchengine.services;

import searchengine.constant.Constants;
import searchengine.dto.IndexingParamDto;

import java.util.*;
import java.util.concurrent.RecursiveAction;

public class SiteRecursiveTask extends RecursiveAction {
    private final IndexingParamDto dto;
    private final PageHandler pageHandler;
    private final Set<String> allUrls;

    public SiteRecursiveTask(PageHandler pageHandler, IndexingParamDto dto) {
        this.pageHandler = pageHandler;
        this.dto = dto;
        allUrls = Collections.synchronizedSet(new HashSet<>());
    }

    public SiteRecursiveTask(PageHandler pageHandler, Set<String> allUrls, IndexingParamDto dto) {
        this.pageHandler = pageHandler;
        this.allUrls = allUrls;
        this.dto = dto;
    }

    @Override
    protected void compute() {
        List<SiteRecursiveTask> taskList = new ArrayList<>();

        for (String url : pageHandler.extractUrls()) {

            synchronized (allUrls) {
                if (isNotAdd(url, allUrls)) {
                    continue;
                }
                allUrls.add(url);
            }

            var pageHandler = new PageHandler(dto, url);
            var task = new SiteRecursiveTask(pageHandler, allUrls, dto);
            task.fork();
            taskList.add(task);
        }

        for (SiteRecursiveTask task : taskList) {
            task.join();
        }
    }

    private boolean isNotAdd(String url, Set<String> allUrls) {
        return url.matches(Constants.REG_TYPES_FILES) || url.endsWith("#") || allUrls.contains(url);
    }
}
