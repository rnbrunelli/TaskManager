package com.ricardo.todo.controller;

import com.ricardo.todo.dto.request.CreateTaskRequest;
import com.ricardo.todo.dto.request.UpdateTaskRequest;
import com.ricardo.todo.dto.response.TaskResponse;
import com.ricardo.todo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping()
    public ResponseEntity<List<TaskResponse>> getAllTasks(){
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<TaskResponse> getTasksById(@PathVariable Long id){
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping()
    public ResponseEntity<TaskResponse> createTask(@RequestBody @Valid CreateTaskRequest createTaskRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(createTaskRequest));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<TaskResponse> updateTask(@RequestBody @Valid UpdateTaskRequest updateTaskRequest, @PathVariable Long id){
        return ResponseEntity.ok(taskService.updateTask(id, updateTaskRequest));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id){
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<TaskResponse> toggleCompleteTask(@PathVariable Long id){
        return ResponseEntity.ok(taskService.toggleComplete(id));
    }


}
