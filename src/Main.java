
import ru.aksndr.domain.*;
import ru.aksndr.enums.TaskStatus;
import ru.aksndr.service.ITaskManager;
import ru.aksndr.util.Managers;

public class Main {

    public static void main(String[] args) {
        ITaskManager taskManager = Managers.getDefaultTaskManager();

        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание 2"));

        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1"));
        SubTask subTask1 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId()));
        SubTask subTask2 = taskManager.createSubTask(new SubTask("Подзадача 2 Эпика 1", "Описание подзадачи 2 Эпика 1", epic1.getId()));

        taskManager.updateTask(new Task(task1.getId(), task1.getTitle(), task1.getDescription(), TaskStatus.IN_PROGRESS));

        taskManager.updateTask(new Task(task1.getId(), task1.getTitle(), task1.getDescription(), TaskStatus.DONE));

        taskManager.updateSubTask(new SubTask(subTask1.getId(), subTask1.getTitle(), subTask1.getDescription(), TaskStatus.DONE, subTask1.getEpicId()));

        taskManager.updateSubTask(new SubTask(subTask2.getId(), subTask2.getTitle(), subTask2.getDescription(), TaskStatus.DONE, subTask2.getEpicId()));

        taskManager.deleteTask(task2.getId());

        taskManager.deleteSubTask(subTask1.getId());

        printAllTasks(taskManager);
    }

    private static void printAllTasks(ITaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubTasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}