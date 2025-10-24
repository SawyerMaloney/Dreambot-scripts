package WhatAreYewDoing;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "WhatAreYewDoing", description = "F2P woodcutter that goes to the right logs and uses the best axe.", author = "sawyerm",
        version = 1.0, category = Category.WOODCUTTING, image = "A3fca38")

public class WoodCutter extends AbstractScript {
    private boolean initialized = false;
    private final List<Tile> treeSpots = new ArrayList<>(Arrays.asList(
            new Tile(3160, 3456),

            new Tile (3107, 3226)
    ));
    private final List<Tile> oakTreeSpots = new ArrayList<>(Arrays.asList(
            new Tile(3166, 3417),
            new Tile(3101, 3243)
    ));

    private final List<Tile> yewTreeSpots = new ArrayList<>(Arrays.asList(
            new Tile(3087, 3477),
            new Tile(3209, 3503)
    ));

    private final List<String> axeNames = new ArrayList<>(Arrays.asList("Bronze axe", "Adamant axe", "Mithril axe", "Rune axe"));
    private int axe_index = 0;

    private boolean returned = false;
    private Tile destination;

    private String tree_name = "Tree";
    private String axe_name = "Bronze axe";

    private Tile returnTreeSpot(List<Tile> treeList) {
        int index = Calculations.random(0, treeList.size() - 1);
        return treeList.get(index);
    }

    private int bankForAxe() {
        if (Bank.open()) {
            Bank.depositAllItems();
            Sleep.sleepUntil(() -> Bank.withdraw(axe_name), 5000);
            Sleep.sleep(Calculations.random(1000, 1500));
            if (!Inventory.contains(axe_name)) {
                Logger.log("Failed to find axe " + axe_name);
                return -1;
            }
        }
        return 0;
    }

    private void chopTree() {
        // Find the nearest  tree
        GameObject tree = GameObjects.closest(tree_name);

        if (tree != null && tree.exists() && tree.canReach()) {
            Sleep.sleep(Calculations.random(200, 2000));
            Logger.log("Found  tree at: " + tree.getTile());
            tree.interact("Chop down");
            Sleep.sleepUntil(() -> !tree.exists(), 30000);
        } else {
            Logger.log("No tree nearby.");
            returned = false;
        }
    }

    private int deposit() {
        Logger.log("Checking if we should grab a better axe and depositing logs.");
        updateTreeAndAxe();
        Bank.depositAllExcept(axe_name);
        Sleep.sleep(Calculations.random(200, 2000));
        if (Inventory.isEmpty()) {
            if (!Bank.withdraw(axe_name)) {
                Logger.log("Failed to find axe " + axe_name + ". Finding next best axe.");
                return findNextBestAxe();
            }
        }
        return 0;
    }

    private int findNextBestAxe() {
        for (int i = 0; i <= axe_index; i++) {
            if (Bank.contains(axeNames.get(i))) {
                Bank.withdraw(axeNames.get(i));
                return 0;
            }
        }

        Logger.log("Failed to find any suitable axe!");
        return -1;
    }

    private void initialize() {
        Logger.log("All changes went well!");
        Logger.log("Starting script...");
        Logger.log("Current woodcutting skill: " + Skills.getRealLevel(Skill.WOODCUTTING));
        updateTreeAndAxe();
        Logger.log("Starting script with axe " + axe_name + " and tree " + tree_name + ".");
    }

    @Override
    public int onLoop() {
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
                    Logger.log("Walking to tree spot.");
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
            axe_index = 3;
        } else if (skill >= 31) {
            axe_index = 2;
        } else if (skill >= 21) {
            axe_index = 1;;
        } else if (skill >= 11) {
            axe_index = 0;
        }

        axe_name = axeNames.get(axe_index);

        // set tree
        if (skill >= 60) {
            tree_name = "Yew tree";
            destination = returnTreeSpot(yewTreeSpots);
        } else if (skill >= 15) {
            tree_name = "Oak tree";
            destination = returnTreeSpot(oakTreeSpots);
        } else {
            destination = returnTreeSpot(treeSpots);
        }

        Logger.log("Current axe name: " + axe_name + ". Current tree name: " + tree_name);
    }
}
