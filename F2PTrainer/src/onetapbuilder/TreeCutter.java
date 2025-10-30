package onetapbuilder;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;

import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TreeCutter extends TaskNode {

    private enum State {WALKING_TO_BANK, WALKING_TO_TREE, CHOP, BUY_AXE}
    private State state = State.WALKING_TO_BANK;
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

    private Tile destination;

    private String tree_name = "Tree";
    private String axe_name = "Bronze axe";

    @Override
    public boolean accept() {
        return OneTapBuilder.valid("TreeCutter");
    }

    private int initialize() {
        Logger.log("Current woodcutting skill: " + Skills.getRealLevel(Skill.WOODCUTTING));
        updateTreeAndAxe();
        if (Inventory.contains(axe_name)) {
            state = State.WALKING_TO_TREE;
        }
        Logger.log("Starting script with axe " + axe_name + " and tree " + tree_name + ", state: " + state + ".");
        return 1;
    }


    private Tile returnTreeSpot(List<Tile> treeList) {
        Logger.log("treelist.size " + treeList.size());
        int index = Calculations.random(0, treeList.size());
        return treeList.get(index);
    }

    @Override
    public int execute() {
        if (!initialized) {
            if (initialize() == 1) {
                initialized = true;
            }
        } else {
            switch (state) {
                case WALKING_TO_BANK:
                    return walk_to_bank();

                case WALKING_TO_TREE:
                    return walk_to_tree();

                case CHOP:
                    return chop();
            }
        }
        return 500;
    }


    private int chop() {
        // Find the nearest  tree
        GameObject tree = GameObjects.closest(tree_name);

        if (tree != null && tree.exists() && tree.canReach()) {
            Logger.log("Found  tree at: " + tree.getTile());
            Sleep.sleepUntil(() -> tree.interact("Chop down"), 5000);
            Sleep.sleepUntil(() -> !tree.exists() || Inventory.isFull(), 30000);
            if (Inventory.isFull()) {
                Logger.log("WALKING_TO_BANK");
                state = State.WALKING_TO_BANK;
            }
        } else {
            Logger.log("No tree nearby.");
        }
        return 500;
    }

    private int walk_to_tree() {
        if (destination.distance() > 5) {
            if (Walking.shouldWalk()) {
                Walking.walk(destination);
            }
        } else {
            Logger.log("CHOP");
            state = State.CHOP;
        }
        return 500;
    }

    private int walk_to_bank() {
        if (Bank.open()) {
            Sleep.sleepUntil(() -> Bank.depositAllExcept(axe_name), 3000);
            Sleep.sleepUntil(() -> Inventory.isEmpty() || Inventory.onlyContains(axe_name), 1000);
            if (!Inventory.contains(axe_name)) {
                if (Bank.contains(axe_name)) {
                    Sleep.sleepUntil(() -> Bank.withdraw(axe_name), 3000);
                } else {
                    Logger.log("Need axe " + axe_name + ". Adding to needed items.");
                    OneTapBuilder.addItemToBuy(axe_name, 1, "TreeCutter");
                }
            }
            Sleep.sleepUntil(() -> Inventory.contains(axe_name), 5000);
            if (Inventory.contains(axe_name)) {
                Logger.log("WALKING_TO_TREE");
                state = State.WALKING_TO_TREE;
            } else {
                Logger.log("Failed to withdraw axe" + axe_name + ".");
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
            axe_index = 1;
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
