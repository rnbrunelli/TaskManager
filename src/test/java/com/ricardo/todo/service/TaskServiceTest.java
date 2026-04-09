package com.ricardo.todo.service;

import com.ricardo.todo.dto.request.CreateTaskRequest;
import com.ricardo.todo.dto.request.UpdateTaskRequest;
import com.ricardo.todo.dto.response.TaskResponse;
import com.ricardo.todo.model.Task;
import com.ricardo.todo.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    private static final Logger log = LoggerFactory.getLogger(TaskServiceTest.class);
    @Mock
    TaskRepository taskRepository;

    @InjectMocks
    TaskService taskService;

    @DisplayName("GET All Tasks")
    @Test
    void getAllTasks() {
        Task task = new Task("Comprar leite", null, false);
        when(taskRepository.findAll()).thenReturn(List.of(task));
        List<TaskResponse> result = taskService.getAllTasks();
        assertEquals(1, result.size());
        assertEquals("Comprar leite", result.get(0).getTitle());
    }

    @Test
    void getTaskById() {
        Task task = new Task("TaskTeste", null,false);
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponse result = taskService.getTaskById(1L);
        assertEquals(1L, result.getId());
        assertEquals("TaskTeste",result.getTitle());
    }

    @Test
    void createTask() {
        Task task = new Task("TaskTeste", null,false);
        when(taskRepository.save(any(Task.class))).thenReturn(task);
        TaskResponse result = taskService.createTask(new CreateTaskRequest(task.getTitle(),task.getDescription()));
        assertEquals("TaskTeste", result.getTitle());
        assertEquals(null, result.getDescription());
    }

    @Test
    void updateTask() {
        Task task = new Task("TaskTeste", null, false);
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        UpdateTaskRequest updateTaskRequest = new UpdateTaskRequest("Novo Titulo", "nova description",true);
        TaskResponse result = taskService.updateTask(1L, updateTaskRequest);
        assertEquals(1L, result.getId());
        assertEquals("Novo Titulo", result.getTitle());
        assertEquals("nova description",result.getDescription());
        assertEquals(true, result.isCompleted());

    }

    @Test
    void deleteTask() {
        Task task = new Task("TaskTeste", null, false);
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        taskService.deleteTask(1L);
        verify(taskRepository).delete(task);
    }

    @Test
    void toggleComplete() {
        Task task = new Task("TaskTeste", null, false);
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        TaskResponse result = taskService.toggleComplete(1L);
        assertEquals(true, result.isCompleted());
        assertEquals("TaskTeste", result.getTitle());
        assertEquals(null, result.getDescription());
    }
}