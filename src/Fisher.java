import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.methods.interactive.Players;

@ScriptManifest(name = "Fishing Tool", description = "AIO Fishing tool.", author = "sawyerdm",
        version = 1.0, category = Category.FISHING, image="")

public class Fisher extends AbstractScript {

    private final Tile small_net_tile = new Tile(3241, 3149);
    private final Tile fly_fishing_tile = new Tile(3108, 3433);
    private Tile destination = small_net_tile;
    private boolean fishing = false;
    private String rod_name = "Small fishing net";
    private boolean feathers = false;
    private String interact = "Net";
    private String fishing_spot_name = "Fishing spot";

    @Override
    public void onStart() {
        Logger.log("Starting fishing bot...");
        setNames();
        Logger.log("Starting script with rod " + rod_name + " and feathers " + feathers + ".");
    }

    @Override
    public int onLoop() {
        if (!Inventory.contains(rod_name) || (feathers && !Inventory.contains("Feather"))) {
            Logger.log("Missing rod or feathers.");
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
            }
            else {
                Logger.log("Busy fishing...");
                return 5000;
            }
        } else {
            Logger.log("Full inventory. Dropping fish.");
            fishing = false;
            Inventory.dropAll("Raw shrimps", "Raw anchovies", "Raw trout", "Raw salmon");
            setNames();
        }
        return 500 + Calculations.random(100, 500);
    }

    @Override
    public void onExit() {
        Logger.log("Stopping script...");
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
