package onetapbuilder;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.*;

public class Firemaker extends TaskNode {
    private enum State {
        WALKING_TO_BANK,
        RETRIEVE_WOOD,
        FIND_OPEN_SPOT,
        FIND_FIRE,
        BURN_WOOD
    }

    private String logName = "";
    private State state = State.WALKING_TO_BANK;

    private GameObject fire;

    private final List<String> logNames = Arrays.asList("Yew logs", "Maple logs", "Willow logs", "Oak logs", "Logs");
    private final List<String> burnableLogs = new ArrayList<>();

    @Override
    public boolean accept() {
        return OneTapBuilder.valid("Firemaker") && hasBurnableItems();
    }

    @Override
    public int execute() {
        switch (state) {
            case WALKING_TO_BANK:
                return walkToBank();
            case RETRIEVE_WOOD:
                return retrieveWood();
            case FIND_OPEN_SPOT:
                return findOpenSpot();
            case FIND_FIRE:
                return findFire();
            case BURN_WOOD:
                return burnWood();
        }
        return 500;
    }

    private int findFire() {
        GameObject fire = GameObjects.closest("Forester's Campfire");
        if (fire != null && fire.exists() && fire.distance() < 5) {
            Logger.log("Nearby Forester's Campfire found.");
            this.fire = fire;
            Logger.log("BURN_WOOD");
            state = State.BURN_WOOD;
            return 0;
        } else {
            if (fire == null) {
                Logger.log("Fire null");
            } else {
                Logger.log("null: false. exists: " + fire.exists() + ". distance: " + fire.distance());
            }
        }
        Item tinderbox = Inventory.get("Tinderbox");
        if (tinderbox != null) {
            Logger.log("Got tinderbox. Using on " + logName + ".");
            tinderbox.useOn(logName);
            OneTapBuilder.sleepWhileAnimating(() -> true, 10000, 500, 1000);
            fire = GameObjects.closest("Fire");
            Logger.log("BURN_WOOD");
            state = State.BURN_WOOD;
        }
        return 500;
    }

    private int burnWood() {
        if (fire == null) {
            Logger.log("Fire null, finding closest fire.");
            fire = GameObjects.closest("Fire");
        }

        Item logs = Inventory.get(logName);
        if (logs != null) {
            if (fire.exists()) {
                Logger.log("Using logs on fire.");
                useLogsOnFire(logs);
            } else {
                Logger.log("Fire no longer exists.");
                Logger.log("FIND_OPEN_SPOT");
                state = State.FIND_OPEN_SPOT;
            }
        } else {
            Logger.log("Failed to find " + logName + " in inventory.");
            logName = findLogInInventory();
            if (logName.isEmpty()) {
                state = State.WALKING_TO_BANK;
            }
        }
        return 500;
    }

    private String findLogInInventory() {
        for (String logName : logNames) {
            if (Inventory.contains(logName)) {
                return logName;
            }
        }
        return "";
    }

    private void useLogsOnFire(Item logs) {
        logs.useOn(fire);
        if (Sleep.sleepUntil(() -> {
            WidgetChild burn = Widgets.get(270, 15);
            return burn != null && burn.isVisible();
        }, 5000)) {
            Logger.log("Found WidgetChild, interacting.");
            WidgetChild burn = Widgets.get(270, 15);
            if (burn != null && burn.interact()) {
                Logger.log("Sleep on animating.");
                if (OneTapBuilder.sleepWhileAnimating(() -> Inventory.contains(logName) && !OneTapBuilder.isLevelUpVisible(), 30000, 500, 1000)) {
                    Logger.log("while statement evaluated to false.");
                } else {
                    Logger.log("Timeout hit.");
                }
            }
            if (Inventory.isEmpty()) {
                Logger.log("WALKING_TO_BANK");
                state = State.WALKING_TO_BANK;
            }
        } else {
            Logger.log("Timeout hit on finding burn widget.");
        }
    }

