package f2pVariedTrainer;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;


public class BonesCollector extends TaskNode {

    private final Tile destination = new Tile(3260, 3277);
    private boolean collecting = false;

    public static int inventories = 0;
    public static int inventory_limit = 3;

    @Override
    public boolean accept() {
        return AIO_Scheduler.inventories < AIO_Scheduler.inventory_limit && inventories < inventory_limit;
    }

    @Override
    public int execute() {
        if (!Inventory.isFull()) {
            Logger.log("Inventory not full. Distance: " + destination.distance() + ". shouldWalk: " + Walking.shouldWalk() + ". collecting: " + collecting);
            if (destination.distance() > 5 && !collecting) {
                if (Walking.shouldWalk()) {
                    Logger.log("Walking back.");
                    Walking.walk(destination);
                } else {
                    Logger.log("Shouldn't walk. Waiting...");
                }
            } else {
                collecting = true;
                GroundItem item = GroundItems.closest("Coins", "Bones");
                Sleep.sleep(Calculations.random(100, 300));
                if (item != null && item.exists()) {
                    Logger.log("Found item " + item.getName());
                    item.interact("Take");
                    Inventory.dropAll("Raw Beef");
                    Sleep.sleepUntil(() -> !item.exists(), 5000);
                } else {
                    Logger.log("No item found.");
                }
            }
        } else {
            if (Bank.open()) {
                Sleep.sleep(Calculations.random(100, 500));
                Logger.log("Bank is open.");
                Bank.depositAllItems();
                inventories += 1;
                AIO_Scheduler.inventories += 1;
                Logger.log("Deposited all items.");
                collecting = false;
            }
        }
        return 1000;
    }
}