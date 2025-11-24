package onetapbuilder.Firemaker;

import onetapbuilder.*;
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

public class Firemaker extends TaskNode implements Resetable, ResourceNode {
    @Override
    public List<String> getProducedItems() {
        return Collections.emptyList();
    }

    enum State {
        WALKING_TO_BANK,
        RETRIEVE_WOOD,
        FIND_OPEN_SPOT,
        FIND_FIRE,
        BURN_WOOD
    }

    static String logName = "";
    static State state = State.WALKING_TO_BANK;

    static GameObject fire;

    private final List<String> logNames = Arrays.asList("Yew logs", "Maple logs", "Willow logs", "Oak logs", "Logs");
    private final List<String> burnableLogs = new ArrayList<>();

    private boolean init = false;

    @Override
    public boolean accept() {
        return TaskScheduler.valid("Firemaker");
    }

    @Override
    public void reset() {
        state = State.WALKING_TO_BANK;
    }

    private void init() {
        TaskScheduler.init("Firemaker");
        init = true;
    }

    @Override
    public int execute() {
        if (!init) {
            init();
        }
        switch (state) {
            case WALKING_TO_BANK:
                return walkToBank();
            case RETRIEVE_WOOD:
                return RetrieveWood.retrieveWood();
            case FIND_OPEN_SPOT:
                return FindOpenSpot.findOpenSpot();
            case FIND_FIRE:
                return FindFire.findFire();
            case BURN_WOOD:
                return burnWood();
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
                if (BotUtils.sleepWhileAnimating(() -> Inventory.contains(logName) && !BotUtils.isLevelUpVisible(), 30000, 500, 1000)) {
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
        if (BotUtils.geTile.distance() > 10) {
            if (Walking.shouldWalk()) {
                Walking.walk(BotUtils.geTile);
            }
        } else {
            Logger.log("RETRIEVE_WOOD");
            state = State.RETRIEVE_WOOD;
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
