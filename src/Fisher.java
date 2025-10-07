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

    @Override
    public void onStart() {
        Logger.log("Starting fishing bot...");
        setNames();
        Logger.log("Starting script with rod " + rod_name + ".");
    }

    @Override
    public int onLoop() {
        if (!Inventory.contains(rod_name)) {
            Logger.log("Missing rod.");
            if (Bank.open()) {
                Sleep.sleep(Calculations.random(0, 100));
                Bank.depositAllItems();
                Sleep.sleep(Calculations.random(0, 100));
                if (!Bank.withdraw(rod_name)) {
                    Logger.log("Failed to get rod " + rod_name + ".");
                    return -1;
                }
            }
        }
        if (!Inventory.isFull()) {
            if (!Players.getLocal().isAnimating()) {
                if (destination.distance() > 10 && !fishing) {
                    if (Walking.shouldWalk()) {
                        Logger.log("Walking to spot...");
                        Walking.walk(destination);
                    }
                } else {
                    Logger.log("At spot. Looking for fishing spot.");
                    fishing = true;
                    NPC fishing_spot = NPCs.closest("Fishing spot");
                    Sleep.sleep(Calculations.random(100, 500));

                    if (fishing_spot != null && fishing_spot.exists() && fishing_spot.canReach()) {
                        Logger.log("Found fishing spot");
                        fishing_spot.interact("Net");
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
        }
    }
}
