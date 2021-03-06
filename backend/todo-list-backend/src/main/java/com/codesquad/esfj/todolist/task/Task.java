package com.codesquad.esfj.todolist.task;

import org.springframework.data.annotation.Id;
import org.springframework.util.StringUtils;

import java.util.Objects;

public class Task {
    public static final long TOP_PREVIOUS_ID = -1L;

    @Id
    private Long id;
    private String title;
    private String content;
    private String writer;
    private boolean deleted;
    private Long previousId = TOP_PREVIOUS_ID;
    private String taskType;
    private boolean isHead;

    public Task(Long id, String title, String content, String writer, String taskType) {
        this.id = id;

        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title is null or empty");
        }

        this.title = title;
        this.content = content;
        this.writer = writer;

        if (!StringUtils.hasText(taskType)) {
            throw new IllegalArgumentException("Task type is null or empty");
        }

        this.taskType = taskType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public void moveAfterPreviousOf(Task targetTask) {
        moveAfter(targetTask.getPreviousId(), targetTask.getTaskType());
    }

    public void moveAfter(Long targetId) {
        moveAfter(targetId, taskType);
    }

    public void moveAfter(Long targetId, String targetTaskType) {
        previousId = targetId;
        taskType = targetTaskType;
    }


    public boolean isDeleted() {
        return deleted;
    }

    public boolean isHead() {
        return isHead;
    }

    public void setIsHead(boolean isHead) {
        this.isHead = isHead;
    }

    public Task update(Task updatedTask) {
        this.title = updatedTask.title;
        this.content = updatedTask.content;
        this.writer = updatedTask.writer;
        return this;
    }

    public Task delete() {
        deleted = true;
        return this;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", writer='" + writer + '\'' +
                ", deleted=" + deleted +
                ", previousId=" + previousId +
                ", taskType='" + taskType + '\'' +
                ", isHead=" + isHead +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
