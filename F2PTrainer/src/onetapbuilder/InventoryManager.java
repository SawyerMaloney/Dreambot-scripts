package onetapbuilder;

import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.util.HashMap;
import java.util.Map;

public class InventoryManager {
    public static int inventory_limit = 50;

    static Map<String, Integer> inventories = new HashMap<>();
    static Map<String, Integer> inventory_limits  = new HashMap<>();

    public static final int individual_inventory_limit = 50;

    public static void init() {
        setInventoryLimits();
        addInventory();
    }

    private static void setInventoryLimits() {
        Logger.log("Setting inventory limits.");
        inventory_limits.put("Fisher", 3);
        inventory_limits.put("Miner", individual_inventory_limit);
        inventory_limits.put("Cooker", individual_inventory_limit);
        inventory_limits.put("Runecrafter", individual_inventory_limit);
        inventory_limits.put("TreeCutter", individual_inventory_limit);
        inventory_limits.put("LesserDemonStriker", individual_inventory_limit);
        inventory_limits.put("Firemaker", individual_inventory_limit);
    }

    private static void addInventory() {
        Logger.log("Adding inventories to the dictionary.");
        inventories.put("Fisher", 0);
        inventories.put("Miner", 0);
        inventories.put("Cooker", 0);
        inventories.put("Runecrafter", 0);
        inventories.put("TreeCutter", 0);
        inventories.put("LesserDemonStriker", 0);
        inventories.put("Firemaker", 0);
    }


    public static boolean atInventoryLimit() {
        int inventory_count = 0;
        for (String key : inventories.keySet()) {
            inventory_count += inventories.get(key);
        }
        return inventory_count >= inventory_limit;
    }

    public static void updateInventories(String task) {
        inventories.put(task, inventories.get(task) + 1);
        Logger.log("Updating inventory for " + task + ". Current inventories: " + inventories.get(task) + "/" + inventory_limits.get(task));

    }

    public static boolean checkTasksInventory(String task) {
        return !atInventoryLimit() && inventories.get(task) < inventory_limits.get(task);
    }

    public static void onInventoryItemAdded(Item item) {
        ItemBuyer.onInventoryItemAdded(item);

        if (NeededItemTracker.isItemToGather(item.getName())) {
            Logger.log("Item " + item.getName() + " removed from items to gather.");
            NeededItemTracker.removeGatherItem(item.getName());
        }
    }

    public static void onInventoryItemChanged(Item incoming, Item existing) {
        ItemBuyer.onInventoryItemChanged(incoming, existing);
    }

}
