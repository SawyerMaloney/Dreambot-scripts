package onetapbuilder;

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
import org.dreambot.api.methods.interactive.Players;

public class Miner extends TaskNode {
    private boolean initialized = false;

    private String rock_name = "Tin rocks";
    private String pickaxe_name = "Black pickaxe";

    private final Tile tin_rock_tile = new Tile(3223, 3147);
    private final Tile iron_rock_tile = new Tile(3286, 3368);
    private Tile destination = tin_rock_tile;

    private final Tile iron_rock_to_skip = new Tile(3285, 3369);

    private void initialize() {
        Logger.log("Starting script...");
        setNames();
        Logger.log("Starting script with pick: " + pickaxe_name + ", ore: " +  rock_name);
        initialized = true;
    }

    @Override
    public boolean accept() {
        return TaskScheduler.valid("Miner");
    }

    @Override
    public int execute() {
        if (!initialized) {
            initialize();
        }
        if (!Players.getLocal().isAnimating()) {
            setNames();
            int status = ensureCorrectPick();
            if (status == -1) {
                return -1;
            } else if (status == 1) {  // we have the correct pickaxe; otherwise, we are getting it
                if (Inventory.isFull()) {
                    Inventory.dropAll("Tin ore", "Iron ore");
                }
                if (destination.distance() > 1) {
                    Walking.walk(destination);
                } else {
                    findInteractRock();
                }
            }
        }
        return 500 +  Calculations.random(0, 1000);
    }

    private int ensureCorrectPick() {
        if (!Inventory.contains(pickaxe_name)) {
            if (Bank.open()) {
                Bank.depositAllItems();
                Sleep.sleep(Calculations.random(500, 1000));
                Sleep.sleepUntil(() -> Bank.withdraw(pickaxe_name), 5000);
                Sleep.sleep(Calculations.random(1500));
                if (!Inventory.contains(pickaxe_name)) {
                    Logger.log("Failed to get pickaxe");
                    return -1;
                }
            }
            return 0;
        }
        return 1;
    }

    private void findInteractRock() {
        Logger.log("Finding new rock");
        Sleep.sleep(Calculations.random(200, 1000));
        GameObject rock = GameObjects.closest(rock_name);
        if (rock != null && rock.exists() && rock.canReach() && rock.distance(destination) < 2 && !rock.getTile().equals(iron_rock_to_skip)) {
            Logger.log("Rock real, reachable. Interacting. Index: " + rock.getIndex());
            rock.interact("Mine");
        }
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
