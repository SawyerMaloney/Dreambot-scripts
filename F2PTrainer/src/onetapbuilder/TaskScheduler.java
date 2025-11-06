package onetapbuilder;

import org.dreambot.api.utilities.Timer;

public class TaskScheduler {
    public static boolean init;
    private static final long task_length = 300_000; // 5 minutes
    private static final Timer timer = new Timer(task_length);

    public static boolean valid(String task) {
        switch (task) {
            case "ItemBuyer":
                return (ItemTracker.needsBuyableItems() || ItemTracker.hasOrderedItems()) && !OneTapBuilder.needGold;
            case "BonesCollector":
                return defaultValidCheck(task) && OneTapBuilder.needGold;
            case "Init":
                return !init;
            default:
                return defaultValidCheck(task);
        }
    }

    private static boolean defaultValidCheck(String task) {
        return !ItemTracker.taskRequiresItems(task) && InventoryManager.checkTasksInventory(task) && !timer.finished();
    }

    public static void startTimer() {
        timer.start();
    }

    public static boolean timerFinished() {
        return timer.finished();
    }

    public static void resetTimer() {
        timer.reset();
    }

    public static void pauseTimer() {
        timer.pause();
    }

    public static void timer() {
        resetTimer();
        startTimer();
    }

}
