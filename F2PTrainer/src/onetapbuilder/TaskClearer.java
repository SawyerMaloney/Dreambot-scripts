package onetapbuilder;

import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;

public class TaskClearer extends TaskNode {
    @Override
    public boolean accept() {
        return true;
    }

    @Override
    public int execute() {
        Logger.log("Clearing finished tasks.");
        TaskScheduler.clearFinishedTasks();
        TaskScheduler.resetTimer();
        return 5000;
    }
}
