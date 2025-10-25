package f2pVariedTrainer;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.HashMap;
import java.util.Map;

@ScriptManifest(name = "AIO", description = "Main controller to run the other scripts.", author = "sawyerm",
        version = 1.0, category = Category.MISC)


public class AIO_Scheduler extends TaskScript {

    public static int inventory_limit = 50;

    public static int fisher_inv = 0;
    public static int miner_inv = 0;
    public static int tree_inv = 0;

    static Map<String, Integer> inventories = new HashMap<>();
    static Map<String, Integer> inventory_limits  = new HashMap<>();

    public static final int individual_inventory_limit = 50;

    public static boolean canCast = true;

    public final static String axe_name = null;
    public final static String tree_name = null;

    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
        setInventoryLimits();
        addInventory();
        setFailLimit(3);
        addNodes(new Runecrafter());
    }

    private void addInventory() {
        Logger.log("Adding inventories to the dictionary.");
        inventories.put("Fisher", 0);
        inventories.put("Miner", 0);
        inventories.put("Cooker", 0);
        inventories.put("Runecrafter", 0);
        inventories.put("TreeCutter", 0);
        inventories.put("LesserDemonStriker", 0);

    }

    private void setInventoryLimits() {
        Logger.log("Setting inventory limits.");
        inventory_limits.put("Fisher", individual_inventory_limit);
        inventory_limits.put("Miner", individual_inventory_limit);
        inventory_limits.put("Cooker", individual_inventory_limit);
        inventory_limits.put("Runecrafter", individual_inventory_limit);
        inventory_limits.put("TreeCutter", individual_inventory_limit);
        inventory_limits.put("LesserDemonStriker", individual_inventory_limit);

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
        Logger.log("Updating inventory for " + task + ". Current inventories: " + inventories.get(task) + 1 + "/" + inventory_limits.get(task));
        inventories.put(task, inventories.get(task) + 1);
    }

    public static boolean valid(String task) {
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

}
