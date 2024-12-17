package service;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import ru.aksndr.domain.Epic;
import ru.aksndr.util.Managers;
import ru.aksndr.web.HttpTaskServer;
import ru.aksndr.web.adapters.DurationAdapter;
import ru.aksndr.web.adapters.LocalDateTimeAdapter;

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
    public void epicCreateTest() throws InterruptedException, IOException {
        Epic epic = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));


        // CREATE_EPIC
        HttpResponse<String> response = POST("http://localhost:8080/epics", gson.toJson(epic));
        assertEquals(200, response.statusCode(), "Ошибка создания эпика: " + response.body());
        assertEquals(epic, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //UPDATE_EPIC
        epic.addSubtask(subTask1);
        epic.addSubtask(subTask2);
        response = POST("http://localhost:8080/epics", gson.toJson(epic));
        assertEquals(200, response.statusCode(), "Ошибка обновления эпика: " + response.body());
        assertEquals(epic, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //GET_EPIC
        response = GET("http://localhost:8080/epics/" + epic.getId());
        assertEquals(200, response.statusCode(), "Ошибка получения эпика по ID: " + response.body());
        assertEquals(epic, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //GET_EPIC_SUBTASKS
        response = GET("http://localhost:8080/epics/" + epic.getId() + "/subtasks");
        assertEquals(200, response.statusCode(), "Ошибка получения подзадач эпика по ID: " + response.body());
        assertEquals(gson.toJson(epic.getSubTasks()), response.body(), "Возвращённые подзадачи не совпадают с созданными");


        response = POST("http://localhost:8080/epics", gson.toJson(epic1));
        assertEquals(200, response.statusCode(), "Ошибка создания эпика: " + response.body());
        assertEquals(epic1, gson.fromJson(response.body(), Epic.class), "Возвращённый эпик не совпадает с созданным");

        //GET_EPICS
        response = GET("http://localhost:8080/epics");
        assertEquals(200, response.statusCode(), "Ошибка получения эпиков: " + response.body());
        assertEquals(gson.toJson(taskManager.getAllEpics()), response.body(), "Возвращённые эпики не совпадают с созданными");

        //DELETE_EPIC
        response = DELETE("http://localhost:8080/epics/" + epic1.getId());
        assertEquals(204, response.statusCode(), "Ошибка удаления эпика: " + response.body());

        //DELETE_EPICS
        response = DELETE("http://localhost:8080/epics");
        assertEquals(204, response.statusCode(), "Ошибка удаления эпиков: " + response.body());
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