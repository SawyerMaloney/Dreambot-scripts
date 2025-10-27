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

import java.util.AbstractMap;
import java.util.List;

public class Firemaker extends TaskNode {
    private enum State {
        WALKING_TO_BANK,
        RETRIEVE_WOOD,
        FIND_OPEN_SPOT,
        FIND_FIRE,
        BURN_WOOD
    }

    private boolean initialized = false;
    private String logName = "";
    private State state = State.WALKING_TO_BANK;

    private final Tile geTile = new Tile(3162, 3487);
    private final Tile burnTile = new Tile(3147, 3501);

    private GameObject fire;

    @Override
    public boolean accept() {
        return OneTapBuilder.valid("Firemaker");
    }

    @Override
    public int execute() {
        if (!initialized) {
            init();
        }
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

    private void init() {
        setLogName();
        initialized = true;
    }

    private int findFire() {
        GameObject fire = GameObjects.closest("Forester's Campfire");
        if (fire != null && fire.distance() < 10) {
            this.fire = fire;
            Logger.log("BURN_WOOD");
            state = State.BURN_WOOD;
            return 0;
        }
        Item tinderbox = Inventory.get("Tinderbox");
        if (tinderbox != null) {
            Logger.log("Got tinderbox. Using on " + logName + ".");
            tinderbox.useOn(logName);
            OneTapBuilder.sleepOnAnimating(() -> true, 10000, 500, 1000);
            Logger.log("BURN_WOOD");
            state = State.BURN_WOOD;
        }
        return 500;
    }

    private int burnWood() {
        if (Sleep.sleepUntil(() -> {
            GameObject fire = GameObjects.closest("Fire");
            GameObject campfire = GameObjects.closest("Forester's Campfire");
            return (fire != null && fire.distance() < 10) || (campfire != null && campfire.distance() < 10);
        }, 5000)) {
            Logger.log("Found nearby fire.");
            fire = OneTapBuilder.getClosest(GameObjects.all("Fire"));
            if (fire == null) {
                fire = GameObjects.closest("Forester's Campfire");
            }
            if (fire != null) {
                Item logs = Inventory.get(logName);
                if (logs != null) {
                    Logger.log("Using logs on fire.");
                    logs.useOn(fire);
                    if (Sleep.sleepUntil(() -> {
                        WidgetChild burn = Widgets.get(270, 15);
                        return burn != null && burn.isVisible();
                    }, 5000)) {
                        Logger.log("Found WidgetChild, interacting.");
                        WidgetChild burn = Widgets.get(270, 15);
                        if (burn != null && burn.interact()) {
                            Logger.log("Sleep on animating.");
                            OneTapBuilder.sleepOnAnimating(() -> Inventory.onlyContains("Tinderbox"), 30000, 500, 1000);
                        }

                        if (Inventory.isEmpty()) {
                            Logger.log("WALKING_TO_BANK");
                            state = State.WALKING_TO_BANK;
                        }
                    }
                } else {
                    Logger.log("Failed to find " + logName + " in inventory.");
                }
            }
        } else {
            // TODO possibly state change here?
            Logger.log("Failed to find close fire.");
        }
        return 500;
    }

    private int walkToBank() {
        if (geTile.distance() > 10) {
            if (Walking.shouldWalk()) {
                Walking.walk(geTile);
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
                    OneTapBuilder.addNeededItem(new AbstractMap.SimpleEntry<>("Tinderbox", 1));
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
            } else {
                Logger.log("Failed to withdraw logs.");
                if (!Bank.contains(logName)) {
                    // TODO get exact number of logs needed so we don't overbuy
                    OneTapBuilder.addNeededItem(new AbstractMap.SimpleEntry<>(logName, 100));
                }
            }
            Bank.close();
            Logger.log("FIND_OPEN_SPOT");
            state = State.FIND_OPEN_SPOT;
        }
        return 500;
    }

    private int findOpenSpot() {
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

        logName = "Logs";
        if (firemakingSkill >= 60) {
            logName = "Yew logs";
        } else if (firemakingSkill >= 45) {
            logName = "Maple logs";
        } else if (firemakingSkill >= 30) {
            logName = "Willow logs";
        } else if (firemakingSkill >= 15) {
            logName = "Oak logs";
        }

        Logger.log("Current firemaking skill: " + firemakingSkill + ". Burning: " + logName + ".");
    }
}
