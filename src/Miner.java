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

    private final Tile tin_rock_tile = new Tile(3223, 3147);
    private final Tile iron_rock_tile = new Tile(3286, 3368);
    private Tile destination = tin_rock_tile;

    @Override
    public void onStart() {
        Logger.log("Starting script...");
        setNames();
        Logger.log("Starting script with pick: " + pickaxe_name + ", ore: " +  rock_name);
    }

    @Override
    public int onLoop() {
        Logger.log("In loop with pick: " + pickaxe_name + ", ore: " +  rock_name);
        if (!Players.getLocal().isAnimating()) {
            setNames();
            if (!Inventory.contains(pickaxe_name)) {
                if (Bank.open()) {
                    Bank.depositAllItems();
                    if (!Bank.withdraw(pickaxe_name)) {
                        Logger.log("Failed to get pickaxe");
                        return -1;
                    }
                }
            } else {
                if (Inventory.isFull()) {
                    Inventory.dropAllExcept(pickaxe_name);
                }
                if (destination.distance() > 1) {
                    Walking.walk(destination);
                } else {
                    Logger.log("Finding new rock");
                    Sleep.sleep(Calculations.random(200, 1000));
                    GameObject rock = GameObjects.closest(rock_name);
                    if (rock != null && rock.exists() && rock.canReach() && rock.distance(destination) < 2 && rock.getIndex() != -4503587711215435L) {
                        Logger.log("Rock real, reachable. Interacting.");
                        rock.interact("Mine");
                    }
                }
                return 500 + Calculations.random(0, 500);
            }
        }
        return 100;
    }

    @Override
    public void onExit() {
        Logger.log("Stopping script...");
    }

    private void setNames() {
        int skill = Skills.getRealLevel(Skill.MINING);

        Logger.log("Mining skill: " + skill);

        // set pickaxe name first
        if (skill > 41) {
            pickaxe_name = "Rune pickaxe";
        } else if (skill >= 31) {
            pickaxe_name = "Adamant pickaxe";
        } else if (skill >= 21) {
            pickaxe_name = "Mithril pickaxe";
        } else if (skill >= 11) {
            pickaxe_name = "Black pickaxe";
        }

        // set rock type
        if (skill < 15) {
            rock_name = "Tin rocks";
        } else {
            rock_name = "Iron rocks";
            destination = iron_rock_tile;
        }
    }
}
