package com.codesquad.esfj.todolist.task;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;
import java.util.stream.Collectors;

public class TaskDTO {
    public static Map<String, List<TaskDTO.Response>> groupingByType(Map<Long, Task> tasks) {
        List<Task> headers = tasks.values().stream()
                .filter(Task::isHead)
                .collect(Collectors.toList());

        Map<String, List<TaskDTO.Response>> result = new HashMap<>();

        for (Task header : headers) {
            Deque<Task> sortedTasks = new ArrayDeque<>();

            Task current = header;

            while (!current.getPreviousId().equals(Task.TOP_PREVIOUS_ID)) {
                sortedTasks.offerFirst(current);
                current = tasks.get(current.getPreviousId());
            }

            sortedTasks.offerFirst(current);

            result.put(
                    current.getTaskType(),
                    sortedTasks.stream()
                            .map(Response::from)
                            .collect(Collectors.toList())
            );
        }

        Set<String> resultKeys = result.keySet();

        if(!resultKeys.contains("todo")) {
            result.put("todo", Collections.emptyList());
        }
        if(!resultKeys.contains("done")) {
            result.put("done", Collections.emptyList());
        }
        if(!resultKeys.contains("doing")) {
            result.put("doing", Collections.emptyList());
        }

        return result;
    }

    public static Response response(Task task) {
        return Response.from(task);
    }

    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String writer;
        private Long previousId;

        @JsonIgnore
        private String taskType;

        private Response(Long id, String title, String content, String writer, Long previousId, String taskType) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.writer = writer;
            this.previousId = previousId;
            this.taskType = taskType;
        }

        public static Response from(Task task) {
            return new Response(
                    task.getId(),
                    task.getTitle(),
                    task.getContent(),
                    task.getWriter(),
                    task.getPreviousId(),
                    task.getTaskType()
            );
        }

        public Long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getWriter() {
            return writer;
        }

        public Long getPreviousId() {
            return previousId;
        }

        public String getTaskType() {
            return taskType;
        }
    }
}
