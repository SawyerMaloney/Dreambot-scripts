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

    private final Tile destination = new Tile(3241, 3149);
    private boolean fishing = false;
    private int inventories = 0;

    @Override
    public void onStart(String... params) {
        Logger.log("Starting fishing bot...");
    }

    @Override
    public int onLoop() {
        if (inventories >= 10) {
            return -1;
        }

        if (!Inventory.isFull()) {
            if (destination.distance() > 5 && !fishing) {
                if (Walking.shouldWalk()) {
                    Walking.walk(destination);
                }
            } else {
                fishing = true;
                NPC fishing_spot = NPCs.closest("Fishing spot");

                if (fishing_spot != null && fishing_spot.exists() && fishing_spot.isOnScreen() && fishing_spot.canReach()) {
                    Logger.log("Found fishing spot");
                    fishing_spot.interact("Net");
                    Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), 20000);
                } else {
                    Logger.log("No fishing spot found.");
                    fishing = false;
                }
            }
        } else {
            fishing = false;
            if (Bank.isOpen()) {
                Bank.depositAllExcept("Small fishing net");
                inventories += 1;
            }
        }
        return 100;
    }

    @Override
    public void onExit() {
        Logger.log("Stopping script...");
    }
}
