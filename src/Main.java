
import ru.aksndr.util.Managers;
import ru.aksndr.web.HttpTaskServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        HttpTaskServer server = new HttpTaskServer(Managers.getDefaultTaskManager());
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}