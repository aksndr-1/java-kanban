import org.junit.jupiter.api.Test;
import ru.aksndr.service.*;
import ru.aksndr.util.Managers;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class UtilsTests {

    // проверить, Managers возвращает экземпляр ITaskManager
    @Test
    public void testIfTaskManagerReturnsInitializedITaskManager() {
        ITaskManager taskManager = Managers.getDefaultTaskManager();
        assertNotNull(taskManager, "ITaskManager is null");
    }

    // проверить, Managers возвращает экземпляр IHistoryManager
    @Test
    public void testIfTaskManagerReturnsInitializedITaskHistoryManager() {
        IHistoryManager historyManager = Managers.getDefaultHistoryManager();
        assertNotNull(historyManager, "IHistoryManager is null");
    }

}