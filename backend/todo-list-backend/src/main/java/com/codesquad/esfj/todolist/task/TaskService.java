package com.codesquad.esfj.todolist.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private JdbcTaskRepository taskRepository;

    public TaskService(JdbcTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Map<String, List<TaskDTO.Response>> readAll() {
        Map<Long, Task> result = taskRepository.findAllByDeletedFalse().stream()
                .collect(Collectors.toMap(Task::getId, task -> task));

        return TaskDTO.groupingByType(result);
    }

    public Map<String, List<TaskDTO.Response>> readAllBy(String taskType) {
        List<Task> tasks = taskRepository.findAllByTaskTypeAndDeletedFalse(taskType);
        return TaskDTO.groupingByType(tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task)));
    }

    public TaskDTO.Response readOne(long id) {
        return TaskDTO.response(taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 Task가 존재하지 않습니다." + id)));
    }

    @Transactional
    public long create(Task task) {
        Optional<Task> topTask = taskRepository.findByPreviousIdAndTaskType(Task.TOP_PREVIOUS_ID, task.getTaskType());
        long id = taskRepository.save(task).getId();

        if (topTask.isPresent()) {
            topTask.get().moveAfter(id);
            taskRepository.save(topTask.get());
        } else {
            setIsHeadTo(task.getId(), true)
                    .ifPresent(taskRepository::save);
        }

        return id;
    }

    @Transactional
    public void update(long id, Task updatedTask) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 Task가 존재하지 않습니다." + id))
                .update(updatedTask);
        taskRepository.save(task);
    }

    @Transactional
    public void delete(long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 Task가 존재하지 않습니다." + id));
        Optional<Task> nextTask = taskRepository.findByPreviousIdAndTaskType(id, task.getTaskType());
        if (nextTask.isPresent()) {
            Task presentNextTask = nextTask.get();
            presentNextTask.moveAfter(task.getPreviousId());
            taskRepository.save(presentNextTask);
        } else {
            setIsHeadTo(task.getPreviousId(), true)
                    .ifPresent(taskRepository::save);
        }

        taskRepository.save(task.delete());
    }

    @Transactional
    public void move(long id, String targetTaskType, long targetId) {
        logger.debug("move start - id: {}, targetTaskType: {}, targetId: {}", id, targetTaskType, targetId);

        Task taskToMove = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 Task가 존재하지 않습니다." + id));

        Optional<Task> originalNextTask = taskRepository.findByPreviousIdAndTaskType(id, taskToMove.getTaskType());
        Optional<Task> newNextTask = taskRepository.findByPreviousIdAndTaskType(targetId, targetTaskType);

        if (originalNextTask.isPresent()) {
            originalNextTask.get().moveAfterPreviousOf(taskToMove);
            taskRepository.save(originalNextTask.get());
        } else {
            setIsHeadTo(taskToMove.getPreviousId(), true)
                    .ifPresent(taskRepository::save);
        }

        if (newNextTask.isPresent()) {
            taskToMove.moveAfterPreviousOf(newNextTask.get());
            taskToMove.setIsHead(false);

            newNextTask.get().moveAfter(taskToMove.getId());

            taskRepository.save(newNextTask.get());
        } else {
            taskToMove.moveAfter(targetId, targetTaskType);
            taskToMove.setIsHead(true);
        }

        if (taskToMove.getId().equals(taskToMove.getPreviousId())) {
            throw new PreviousTaskNotAllowedException(taskToMove.toString());
        }

        taskRepository.save(taskToMove);

        setIsHeadTo(taskToMove.getPreviousId(), false)
                .ifPresent(taskRepository::save);

        Map<String, List<Task>> results = taskRepository.findAllByDeletedFalse().stream()
                .collect(Collectors.groupingBy(Task::getTaskType));

        for (List<Task> resultTasks : results.values()) {
            if(resultTasks.stream().filter(Task::isHead).count() != 1) {
                throw new IllegalStateException("비정상적 이동 발생(헤더이상) : id : " + id + ", targetType : " + targetTaskType + ", targetId : " + targetId);
            }
            if(resultTasks.stream().filter(task -> task.getPreviousId().equals(Task.TOP_PREVIOUS_ID)).count() != 1) {
                throw new IllegalStateException("비정상적 이동 발생(TOP이상) : id : " + id + ", targetType : " + targetTaskType + ", targetId : " + targetId);
            }
        }

        logger.debug("move end\ntaskToMove: {}\noriginalNextTask: {}\nnewNextTask: {}", taskToMove, originalNextTask, newNextTask);
    }

    private Optional<Task> setIsHeadTo(long targetId, boolean isHead) {
        Optional<Task> task = taskRepository.findById(targetId);

        task.ifPresent(presentTask -> presentTask.setIsHead(isHead));

        return task;
    }
}
