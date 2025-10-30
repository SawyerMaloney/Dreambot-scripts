package onetapbuilder;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Locatable;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

@ScriptManifest(name = "[One Tap] F2P Account Builder", description = "Main controller to run the other scripts.", author = "sawyerm",
        version = 1.0, category = Category.MISC)


public class OneTapBuilder extends TaskScript implements ItemContainerListener {

    public static int inventory_limit = 50;

    static Map<String, Integer> inventories = new HashMap<>();
    static Map<String, Integer> inventory_limits  = new HashMap<>();

    public static final int individual_inventory_limit = 50;

    public static boolean canCast = true;

    // Maps for ItemBuyer, tracking items we need to place orders for,
    // and items we already have placed orders for.
    private final static Map<String, Integer> itemsToBuy = new HashMap<>();
    private final static Map<String, Integer> orderedItems = new HashMap<>();
    // gather from other scripts. No amount, will just do it for one task length
    // so maps to the task name
    private final static List<String> itemsToGather = new ArrayList<>();
    // Map that allows us to block execution for scripts that have items we're yet to get
    private final static Map<String, List<String>> taskRequiredItems = new HashMap<>();

    public static final Tile geTile = new Tile(3162, 3487);
    private static int gold = 0;
    private final static boolean needGold = false;
    public static boolean init;
    private final long task_length = 60_000;
    private final Timer timer = new Timer(task_length);
    private static final List<String> gatherableItems = Arrays.asList("Raw shrimps", "Raw anchovies", "Raw trout", "Raw salmon", "Logs", "Oak logs", "Yew logs", "Maple logs", "Willow logs");
    private static final List<String> fishableItems = Arrays.asList("Raw shrimps", "Raw anchovies", "Raw trout", "Raw salmon");
    private static final List<String> cuttableItems = Arrays.asList("Logs", "Oak logs", "Yew logs", "Willow Logs", "Maple logs");

    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
        setInventoryLimits();
        addInventory();
        setFailLimit(3);
        addItemToBuy("Raw shrimps", 30, "Blah Blah");
        addNodes(new Init(), new ItemBuyer(), new Cooker(), new Fisher());
    }

    private void addInventory() {
        Logger.log("Adding inventories to the dictionary.");
        inventories.put("Fisher", 0);
        inventories.put("Miner", 0);
        inventories.put("Cooker", 0);
        inventories.put("Runecrafter", 0);
        inventories.put("TreeCutter", 0);
        inventories.put("LesserDemonStriker", 0);
        inventories.put("Firemaker", 0);
    }

    private void setInventoryLimits() {
        Logger.log("Setting inventory limits.");
        inventory_limits.put("Fisher", 3);
        inventory_limits.put("Miner", individual_inventory_limit);
        inventory_limits.put("Cooker", individual_inventory_limit);
        inventory_limits.put("Runecrafter", individual_inventory_limit);
        inventory_limits.put("TreeCutter", individual_inventory_limit);
        inventory_limits.put("LesserDemonStriker", individual_inventory_limit);
        inventory_limits.put("Firemaker", individual_inventory_limit);
    }

    @Override
    public void onExit() {
        Logger.log("Script ended");

        // if script reached its endpoint (not stopped by user)
        if (atInventoryLimit()) {
            Client.logout();
        }
    }

    private static boolean atInventoryLimit() {
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

    public static boolean valid(String task) {
        switch (task) {
            case "ItemBuyer":
                return (needsBuyableItems() || !orderedItems.isEmpty()) && !needGold;
            case "BonesCollector":
                return defaultValidCheck(task) && needGold;
            case "Init":
                return !init;
            default:
                return defaultValidCheck(task);
        }
    }

    private static boolean defaultValidCheck(String task) {
        return !taskRequiresItems(task) && checkTasksInventory(task);
    }

    private static boolean checkTasksInventory(String task) {
        return !atInventoryLimit() && inventories.get(task) < inventory_limits.get(task);
    }

    public static int retrieveItem(String item, boolean all) {
        if (all) {
            Sleep.sleepUntil(() -> Bank.withdrawAll(item), 5000);
        } else {
            Sleep.sleepUntil(() -> Bank.withdraw(item), 5000);
        }
        Sleep.sleepUntil(() -> Inventory.contains(item), 5000);
        if (!Inventory.contains(item)) {
            Logger.log("Failed to get item " + item);
            return -1;
        }
        return 1;
    }

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

    private static boolean needsBuyableItems() {
        List<String> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemsToBuy.entrySet()) {
            if (!gatherableItems.contains(entry.getKey())) {
                items.add(entry.getKey());
            }
        }
        return !items.isEmpty();
    }

    public static boolean sleepWhileAnimating(BooleanSupplier returnPredicate, int timeout, int randomLower, int randomUpper) {
        AtomicLong lastAnimationTime = new AtomicLong(System.currentTimeMillis());
        // wait while cooking
        return Sleep.sleepWhile(() -> {
            if (Players.getLocal().isAnimating()) {
                lastAnimationTime.set(System.currentTimeMillis());
            }
            return returnPredicate.getAsBoolean() && (Players.getLocal().isAnimating() || System.currentTimeMillis() - lastAnimationTime.get() < 2000);
        }, timeout + Calculations.random(randomLower, randomUpper));
    }

    public static boolean anyOnTile(List<? extends Locatable> entities, Tile tile) {
        for (Locatable entity : entities) {
            if (entity.getTile().equals(tile) && !entity.equals(Players.getLocal())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLevelUpVisible() {
        WidgetChild w = Widgets.get(233, 1);
        return w != null && w.isVisible();
    }

    public static void setGoldAmount(int gold) {
        OneTapBuilder.gold = gold;
    }

    @Override
    public void onInventoryItemAdded(Item item) {
        ItemBuyer.onInventoryItemAdded(item);
    }

    @Override
    public void onInventoryItemChanged(Item incoming, Item existing) {
        ItemBuyer.onInventoryItemChanged(incoming, existing);
    }

    private static boolean taskRequiresItems(String taskName) {
        return taskRequiredItems.containsKey(taskName) && !taskRequiredItems.get(taskName).isEmpty();
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
}