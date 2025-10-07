import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.methods.interactive.Players;


public class Fisher extends TaskNode {
    private boolean initalized = false;

    private final Tile small_net_tile = new Tile(3241, 3149);
    private final Tile fly_fishing_tile = new Tile(3108, 3433);
    private Tile destination = small_net_tile;
    private boolean fishing = false;
    private String rod_name = "Small fishing net";
    private boolean feathers = false;
    private String interact = "Net";
    private String fishing_spot_name = "Fishing spot";

    public static int inventories = 0;
    public static int inventory_limit = 3;

    private void initialize() {
        Logger.log("Starting fishing bot...");
        setNames();
        Logger.log("Starting script with rod " + rod_name + " and feathers " + feathers + ".");
        initalized = true;
    }
    @Override
    public boolean accept() {
        return inventories < inventory_limit && AIO_Scheduler.inventories < AIO_Scheduler.inventory_limit;
    }

    private int retrieveRod() {
        if (Bank.open()) {
            Sleep.sleep(Calculations.random(0, 100));
            Bank.depositAllItems();
            Sleep.sleep(Calculations.random(500, 1000));
            if (!Bank.withdraw(rod_name)) {
                Logger.log("Failed to get rod " + rod_name + ".");
                return -1;
            }
            if (feathers) {
                if (!Bank.withdrawAll("Feather")) {
                    Logger.log("Failed to get feathers");
                    return -1;
                }
            }
        }
        return 500 + Calculations.random(100, 300);
    }

    private void fish() {
        Logger.log("At spot. Looking for fishing spot.");
        fishing = true;
        NPC fishing_spot = NPCs.closest(fishing_spot_name);
        Sleep.sleep(Calculations.random(100, 500));

        if (fishing_spot != null && fishing_spot.exists() && fishing_spot.canReach()) {
            Logger.log("Found fishing spot");
            fishing_spot.interact(interact);
            Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 5000);
        } else {
            Logger.log("No fishing spot found.");
            fishing = false;
        }
    }

    @Override
    public int execute() {
        if (!initalized) {
            initialize();
        }
        if (!Inventory.contains(rod_name) || (feathers && !Inventory.contains("Feather"))) {
            Logger.log("Missing rod or feathers.");
            return retrieveRod();
        }
        else if (!Inventory.isFull()) {
            if (!Players.getLocal().isAnimating()) {
                if (destination.distance() > 10 && !fishing) {
                    if (Walking.shouldWalk()) {
                        Logger.log("Walking to spot...");
                        Walking.walk(destination);
                    } else {
                        Logger.log("Shouldn't walk. Waiting...");
                    }
                } else {
                    fish();
                }
            }
            else {
                Logger.log("Busy fishing...");
                return 5000;
            }
        } else {
            Logger.log("Full inventory. Dropping fish.");
            fishing = false;
            Inventory.dropAll("Raw shrimps", "Raw anchovies", "Raw trout", "Raw salmon");
            inventories += 1;
            AIO_Scheduler.inventories += 1;
            setNames();
        }
        return 500 + Calculations.random(100, 500);
    }

    private void setNames() {
        int skill = Skills.getRealLevel(Skill.FISHING);

        if (skill >= 20) {
            rod_name = "Fly fishing rod";
            destination = fly_fishing_tile;
            feathers = true;
            fishing_spot_name = "Rod fishing spot";
            interact = "Lure";
        }
    }


}
