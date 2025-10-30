package onetapbuilder;

import org.dreambot.api.methods.container.impl.Inventory;

public class TaskScheduler {
    public static boolean init;

    public static boolean valid(String task) {
        switch (task) {
            case "ItemBuyer":
                return (NeededItemTracker.needsBuyableItems() || NeededItemTracker.hasOrderedItems()) && !OneTapBuilder.needGold;
            case "BonesCollector":
                return defaultValidCheck(task) && OneTapBuilder.needGold;
            case "Init":
                return !init;
            default:
                return defaultValidCheck(task);
        }
    }

    private static boolean defaultValidCheck(String task) {
        return !NeededItemTracker.taskRequiresItems(task) && InventoryManager.checkTasksInventory(task);
    }

}
