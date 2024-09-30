import ru.aksndr.TaskManager;
import ru.aksndr.domain.Epic;
import ru.aksndr.domain.SubTask;
import ru.aksndr.domain.Task;
import ru.aksndr.enums.TaskStatus;

public class Main {

    // Да, так и буду делать когда буду использовать JUnit
    // В данном случае тестирование выполняемое из метода мейн не нарушает принцип единственной ответственности.
    // Разделять содержимое этого метода на несколько - искусттвенное разделение, без практического смысла.
    public static void main(String[] args) {

        System.out.println("Поехали!");

        TaskManager taskManager = new TaskManager();
        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.

        Task task1 = taskManager.createTask(new Task("Задача 1", "Описание 1"));
        Task task2 = taskManager.createTask(new Task("Задача 2", "Описание 2"));
        System.out.println("Созданы задачи:");
        System.out.println(task1);
        System.out.println(task2);


        Epic epic1 = taskManager.createEpic(new Epic("Эпик 1", "Описание эпика 1") );
        SubTask subTask1 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 1", "Описание подзадачи 1 Эпика 1", epic1.getId()));
        SubTask subTask2 = taskManager.createSubTask(new SubTask("Подзадача 2 Эпика 1", "Описание подзадачи 2 Эпика 1", epic1.getId()));

        System.out.println("Создан эпик: " + epic1);
        System.out.println("С подзадачами: " + taskManager.getEpicSubtasks(epic1.getId()));

        Epic epic2 = taskManager.createEpic(new Epic("Эпик 2", "Описание эпика 2") );
        SubTask subTask3 = taskManager.createSubTask(new SubTask("Подзадача 1 Эпика 2", "Описание подзадачи 1 Эпика 2", epic2.getId()));

        System.out.println("Создан эпик: " + epic2);
        System.out.println("С подзадачами: " + taskManager.getEpicSubtasks(epic2.getId()));

        //Измените статусы созданных объектов, распечатайте их. Проверьте, что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        taskManager.updateTask(new Task(task1.getId(), task1.getTitle(), task1.getDescription(), TaskStatus.IN_PROGRESS));
        System.out.println("Задача " + task1.getId() + " обновлена: ");
        System.out.println(taskManager.getTask(task1.getId()));

        taskManager.updateTask(new Task(task1.getId(), task1.getTitle(), task1.getDescription(), TaskStatus.DONE));
        System.out.println("Задача " + task2.getId() + " обновлена: ");
        System.out.println(taskManager.getTask(task2.getId()));

        taskManager.updateSubTask(new SubTask(subTask1.getId(), subTask1.getTitle(), subTask1.getDescription(), TaskStatus.DONE, subTask1.getEpicId()));
        System.out.println("Подзадача " + subTask1.getId() + " обновлена: ");
        System.out.println(taskManager.getSubTask(subTask1.getId()));

        taskManager.updateSubTask(new SubTask(subTask2.getId(), subTask2.getTitle(), subTask2.getDescription(), TaskStatus.DONE, subTask2.getEpicId()));
        System.out.println("Подзадача " + subTask2.getId() + " обновлена: ");
        System.out.println(taskManager.getSubTask(subTask2.getId()));

        System.out.println("Эпик" + epic1.getId() + " изменил статус: ");
        System.out.println(taskManager.getEpic(epic1.getId()));

        taskManager.updateSubTask(new SubTask(subTask3.getId(), subTask3.getTitle(), subTask3.getDescription(), TaskStatus.DONE, subTask3.getEpicId()));

        System.out.println("Подзадача " + subTask3.getId() + " обновлена: ");
        System.out.println(taskManager.getSubTask(subTask3.getId()));

        System.out.println("Эпик" + epic2.getId() + " изменил статус: ");
        System.out.println(taskManager.getEpic(epic2.getId()));

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        taskManager.deleteTask(task2.getId());
        System.out.println("Задача " + task2.getId() + " удалена. Оставшиеся задачи: ");
        System.out.println(taskManager.getAllTasks());


        taskManager.deleteSubTask(subTask1.getId());
        System.out.println("Подзадача " + subTask1.getId() + " удалена. Оставшиеся подзадачи эпика: ");
        System.out.println(taskManager.getEpicSubtasks(subTask1.getEpicId()));

        taskManager.deleteEpic(epic2.getId());
        System.out.println("Эпик " + epic2.getId() + " удален. Оставшиеся эпики: ");
        System.out.println(taskManager.getAllEpics());

        System.out.println("Оставшиеся подзадачи: ");
        System.out.println(taskManager.getAllSubTasks());
    }
}