    private int walkToBank() {
        setLogName();
        if (OneTapBuilder.geTile.distance() > 10) {
            if (Walking.shouldWalk()) {
                Walking.walk(OneTapBuilder.geTile);
            }
        } else {
            Logger.log("RETRIEVE_WOOD");
            state = State.RETRIEVE_WOOD;
        }
        return 500;
    }

    private int retrieveWood() {
        if (Bank.open()) {
            if (!Inventory.contains("Tinderbox")) {
                if (!Sleep.sleepUntil(() -> Bank.withdraw("Tinderbox"), 5000)) {
                    Logger.error("Failed to withdraw tinderbox.");
                    OneTapBuilder.addItemToBuy("Tinderbox", 1, "Firemaker");
                    return 500;
                }
                if (!Sleep.sleepUntil(() -> Inventory.contains("Tinderbox"), 5000)) {
                    Logger.error("Did not find tinderbox in inventory.");
                    return -1;
                }
            }

            // withdraw logs
            if (Sleep.sleepUntil(() -> Bank.withdrawAll(logName), 5000)) {
                Logger.log("Withdrew logs.");
                Bank.close();
                Logger.log("FIND_OPEN_SPOT");
                state = State.FIND_OPEN_SPOT;
            } else {
                Logger.log("Failed to withdraw logs " + logName + ".");
                if (!Bank.contains(logName)) {
                    // TODO get exact number of logs needed so we don't overbuy
                    OneTapBuilder.addItemToBuy(logName, 100, "Firemaker");
                    logName = stepDownOneLog();
                    if (logName.isEmpty()) {
                        Logger.log("No usable logs.");
                    }
                }
            }
        }
        return 500;
    }

    private String stepDownOneLog () {
        int index = logNames.indexOf(logName);
        if (index != logNames.size()) {
            return logNames.get(index + 1);
        } else {
            return "";
        }
    }

    private int findOpenSpot() {
        GameObject campfire = GameObjects.closest("Forester's Campfire");
        if (campfire != null && campfire.distance() < 5) {
            Logger.log("FIND_FIRE");
            state = State.FIND_FIRE;
            return 0;
        } else {
            Logger.log("No nearby Forester's Campfire.");
        }
        List<GameObject> gos = GameObjects.all();
        List<Player> players = Players.all();
        Tile localTile = Players.getLocal().getTile();
        if (OneTapBuilder.anyOnTile(gos, localTile) || OneTapBuilder.anyOnTile(players, localTile)) {
            Logger.log("Tile occupied.");
            Tile newTile;
            if (Calculations.random(0, 2) == 0) {
                newTile = localTile.translate(-1, 0);
            } else {
                newTile = localTile.translate(0, 1);
            }

            if (Map.canReach(newTile)) {
                Logger.log("Moving to " + newTile + ".");
                Walking.walk(newTile);
            }
        } else {
            Logger.log("Unoccupied tile found.");
            state = State.FIND_FIRE;
        }
        return 500;
    }

    private void setLogName() {
        int firemakingSkill = Skills.getRealLevel(Skill.FIREMAKING);
        burnableLogs.clear();
        burnableLogs.add("Logs");

        logName = "Logs";
        if (firemakingSkill >= 60) {
            logName = "Yew logs";
            burnableLogs.add("Yew logs");
        } else if (firemakingSkill >= 45) {
            logName = "Maple logs";
            burnableLogs.add("Maple logs");
        } else if (firemakingSkill >= 30) {
            logName = "Willow logs";
            burnableLogs.add("Willow logs");
        } else if (firemakingSkill >= 15) {
            logName = "Oak logs";
            burnableLogs.add("Oak logs");
        }

        Logger.log("Current firemaking skill: " + firemakingSkill + ". Burning: " + logName + ".");
    }

    private boolean hasBurnableItems() {
        for (String log : burnableLogs) {
            if (Bank.contains(log) || Inventory.contains(log)) {
                return true;
            }
        }
        return false;
    }
}
