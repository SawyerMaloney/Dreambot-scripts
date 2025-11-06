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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Fisher extends TaskNode {
    private boolean initialized = false;

    private final Tile small_net_tile = new Tile(3241, 3149);
    private final Tile fly_fishing_tile = new Tile(3108, 3433);
    private Tile destination = small_net_tile;
    private boolean fishing = false;
    private String rod_name = "";
    private boolean feathers = false;
    private String interact = "";
    private String fishing_spot_name = "";

    private static final Map<String, Integer> fishLevelRequirements = new HashMap<>();
    private static final Map<String, Tile> fishSpotMap = new HashMap<>();

    private void initialize() {
        Logger.log("Starting fishing bot...");
        setFishMaps();
        setNames();
        Logger.log("Starting script with rod " + rod_name + " and feathers " + feathers + ".");
        TaskScheduler.init("Fisher");
        initialized = true;
    }
    @Override
    public boolean accept() {
        return TaskScheduler.valid("Fisher");
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
            if (BotUtils.retrieveItem(rod_name, false) == -1) return -1;
            Sleep.sleep(Calculations.random(500, 1000));
            if (feathers) {
                if (BotUtils.retrieveItem("Feather", true) == -1) return -1;
                Sleep.sleep(Calculations.random(500, 1000));
            }
        }

        return 1000 + Calculations.random(100, 300);
    }

    private void fish() {
        Logger.log("At spot. Looking for fishing spot: " + fishing_spot_name + ".");
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
            Logger.log("Missing rod or feathers.");
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
        return 1000 + Calculations.random(100, 500);
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
        int currentReqSkill = 0;

        // check if we need to gather a particular fish, and if we have the skill to do so,
        // try to get the highest skill level fish we can, for max XP
        List<String> neededItems = ItemTracker.getFishableNeededItems();
        if (!neededItems.isEmpty()) {
            // check if we can fish them
            for (String fish : neededItems) {
                if (fishingSkill >= fishLevelRequirements.get(fish) && fishLevelRequirements.get(fish) > currentReqSkill) {
                    destination = fishSpotMap.get(fish);
                    currentReqSkill = fishLevelRequirements.get(fish);
                    if (destination == fly_fishing_tile) {
                        rod_name = "Fly fishing rod";
                        feathers = true;
                        interact = "Lure";
                        fishing_spot_name = "Rod Fishing spot";
                    } else {
                        rod_name = "Small fishing net";
                        feathers = false;
                        interact = "Net";
                        fishing_spot_name = "Fishing spot";
                    }
                }
            }
        } else {
            // if no fish is needed, fish the best we can
            if (fishingSkill >= 20) {
                rod_name = "Fly fishing rod";
                destination = fly_fishing_tile;
                feathers = true;
                fishing_spot_name = "Rod Fishing spot";
                interact = "Lure";
            } else {
                rod_name = "Small fishing net";
                destination = small_net_tile;
                feathers = false;
                fishing_spot_name = "Fishing spot";
                interact = "Net";
            }
        }

        Logger.log("Fishing spot: " + fishing_spot_name + ". Rod: " + rod_name);
    }

    private void setFishMaps() {
        fishLevelRequirements.put("Raw shrimps", 1);
        fishLevelRequirements.put("Raw anchovies", 1);
        fishLevelRequirements.put("Raw trout", 20);
        fishLevelRequirements.put("Raw salmon", 30);

        fishSpotMap.put("Raw shrimps", small_net_tile);
        fishSpotMap.put("Raw anchovies", small_net_tile);
        fishSpotMap.put("Raw trout", fly_fishing_tile);
        fishSpotMap.put("Raw salmon", fly_fishing_tile);
    }
}