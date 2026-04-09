package com.ricardo.todo.service;

import com.ricardo.todo.dto.request.CreateTaskRequest;
import com.ricardo.todo.dto.request.UpdateTaskRequest;
import com.ricardo.todo.dto.response.TaskResponse;
import com.ricardo.todo.exception.TaskNotFoundException;
import com.ricardo.todo.model.Task;
import com.ricardo.todo.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository){
        this.taskRepository = taskRepository;
    }

    private TaskResponse toResponse(Task task){
        return new TaskResponse(task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt(),
                task.getUpdatedAt());
    }

    public List<TaskResponse> getAllTasks(){
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream().map(this::toResponse).toList();
    }

    public TaskResponse getTaskById(Long id){
        return toResponse(taskFinderById(id));
    }

    public TaskResponse createTask(CreateTaskRequest request){
        Task task = new Task(request.getTitle(),request.getDescription(),false);
        return toResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, UpdateTaskRequest request){
        Task task = taskFinderById(id);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCompleted(request.isCompleted());
        return toResponse(task);
    }

    public void deleteTask(Long id){
        Task task = taskFinderById(id);
        taskRepository.delete(task);
    }

    public TaskResponse toggleComplete(Long id){
        Task task = taskFinderById(id);
        task.setCompleted(!task.isCompleted());
        return toResponse(task);
    }


    private Task taskFinderById(Long id){
        return taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
    }
}
