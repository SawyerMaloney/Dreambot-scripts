package onetapbuilder;

import org.dreambot.api.utilities.Logger;

import java.util.*;

public class NeededItemTracker {
    // Maps for ItemBuyer, tracking items we need to place orders for,
    // and items we already have placed orders for.
    private final static Map<String, Integer> itemsToBuy = new HashMap<>();
    private final static Map<String, Integer> orderedItems = new HashMap<>();
    // gather from other scripts. No amount, will just do it for one task length
    // so maps to the task name
    private final static List<String> itemsToGather = new ArrayList<>();
    // Map that allows us to block execution for scripts that have items we're yet to get
    private final static Map<String, List<String>> taskRequiredItems = new HashMap<>();

    private static final List<String> gatherableItems = Arrays.asList("Raw shrimps", "Raw anchovies", "Raw trout", "Raw salmon", "Logs", "Oak logs", "Yew logs", "Maple logs", "Willow logs");
    private static final List<String> fishableItems = Arrays.asList("Raw shrimps", "Raw anchovies", "Raw trout", "Raw salmon");
    private static final List<String> cuttableItems = Arrays.asList("Logs", "Oak logs", "Yew logs", "Willow Logs", "Maple logs");

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

    public static void addItemToGather(String itemName, String task) {
        itemsToGather.add(itemName);
        if (taskRequiredItems.containsKey(task)) {
            taskRequiredItems.get(task).add(itemName);
        } else {
            taskRequiredItems.put(task, new ArrayList<>());
            taskRequiredItems.get(task).add(itemName);
        }
    }

    public static Iterator<Map.Entry<String, Integer>> neededItems() {
        return itemsToBuy.entrySet().iterator();
    }

    public static boolean areOrderedItems() {
        return !orderedItems.isEmpty();
    }

    public static boolean needsBuyableItems() {
        List<String> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemsToBuy.entrySet()) {
            if (!gatherableItems.contains(entry.getKey())) {
                items.add(entry.getKey());
            }
        }
        return !items.isEmpty();
    }

    public static List<String> getFishableNeededItems() {
        List<String> fishableNeededItems = new ArrayList<>();
        for (String item : itemsToGather) {
            if (fishableItems.contains(item)) {
                fishableNeededItems.add(item);
            }
        }
        return fishableNeededItems;
    }

    public static List<String> getCuttableNeededItems() {
        List<String> cuttableNeededItems = new ArrayList<>();
        for (String item : itemsToGather) {
            if (cuttableItems.contains(item)) {
                cuttableNeededItems.add(item);
            }
        }
        return cuttableNeededItems;
    }

    public static boolean taskRequiresItems(String taskName) {
        return taskRequiredItems.containsKey(taskName) && !taskRequiredItems.get(taskName).isEmpty();
    }

    public static boolean hasOrderedItems() {
        return !orderedItems.isEmpty();
    }
}
