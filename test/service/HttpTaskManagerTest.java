package service;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;
import ru.aksndr.exceptions.TasksIntersectsException;
import ru.aksndr.util.Managers;
import ru.aksndr.web.HttpTaskServer;
import ru.aksndr.web.adapters.DurationAdapter;
import ru.aksndr.web.adapters.LocalDateTimeAdapter;
import ru.aksndr.web.adapters.TasksListTypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest{

    private static HttpTaskServer taskServer;
    private static HttpClient httpClient;
    private final Gson gson = new Gson().newBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .setPrettyPrinting()
            .create();

    @BeforeAll
    public static void initManger(){
        taskManager = Managers.getDefaultTaskManager();
        taskServer = new HttpTaskServer(taskManager);
        httpClient = HttpClient.newHttpClient();
    }

    @Override
    @BeforeEach
    public void init() throws Exception {
        super.init();
        taskServer.start();
    }

    @Override
    @AfterEach
    public void clean() {
        super.clean();
        taskServer.stop();
    }

    @DisplayName("Операции с эпиками")
    @Test
    public void epicTests() throws InterruptedException, IOException {
        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));

        //CREATE EPIC
        HttpResponse<String> response = POST("http://localhost:8080/epics", gson.toJson(epic));
        assertEquals(200, response.statusCode(), "Ошибка создания эпика: " + response.body());
        assertEquals(epic, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //UPDATE EPIC
        epic.addSubtask(subTask1);
        epic.addSubtask(subTask2);
        response = POST("http://localhost:8080/epics", gson.toJson(epic));
        assertEquals(200, response.statusCode(), "Ошибка обновления эпика: " + response.body());
        assertEquals(epic, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //GET EPIC
        response = GET("http://localhost:8080/epics/" + epic.getId());
        assertEquals(200, response.statusCode(), "Ошибка получения эпика по ID: " + response.body());
        assertEquals(epic, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //GET EPIC SUBTASKS
        response = GET("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        assertEquals(200, response.statusCode(), "Ошибка получения подзадач эпика по ID: " + response.body());
        assertEquals(gson.toJson(epic.getSubTasks()), response.body(), "Возвращённые подзадачи не совпадают с созданными");


        response = POST("http://localhost:8080/epics", gson.toJson(epic1));
        assertEquals(200, response.statusCode(), "Ошибка создания эпика: " + response.body());
        assertEquals(epic1, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //GET EPICS
        response = GET("http://localhost:8080/epics");
        assertEquals(200, response.statusCode(), "Ошибка получения эпиков: " + response.body());
        assertEquals(gson.toJson(taskManager.getAllEpics()), response.body(), "Возвращённые эпики не совпадают с созданными");

        //DELETE EPIC
        response = DELETE("http://localhost:8080/epics/" + epic1.getId());
        assertEquals(204, response.statusCode(), "Ошибка удаления эпика: " + response.body());

        //DELETE EPICS
        response = DELETE("http://localhost:8080/epics");
        assertEquals(204, response.statusCode(), "Ошибка удаления эпиков: " + response.body());
    }

    @DisplayName("Получение эпика по некорректному идентификатору")
    @Test
    public void unparsableIdTest() throws InterruptedException, IOException {
        HttpResponse<String>  response = GET("http://localhost:8080/epics/NaN");
        assertEquals(500, response.statusCode(), "Сервер не вернул ошибку при получении буквенного идентификатора");
    }

    @DisplayName("Операции с задачами")
    @Test
    public void taskTests() throws InterruptedException, IOException, TasksIntersectsException {
        Task task = taskManager.createTask(new Task("Задача 1", "Описание 1", LocalDateTime.of(2024, 12, 18, 10, 20), Duration.ofMinutes(15)));

        //CREATE TASK
        HttpResponse<String> response = POST("http://localhost:8080/tasks", gson.toJson(task));
        assertEquals(200, response.statusCode(), "Ошибка создания задачи: " + response.body());
        assertEquals(task, gson.fromJson(response.body(), Task.class), "Возвращённая задача не совпадает с созданным");

        //UPDATE TASK
        task.setDescription("Описание 1 - обновлено");
        response = POST("http://localhost:8080/tasks", gson.toJson(task));
        assertEquals(200, response.statusCode(), "Ошибка обновления задачи: " + response.body());
        assertEquals(task, gson.fromJson(response.body(), Task.class), "Возвращённая задача не совпадает с созданным");

        //GET TASK
        response = GET("http://localhost:8080/tasks/" + task.getId());
        assertEquals(200, response.statusCode(), "Ошибка получения задачи по ID: " + response.body());
        assertEquals(task, gson.fromJson(response.body(), Task.class), "Возвращённая задача не совпадает с созданным");

        //GET TASKS
        response = GET("http://localhost:8080/tasks");
        assertEquals(200, response.statusCode(), "Ошибка получения задач: " + response.body());
        assertEquals(gson.toJson(taskManager.getAllTasks()), response.body(), "Возвращённые задачи не совпадают с созданными");

        //DELETE TASK
        response = DELETE("http://localhost:8080/tasks/" + task.getId());
        assertEquals(204, response.statusCode(), "Ошибка удаления задачи: " + response.body());

        //DELETE TASKS
        response = DELETE("http://localhost:8080/tasks");
        assertEquals(204, response.statusCode(), "Ошибка удаления задач: " + response.body());
    }

    @DisplayName("Операции с подзадачами")
    @Test
    public void subTaskTests() throws InterruptedException, IOException, TasksIntersectsException {
        SubTask subTask = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId(), LocalDateTime.of(2024, 12, 15, 10, 20), Duration.ofMinutes(15)));

        //CREATE SUBTASK
        HttpResponse<String> response = POST("http://localhost:8080/subtasks", gson.toJson(subTask));
        assertEquals(200, response.statusCode(), "Ошибка создания подзадачи: " + response.body());
        assertEquals(subTask, gson.fromJson(response.body(), SubTask.class), "Возвращённая подзадача не совпадает с созданным");

        //UPDATE SUBTASK
        subTask.setDescription("Описание подзадачи 1 Эпика 1 - обновлено");
        response = POST("http://localhost:8080/subtasks", gson.toJson(subTask));
        assertEquals(200, response.statusCode(), "Ошибка обновления подзадачи: " + response.body());
        assertEquals(subTask, gson.fromJson(response.body(), SubTask.class), "Возвращённая подзадача не совпадает с созданным");

        //GET SUBTASK
        response = GET("http://localhost:8080/subtasks/" + subTask.getId());
        assertEquals(200, response.statusCode(), "Ошибка получения подзадачи по ID: " + response.body());
        assertEquals(subTask, gson.fromJson(response.body(), SubTask.class), "Возвращённая подзадача не совпадает с созданным");

        //GET SUBTASKS
        response = GET("http://localhost:8080/subtasks");
        assertEquals(200, response.statusCode(), "Ошибка получения подзадач: " + response.body());
        assertEquals(gson.toJson(taskManager.getAllSubTasks()), response.body(), "Возвращённые подзадачи не совпадают с созданными");

        //DELETE SUBTASK
        response = DELETE("http://localhost:8080/subtasks/" + subTask.getId());
        assertEquals(204, response.statusCode(), "Ошибка удаления подзадачи: " + response.body());

        //DELETE SUBTASKS
        response = DELETE("http://localhost:8080/subtasks");
        assertEquals(204, response.statusCode(), "Ошибка удаления подзадач: " + response.body());
    }

    @DisplayName("Получение истории задач")
    @Test
    public void historyTest() throws InterruptedException, IOException {
        HttpResponse<String> response = GET("http://localhost:8080/history");
        assertEquals(200, response.statusCode(), "Ошибка получения истории: " + response.body());
        assertEquals(taskManager.getHistory(), gson.fromJson(response.body(), new TasksListTypeToken().getType()), "Возвращённая история не совпадает с управляемой");
    }

    @DisplayName("Получение приоритезированных задач")
    @Test
    public void prioritizedTasksTest() throws InterruptedException, IOException {
        HttpResponse<String> response = GET("http://localhost:8080/prioritized");
        assertEquals(200, response.statusCode(), "Ошибка получения приоритезированных задач: " + response.body());
    }

    private HttpResponse<String> GET(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> POST(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> DELETE(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }



}