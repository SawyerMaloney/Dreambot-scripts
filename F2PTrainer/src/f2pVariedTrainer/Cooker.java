package f2pVariedTrainer;

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
import java.util.List;

public class Cooker extends TaskNode {

    private final Tile bank_tile = new Tile(3094, 3489);
    private final Tile stove_tile = new Tile(3078, 3495);
    private boolean initialized = false;
    private List<String> fishNames = new ArrayList<>();

    private boolean setupInventory = false;

    @Override
    public boolean accept() {
        Logger.log("Accept: " + AIO_Scheduler.valid("Cooker"));
        return AIO_Scheduler.valid("Cooker");
    }

    @Override
    public int execute() {
        if (!initialized) {
            findBestFish();
            initialized = true;
        }
        if (bank_tile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Logger.log("Walking to varrock");
                Walking.walk(bank_tile);
                return 1000;
            }
        } else {
            if (!setupInventory && Bank.open()) {
                if (Bank.contains("Tinderbox", "Logs")) {
                    if (!Inventory.contains("Tinderbox")) {
                        AIO_Scheduler.retrieveItem("Tinderbox", false);
                        Sleep.sleep(Calculations.random(500, 1500));
                    }
                    if (!Inventory.contains("Logs")) {
                        AIO_Scheduler.retrieveItem("Logs", false);
                    }

                    // now grab fish
                    for (String fishName : fishNames) {
                        if (!Inventory.isFull() && Bank.contains(fishName)) {
                            AIO_Scheduler.retrieveItem(fishName, true);
                        }
                    }
                    setupInventory = true;
                }
            } else {
                // cook fish
                if (stove_tile.distance() > 5) {
                    if (Walking.shouldWalk()) {
                        Logger.log("Walking to stove.");
                    }
                } else {
                    GameObject stove = GameObjects.closest("Stove");
                    if (stove != null && stove.exists() && stove.canReach()) {
                        String[] actions = stove.getActions();
                        for (String action : actions) {
                            Logger.log("Action: " + action + ".");
                        }
                    }
                }
            }
        }
        return 0;
    }

    private void findBestFish() {
        int cookingSkill = Skills.getRealLevel(Skill.COOKING);

        if (cookingSkill < 15) {
            fishNames.add("Raw shrimp");
            fishNames.add("Raw anchovies");
        } else {
            fishNames.add("Raw trout");
            if (cookingSkill >= 25) {
                fishNames.add("Raw salmon");
            }
        }
    }
}
