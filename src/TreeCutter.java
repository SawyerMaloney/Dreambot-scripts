import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.skills.Skills;

public class TreeCutter extends TaskNode {
    private boolean initialized = false;
    private final Tile tree_spot = new Tile(3160, 3456);
    private final Tile oak_tree_spot = new Tile(3166, 3417);
    private boolean returned = false;
    private Tile destination = tree_spot;

    private String tree_name = "Tree";
    private String axe_name = "Bronze axe";

    public static int inventories = 0;
    public static final int inventory_limit = 3;

    @Override
    public boolean accept() {
        return AIO_Scheduler.inventories < AIO_Scheduler.inventory_limit && inventories < inventory_limit; //
    }

    private int bankForAxe() {
        if (Bank.open()) {
            Bank.depositAllItems();
            if (!Bank.withdraw(axe_name)) {
                Logger.log("Failed to find axe " + axe_name);
                return -1;
            }
        }
        return 0;
    }

    private void chopTree() {
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

    private int deposit() {
        updateTreeAndAxe();
        Bank.depositAllExcept(axe_name);
        Sleep.sleep(Calculations.random(200, 2000));
        if (Inventory.isEmpty()) {
            if (!Bank.withdraw(axe_name)) {
                Logger.log("Failed to find axe " + axe_name);
                return -1;
            }
        }
        return 0;
    }

    private void initialize() {
        Logger.log("Starting script...");
        Logger.log("Current woodcutting skill: " + Skills.getRealLevel(Skill.WOODCUTTING));
        updateTreeAndAxe();
        Logger.log("Starting script with axe " + axe_name + " and tree " + tree_name + ".");
    }

    @Override
    public int execute() {
        if (!initialized) {
            initialize();
            initialized = true;
        } else if (!Inventory.contains(axe_name)) {
            int status = bankForAxe();
            if (status == -1) {
                return -1;
            }
        }
        else if (!Inventory.isFull()) {
            if (destination.distance() > 5 && !returned) {
                if (Walking.shouldWalk()) {
                    Walking.walk(destination);
                }
            } else {
                returned =  true;
                chopTree();
            }
        } else {
            if (Bank.open()) {
                int status = deposit();
                if (status == -1) {
                    return -1;
                }
                returned = false;
            }
        }
        return 500;
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
}
