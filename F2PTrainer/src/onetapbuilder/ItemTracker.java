package onetapbuilder;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.util.*;

public class ItemTracker  {
    // Maps for ItemBuyer, tracking items we need to place orders for,
    // and items we already have placed orders for.
    private final static Map<String, Integer> itemsToBuy = new HashMap<>();
    private final static Map<String, Integer> orderedItems = new HashMap<>();
    // gather from other scripts. No amount, will just do it for one task length
    // so maps to the task name
    private final static List<String> itemsToGather = new ArrayList<>();
    // Map that allows us to block execution for scripts that have items we're yet to get
    private final static Map<String, List<String>> taskRequiredItems = new HashMap<>();

    public static void addItemToBuy(String itemName, int amount, String task) {
        Logger.log("Adding needed item: " + itemName + " ("+amount+").");
        if (itemsToBuy.containsKey(itemName)) {
            itemsToBuy.put(itemName, itemsToBuy.get(itemName) + amount);
        } else {
            itemsToBuy.put(itemName, amount);
        }
        if (taskRequiredItems.containsKey(task)) {
            taskRequiredItems.get(task).add(itemName);
        }  else {
            taskRequiredItems.put(task, new ArrayList<>());
            taskRequiredItems.get(task).add(itemName);
        }
    }

    public static void addItemToGather(String itemName, String task) {
        Logger.log("Adding item to gather: " + itemName + " for task " + task + ".");
        itemsToGather.add(itemName);
        if (taskRequiredItems.containsKey(task)) {
            taskRequiredItems.get(task).add(itemName);
        } else {
            taskRequiredItems.put(task, new ArrayList<>());
            taskRequiredItems.get(task).add(itemName);
        }
    }

    public static void addOrderedItem(String itemName) {
        int amount = itemsToBuy.get(itemName);
        Logger.log("Adding ordered item: " + itemName + " ("+amount+").");
        itemsToBuy.remove(itemName);
        orderedItems.put(itemName, amount);
    }

    public static boolean isOrderedItem(String itemName) {
        return orderedItems.containsKey(itemName);
    }

    public static void removeOrderedItem(String itemName) {
        orderedItems.remove(itemName);
        removeTaskRequiredItems(itemName);  // assuming here that tasks don't overlap in what items they need
    }

    private static void removeTaskRequiredItems(String itemName) {
        for (String key : taskRequiredItems.keySet()) {
            taskRequiredItems.get(key).remove(itemName);
        }
    }

    public static Iterator<Map.Entry<String, Integer>> itemsToBuy() {
        return itemsToBuy.entrySet().iterator();
    }

    public static boolean areOrderedItems() {
        return !orderedItems.isEmpty();
    }

    public static boolean needsBuyableItems() {
        List<String> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemsToBuy.entrySet()) {
            if (!gatherableItems().contains(entry.getKey())) {
                items.add(entry.getKey());
            }
        }
        return !items.isEmpty();
    }

    public static List<String> gatherableItems() {
        List<String> gatherables = new ArrayList<>();
        for (TaskNode node : OneTapBuilder.nodes) {
            if (node instanceof ResourceNode) {
                gatherables.addAll(((ResourceNode) node).getProducedItems());
            }
        }
        return gatherables;
    }

    public static boolean taskRequiresItems(String taskName) {
        return taskRequiredItems.containsKey(taskName) && !taskRequiredItems.get(taskName).isEmpty();
    }

    public static boolean hasOrderedItems() {
        return !orderedItems.isEmpty();
    }

    public static boolean isItemToGather (String itemName) {
        return itemsToGather.contains(itemName);
    }

    public static void removeGatherItem(String itemName) {
        itemsToGather.remove(itemName);
    }

    public static boolean isTaskRequiredItem(String itemName) {
        for (String task : taskRequiredItems.keySet()) {
            if (taskRequiredItems.get(task).contains(itemName)) {
                return true;
            }
        }
        return false;
    }

    public static void removeTaskRequiredItem(String itemName) {
        for (String task : taskRequiredItems.keySet()) {
            taskRequiredItems.get(task).remove(itemName);
        }
    }

    public static List<String> getSellableResources() {
        List<String> sellableResources = new ArrayList<>();

        for (TaskNode node : OneTapBuilder.nodes) {
            if (node instanceof SellableProducer) {
                sellableResources.addAll(((SellableProducer) node).getSellableResources());
            }
        }
        return sellableResources;
    }

    public static int getNumberSellableResources() {
        int numberSellableResources = 0;
        for (String item : getSellableResources()) {
            if (Inventory.contains(item)) {
                Item invItem = Inventory.get(item);
                if (invItem != null) {
                    numberSellableResources += invItem.getAmount();
                }
            }
            if (Bank.contains(item)) {
                Item bankItem = Bank.get(item);
                if (bankItem != null) {
                    numberSellableResources += bankItem.getAmount();
                }
            }
        }
        return numberSellableResources;
    }

    public static List<String> getItemsToGather() {
        return itemsToGather;
    }
}
