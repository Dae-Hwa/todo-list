package com.codesquad.esfj.todolist.task;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskDTO {
    public static Map<String, List<TaskDTO.Response>> groupingByType(List<Task> tasks) {
        return tasks.stream()
                .map(Response::from)
                .collect(Collectors.groupingBy(Response::getTaskType));
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
