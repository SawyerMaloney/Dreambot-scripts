package onetapbuilder;

import onetapbuilder.ItemSeller.ItemSeller;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Timer;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class TaskScheduler {
    public static boolean init;
    private static final long task_length = 600_000;
    private static final Timer timer = new Timer(task_length);
    private static final Set<String> finishedTasks = new HashSet<>();
    private static String currentTask = "";


    public static boolean valid(String task) {
        switch (task) {
            case "ItemSeller":
                return ItemTracker.getNumberSellableResources() > 100 || OneTapBuilder.selling;
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
        boolean ret = !ItemTracker.taskRequiresItems(task) && !finishedTasks.contains(task);
        if (!ret && currentTask.equals(task)) {
            Logger.log("Task " + task + " cannot continue, adding to finished tasks.");
            finishedTasks.add(task);
        }
        return ret;
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

    public static boolean finishedAllTasks() {
        int numberOfTasks = (int) OneTapBuilder.nodes.stream()
                .filter(node -> node instanceof Task)
                .count();
        return finishedTasks.size() == numberOfTasks;
    }
}
