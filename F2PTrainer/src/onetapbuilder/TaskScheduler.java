package onetapbuilder;

import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Timer;

import java.util.HashSet;
import java.util.Set;

public class TaskScheduler {
    public static boolean init;
    private static final long task_length = 600_000;
    private static final Timer timer = new Timer(task_length);
    private static final Set<String> finishedTasks = new HashSet<>();
    private static String currentTask = "";


    public static boolean valid(String task) {
        switch (task) {
            case "ItemBuyer":
                return (ItemTracker.needsBuyableItems() || ItemTracker.hasOrderedItems()) && !OneTapBuilder.needGold;
            case "BonesCollector":
                return defaultValidCheck(task) && OneTapBuilder.needGold;
            case "Init":
                return !init;
            case "TaskClearer":
                return finishedAllTasks();
            default:
                return defaultValidCheck(task);
        }
    }

    private static boolean defaultValidCheck(String task) {
        if (timer.finished() && currentTask.equals(task)) {
            Logger.log("Adding task " + task + " to finished tasks.");
            finishedTasks.add(task);
        }
        // Logger.log("Checking task " + task + ", timer: " + timer.finished() + ". finishedTaskContains: " + finishedTasks.contains(task) + ". Current task: " + currentTask + ".");
        return !ItemTracker.taskRequiresItems(task) && !finishedTasks.contains(task);
    }

    public static void clearFinishedTasks() {
        finishedTasks.clear();
    }

    public static void init(String task) {
        Logger.log("Initializing task " + task + ".");
        currentTask = task;
        timer.reset();
        timer.start();
        Logger.log("Current task: " + currentTask + ". Timer elapsed " + timer.elapsed() + ".");
    }

    public static void resetTimer() {
        timer.reset();
    }

    // TODO this needs to be updated in case the last one quits w/o reaching timer
    public static boolean finishedAllTasks() {
        return finishedTasks.size() == OneTapBuilder.nodes.size() - 3 && timer.finished();
    }
}
