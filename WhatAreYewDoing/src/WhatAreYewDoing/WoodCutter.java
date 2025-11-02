package WhatAreYewDoing;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
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
import org.dreambot.api.wrappers.interactive.Player;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ScriptManifest(name = "[One Tap] F2P Woodcutting", description = "[One Tap] F2P woodcutter that goes to the right logs and uses the best axe.", author = "sawyerm",
        version = 1.0, category = Category.WOODCUTTING, image = "8Zl24YQ")

public class WoodCutter extends AbstractScript {
    private enum State {WALKING_TO_BANK, WALKING_TO_TREE, CHOP, BUY_AXE}
    private State state = State.WALKING_TO_BANK;
    private boolean initialized = false;
    private boolean tree = false;
    private boolean oak = false;
    private boolean yew = false;
    private boolean useGE = false;

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

    private final Tile ge_tile = new Tile(3162, 3488);

    private final List<String> axeNames = new ArrayList<>(Arrays.asList("Bronze axe", "Black axe", "Mithril axe", "Adamant axe", "Rune axe"));
    private int axe_index = 0;

    private Tile destination;

    private String tree_name = "Tree";
    private String axe_name = "Bronze axe";

    private DiscordWebhook webhook;
    private final String webhook_url = "https://discord.com/api/webhooks/1432171424512217139/E3AwwCdQS6f2vGAf_9k5T65m7SScnc2LNVH5zpHKYrMm9lApA_GpiJIYAouajelmyhFe";
    private long startTime;
    private int inventories = 0;
    private int startExp;

    @Override
    public void onStart(String... params) {
        Logger.log("Script started with quickstart.");
        for (String param : params) {
            Logger.log("Examining parameter: " + param);
            switch (param) {
                case "tree":
                    tree = true;
                    break;
                case "oak":
                    oak = true;
                    break;
                case "yew":
                    yew = true;
                    break;
                default:
                    Logger.log("Unknown parameter: " + param);
                    break;
            }
        }

        Logger.log("tree: " + tree + ". oak: " + oak + ". yew: " + yew + ".");

        startExp = Skills.getExperience(Skill.WOODCUTTING);
        startTime = System.currentTimeMillis();
        webhook = new DiscordWebhook(webhook_url);
    }

    @Override
    public void onStart() {
        Logger.log("Script started without quickstart.");
        YewGUI gui = new YewGUI();
        gui.sleepUntilConfirmed();

        tree = gui.getTree();
        oak = gui.getOak();
        yew = gui.getYew();

        startTime = System.currentTimeMillis();
        webhook = new DiscordWebhook(webhook_url);
    }

    private int initialize() {
        Logger.log("Sleeping until handlers are done.");
        if (Client.getGameState() != GameState.LOGGED_IN
                || !Players.getLocal().exists()) {
            Logger.log("Waiting for login/welcome screen handlers...");
            return -1;
        }
        startExp = Skills.getExperience(Skill.WOODCUTTING);
        Logger.log("Starting script...");
        Logger.log("Current woodcutting skill: " + Skills.getRealLevel(Skill.WOODCUTTING));
        updateTreeAndAxe();
        if (Inventory.contains(axe_name)) {
            state = State.WALKING_TO_TREE;
        }
        Logger.log("Starting script with axe " + axe_name + " and tree " + tree_name + ", state: " + state + ".");
        return 1;
    }

    @Override
    public void onExit() {
        long endTime = System.currentTimeMillis();
        long runtime = (endTime - startTime) / (1000 * 60);
        int totalExp = Skills.getExperience(Skill.WOODCUTTING) - startExp;
        if (runtime > 10) {
            webhook.sendMessage("A user has exited OTS Woodcutting after " + runtime + " minutes, gaining " + totalExp + " experience.");
        }
    }

    private Tile returnTreeSpot(List<Tile> treeList) {
        Logger.log("treelist.size " + treeList.size());
        int index = Calculations.random(0, treeList.size());
        return treeList.get(index);
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

    @Override
    public int onLoop() {
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

                case BUY_AXE:
                    return buy_axe();
            }
        }
        return 500;
    }

    private int buy_axe() {
        if (ge_tile.distance() < 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(ge_tile);
            }
        } else {
            int open_slot = GrandExchange.getFirstOpenSlot();
            if (open_slot == -1) {
                Logger.log("no GE slots open.");
                return -1;
            } else {
                GrandExchange.addBuyItem(axe_name);
                Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(open_slot), 30000);
                if (GrandExchange.isReadyToCollect()) {
                    GrandExchange.collect();
                    state = State.WALKING_TO_BANK;
                } else {
                    GrandExchange.cancelOffer(open_slot);
                }
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
                inventories += 1;
                if (inventories % 10 == 0) {
                    webhook.sendMessage("A user completed " + inventories + " inventories.");
                }
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
            updateAxe();
            updateAxeIndex();
            Sleep.sleepUntil(() -> Bank.depositAllExcept(axe_name), 3000);
            Sleep.sleepUntil(Inventory::isEmpty, 1000);
            if (!Inventory.contains(axe_name)) {
                if (Bank.contains(axe_name)) {
                    Sleep.sleepUntil(() -> Bank.withdraw(axe_name), 3000);
                } else if (useGE) {
                    state = State.BUY_AXE;
                    return 500;
                }
            }
            Sleep.sleepUntil(() -> Inventory.contains(axe_name), 5000);
            if (Inventory.contains(axe_name)) {
                Logger.log("WALKING_TO_TREE");
                state = State.WALKING_TO_TREE;
            } else {
                Logger.log("Failed to withdraw axe " + axe_name + ".");
            }
        }
        return 500;
    }

    private void updateAxeIndex() {
        while (!Inventory.contains(axe_name) && !Bank.contains(axe_name)) {
            axe_index--;
            axe_name = axeNames.get(axe_index);
        }
    }

    private void updateAxe() {
        int skill = Skills.getRealLevel(Skill.WOODCUTTING);

        // set axe
        if (skill >= 41) {
            axe_index = 4;
        } else if (skill >= 31) {
            axe_index = 3;
        } else if (skill >= 21) {
            axe_index = 2;
        } else if (skill >= 11) {
            axe_index = 1;
        } else {
            axe_index = 0;
        }

        axe_name = axeNames.get(axe_index);
    }

    private void updateTree() {
        int skill = Skills.getRealLevel(Skill.WOODCUTTING);

        // see if user has set tree
        if (yew || oak || tree) {
            Logger.log("Setting custom tree.");
            if (yew) {
                tree_name = "Yew tree";
                destination = returnTreeSpot(yewTreeSpots);
            } else if (oak) {
                tree_name = "Oak tree";
                destination = returnTreeSpot(oakTreeSpots);
            } else {
                destination = returnTreeSpot(treeSpots);
            }
        } else {
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
        }
    }

    private void updateTreeAndAxe() {
        updateTree();
        updateAxe();
    }
}
