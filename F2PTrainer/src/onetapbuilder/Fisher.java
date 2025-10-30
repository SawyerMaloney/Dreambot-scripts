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
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.methods.interactive.Players;

import java.util.List;

public class Fisher extends TaskNode {
    private boolean initialized = false;

    private final Tile small_net_tile = new Tile(3241, 3149);
    private final Tile fly_fishing_tile = new Tile(3108, 3433);
    private Tile destination = small_net_tile;
    private boolean fishing = false;
    private String rod_name = "Small fishing net";
    private boolean feathers = false;
    private String interact = "Net";
    private String fishing_spot_name = "Fishing spot";

    private void initialize() {
        Logger.log("Starting fishing bot...");
        setNames();
        Logger.log("Starting script with rod " + rod_name + " and feathers " + feathers + ".");
        initialized = true;
    }
    @Override
    public boolean accept() {
        return OneTapBuilder.valid("Fisher");
    }

    private int retrieveItems() {
        if (Bank.open()) {
            Sleep.sleep(Calculations.random(0, 100));
            if (feathers) {
                Bank.depositAllExcept("Feather", rod_name);
            } else {
                Bank.depositAllExcept(rod_name);
            }
            Sleep.sleep(Calculations.random(500, 1000));
            if (OneTapBuilder.retrieveItem(rod_name, false) == -1) return -1;
            Sleep.sleep(Calculations.random(500, 1000));
            if (feathers) {
                if (OneTapBuilder.retrieveItem("Feather", true) == -1) return -1;
                Sleep.sleep(Calculations.random(500, 1000));
            }
        }

        return 500 + Calculations.random(100, 300);
    }

    private void fish() {
        Logger.log("At spot. Looking for fishing spot.");
        fishing = true;
        NPC fishing_spot = NPCs.closest(fishing_spot_name);
        Sleep.sleep(Calculations.random(100, 500));

        if (fishing_spot != null && fishing_spot.exists() && fishing_spot.canReach()) {
            Logger.log("Found fishing spot");
            fishing_spot.interact(interact);
            Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 5000);
        } else {
            Logger.log("No fishing spot found.");
            fishing = false;
        }
    }

    @Override
    public int execute() {
        if (!initialized) {
            Logger.log("Initializing fisher.");
            initialize();
        }
        if (!Inventory.contains(rod_name) || (feathers && !Inventory.contains("Feather"))) {
            Logger.log("Missing rod, feathers, or axe and tinderbox.");
            return retrieveItems();
        }
        else if (!Inventory.isFull()) {
            if (!Players.getLocal().isAnimating()) {
                if (destination.distance() > 10 && !fishing) {
                    walkToSpot();
                } else {
                    fish();
                }
            }
            else {
                Logger.log("Busy fishing...");
                return 5000;
            }
        } else {
            Logger.log("Full inventory. Cooking or banking fish.");
            fishing = false;
            return bankFish();
        }
        return 500 + Calculations.random(100, 500);
    }

    private int bankFish() {
        if (Bank.open()) {
            if (feathers) {
                Bank.depositAllExcept(rod_name, "Feather");
            } else {
                Bank.depositAllExcept(rod_name);
            }
        }

        return 1000;
    }

    private void walkToSpot() {
        if (Walking.shouldWalk()) {
            Logger.log("Walking to spot...");
            Walking.walk(destination);
        } else {
            Logger.log("Shouldn't walk. Waiting...");
        }
    }

    private void setNames() {
        int fishingSkill = Skills.getRealLevel(Skill.FISHING);

        // check if we need low level fish (and only low level fish)
        List<String> neededItems = OneTapBuilder.getFishableNeededItems();
        if (!neededItems.isEmpty()
            && !neededItems.contains("Raw salmon")
            && !neededItems.contains("Raw trout")) {
            return;
        }

        // if no fish is needed, fish the best we can
        if (fishingSkill >= 20) {
            rod_name = "Fly fishing rod";
            destination = fly_fishing_tile;
            feathers = true;
            fishing_spot_name = "Rod fishing spot";
            interact = "Lure";
        }
    }


}
