package ru.aksndr.exceptions;

import ru.aksndr.enums.WorkItemType;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(WorkItemType itemType, int id) {
        super(itemType.name() + " with id " + id + " not found");
    }

}