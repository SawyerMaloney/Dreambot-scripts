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

@ScriptManifest(name = "Mining Tool", description = "AIO Mining tool.", author = "sawyerdm",
        version = 1.0, category = Category.MINING, image="")

public class Miner extends AbstractScript {

    private String rock_name = "Tin rocks";
    private String pickaxe_name = "Black pickaxe";
    @Override
    public void onStart(String... params) {
        Logger.log("Starting script...");
    }

    @Override
    public int onLoop() {
        if (!Players.getLocal().isAnimating()) {
            if (Inventory.isFull()) {
                Inventory.dropAllExcept(pickaxe_name);
            }
            Logger.log("Finding new rock");
            Sleep.sleep(Calculations.random(200, 1000));
            GameObject rock = GameObjects.closest(rock_name);
            if (rock != null && rock.exists() && rock.canReach()) {
                Logger.log("Rock real, reachable. Interacting.");
                rock.interact("Mine");
            }
            return 500 + Calculations.random(0, 500);
        }
        return 100;
    }

    @Override
    public void onExit() {
        Logger.log("Stopping script...");
    }
}
