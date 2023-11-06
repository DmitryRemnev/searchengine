package searchengine.services;

import searchengine.dto.RecursiveTaskDto;

import java.util.*;
import java.util.concurrent.RecursiveAction;

public class SiteRecursiveTask extends RecursiveAction {
    public static final String REG_TYPES_FILES = ".*\\.(jpg|docx|doc|pdf|png|zip)";
    private final RecursiveTaskDto dto;
    public final PageHandler pageHandler;
    private final Set<String> allUrls;

    public SiteRecursiveTask(PageHandler pageHandler, RecursiveTaskDto dto) {
        this.pageHandler = pageHandler;
        this.dto = dto;
        allUrls = Collections.synchronizedSet(new HashSet<>());
    }

    public SiteRecursiveTask(PageHandler pageHandler, Set<String> allUrls, RecursiveTaskDto dto) {
        this.pageHandler = pageHandler;
        this.allUrls = allUrls;
        this.dto = dto;
    }

    @Override
    protected void compute() {
        List<SiteRecursiveTask> taskList = new ArrayList<>();

        for (String url : pageHandler.getUrls()) {

            synchronized (allUrls) {
                if (isNotAdd(url, allUrls)) {
                    continue;
                }
                allUrls.add(url);
            }

            var task = new SiteRecursiveTask(new PageHandler(dto, url), allUrls, dto);
            task.fork();
            taskList.add(task);
        }

        for (SiteRecursiveTask task : taskList) {
            task.join();
        }
    }

    private boolean isNotAdd(String url, Set<String> allUrls) {
        return allUrls.contains(url) || url.matches(REG_TYPES_FILES);
    }
}
