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
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Locatable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

@ScriptManifest(name = "[One Tap] F2P Account Builder", description = "Main controller to run the other scripts.", author = "sawyerm",
        version = 1.0, category = Category.MISC)


public class OneTapBuilder extends TaskScript {

    public static int inventory_limit = 50;

    static Map<String, Integer> inventories = new HashMap<>();
    static Map<String, Integer> inventory_limits  = new HashMap<>();

    public static final int individual_inventory_limit = 50;

    public static boolean canCast = true;

    public final static String axe_name = null;

    private static List<Map.Entry<String, Integer>> neededItems = new ArrayList<>();

    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
        setInventoryLimits();
        addInventory();
        setFailLimit(3);
        addNodes(new Firemaker());
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
        inventory_limits.put("Fisher", individual_inventory_limit);
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

    public static boolean addNeededItem(Map.Entry<String, Integer> item) {
        Logger.log("Adding needed item: " + item.getKey() + ".");
        neededItems.add(item);
        return true;
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

    public static <T extends Locatable> List<T> getSortedClosest(List<T> locatables) {
        Tile playerTile = Players.getLocal().getTile();
        return locatables.stream()
                .sorted(Comparator.comparingDouble(l -> l.distance(playerTile)))
                .collect(Collectors.toList());
    }

    public static <T extends Locatable> T getClosest(List<T> locatables) {
        List<T> sortedLocatables =  getSortedClosest(locatables);
        return sortedLocatables.get(0);
    }

    public static boolean isLevelUpVisible() {
        return Widgets.get(233, 1) != null && Widgets.get(233, 1).isVisible();
    }
}
