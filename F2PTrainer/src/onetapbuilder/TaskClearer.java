package onetapbuilder;

import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;

public class TaskClearer extends TaskNode {
    @Override
    public boolean accept() {
        Logger.log("Calling TaskClearer");
        return TaskScheduler.valid("TaskClearer");
    }

    @Override
    public int execute() {
        Logger.log("Clearing finished tasks.");
        TaskScheduler.clearFinishedTasks();
        reset();
        return 5000;
    }

    public static void reset() {
        Logger.log("Resetting task specific flags.");
        TaskScheduler.resetTimer();

        // reset task specific flags
        for (TaskNode node : OneTapBuilder.nodes) {
            if (node instanceof Resetable) {
                ((Resetable) node).reset();
            }
        }
    }
}
