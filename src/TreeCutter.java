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

@ScriptManifest(name = "Woodcutter", description = "Woodcutting script.", author = "sawyerdm",
        version = 1.0, category = Category.WOODCUTTING, image="")


public class TreeCutter extends AbstractScript {
    private final Tile tree_spot = new Tile(3160, 3456);
    private final Tile oak_tree_spot = new Tile(3166, 3417);
    private boolean returned = false;
    private Tile destination = tree_spot;

    private String tree_name = "Tree";
    private String axe_name = "Bronze axe";

    @Override
    public void onStart() {
        Logger.log("Starting script...");
        Logger.log("Current woodcutting skill: " + Skills.getRealLevel(Skill.WOODCUTTING));
        updateTreeAndAxe();
        Logger.log("Starting script with axe " + axe_name + " and tree " + tree_name + ".");
    }

    private void updateTreeAndAxe() {
        int skill = Skills.getRealLevel(Skill.WOODCUTTING);

        // set axe
        if (skill >= 41) {
            axe_name = "Rune axe";
        } else if (skill >= 31) {
            axe_name = "Adamant axe";
        } else if (skill >= 21) {
            axe_name = "Mithril axe";
        } else if (skill >= 11) {
            axe_name = "Black axe";
        }

        // set tree
        if (skill >= 15) {
            tree_name = "Oak tree";
            destination = oak_tree_spot;
        }

        Logger.log("Current axe name: " + axe_name + ". Current tree name: " + tree_name);
    }

    @Override
    public int onLoop() {
        if (!Inventory.contains(axe_name)) {
            if (Bank.open()) {
                if (!Bank.withdraw(axe_name)) {
                    Logger.log("Failed to find axe " + axe_name);
                    return -1;
                }
            }
        }
        else if (!Inventory.isFull()) {
            if (destination.distance() > 5 && !returned) {
                if (Walking.shouldWalk()) {
                    Walking.walk(destination);
                }
            } else {
                returned =  true;
                // Find the nearest  tree
                GameObject tree = GameObjects.closest(tree_name);

                if (tree != null && tree.exists() && tree.isOnScreen()) {
                    Sleep.sleep(Calculations.random(200, 2000));
                    Logger.log("Found  tree at: " + tree.getTile());
                    tree.interact("Chop down");
                    Sleep.sleepUntil(() -> !tree.exists(), 5000);
                } else {
                    Logger.log("No tree nearby.");
                    returned = false;
                }
            }
        } else {
            if (Bank.open()) {
                updateTreeAndAxe();
                Bank.depositAllExcept(axe_name);
                Sleep.sleep(Calculations.random(200, 2000));
                if (Inventory.isEmpty()) {
                    if (!Bank.withdraw(axe_name)) {
                        Logger.log("Failed to find axe " + axe_name);
                        return -1;
                    }
                }
                returned = false;
            }
        }
        return 500;
    }

    @Override
    public void onExit() {
        Logger.log("Script stopped.");
    }
}
