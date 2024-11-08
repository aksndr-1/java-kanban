package ru.aksndr.service;

import ru.aksndr.domain.Task;

import java.util.Collection;
import java.util.List;

public interface IHistoryManager {

    /**
    * Добавить задачу в историю просмотров
    */
    void add(Task task);

    /**
     * Добавить набор задач в историю просмотров
     */
    void add(Collection<? extends Task> values);

    /**
     * Удалить задачу из истории просмотров
     */
    void remove(int taskId);

    /**
     * Получить историю просмотра задачи
     * @return  История росмотров задачи
     */
    List<Task> getHistory();

}