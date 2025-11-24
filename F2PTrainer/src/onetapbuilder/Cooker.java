package onetapbuilder;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.ArrayList;
import java.util.List;

public class Cooker extends TaskNode implements Resetable, SellableProducer, ResourceNode {

    private final Tile bank_tile = new Tile(3094, 3489);
    private final Tile stove_tile = new Tile(3078, 3495);
    private boolean initialized = false;
    private final List<String> fishNames = new ArrayList<>();
    private boolean atBank = false;

    private boolean setupInventory = false;

    public void reset() {
        atBank = false;
        initialized = false;
    }

    private void init() {
        TaskScheduler.init("Cooker");
        findBestFish();
    }

    @Override
    public boolean accept() {
        return TaskScheduler.valid("Cooker");
    }

    @Override
    public int execute() {
        if (!hasCookableItems()) {
            ItemTracker.addItemToGather(fishNames.get(0), "Cooker");
        }
        if (!initialized) {
            init();
            initialized = true;
        } else if (!atBank) {
            if (bank_tile.distance() > 1) {
                if (Walking.shouldWalk()) {
                    Walking.walk(bank_tile);
                    return 500;
                }
                return 500;
            }
            atBank = true;
        } else {
            if (!setupInventory) {
                if (Bank.open()) {
                    // now grab fish
                    Bank.depositAllItems();
                    for (String fishName : fishNames) {
                        if (!Inventory.isFull() && Bank.contains(fishName)) {
                            Logger.log("Found fish: " + fishName);
                            BotUtils.retrieveItem(fishName, true);
                            Sleep.sleep(Calculations.random(1000, 1500));
                        } else {
                            if (!Inventory.isFull()) {
                                Logger.log("Did not find fish: " + fishName);
                            }
                        }
                    }
                    // if we don't have anything in our inventory, quit because we have no fish
                    if (Inventory.isEmpty()) {
                        Logger.log("Nothing to cook!");
                    } else if (inventoryHasRawFood()) {
                        // need this to know that we've reset our inventory fully
                        setupInventory = true;
                    }
                }
            } else {
                // cook fish
                if (stove_tile.distance() > 5) {
                    if (Walking.shouldWalk()) {
                        Walking.walk(stove_tile);
                    }
                    return 500;
                } else {
                    Sleep.sleepUntil(() -> {
                        GameObject stove = GameObjects.closest("Stove");
                        return stove != null && stove.exists() && stove.canReach();
                    }, 10000);
                    GameObject stove = GameObjects.closest("Stove");
                    if (stove != null && stove.exists() && stove.canReach()) {
                        Logger.log("interacting with stove");
                        if (stove.interact("Cook")) {
                            if (Sleep.sleepUntil(() -> {
                                WidgetChild cookWidget = Widgets.get(270, 15);
                                return cookWidget != null && cookWidget.isVisible();
                            }, 3000)) {
                                WidgetChild cookWidget = Widgets.get(270, 15);
                                if (cookWidget != null && cookWidget.interact()) {
                                    Logger.log("Started cooking all.");
                                    BotUtils.sleepWhileAnimating(this::inventoryHasRawFood, 60000, 3000, 10000);
                                    Logger.log("sleepWhile cooking loop broke.");
                                    if (!inventoryHasRawFood()) {
                                        // really done cooking
                                        Logger.log("Cooking finished.");
                                        findBestFish();
                                        setupInventory = false;
                                    }
                                }
                            } else {
                                if (Sleep.sleepUntil(() -> {
                                    WidgetChild cookWidget = Widgets.get(229, 3);
                                    return cookWidget != null && cookWidget.isVisible();
                                }, 5000)) {
                                    // "You haven't got anything to cook." Widget
                                    Logger.log("Cooking finished.");
                                    findBestFish();
                                    setupInventory = false;
                                }
                            }
                        } else {
                            Logger.log("Could not click cook widget.");
                            return 1000;
                        }
                    } else {
                        Logger.log("Couldn't not find stove.");
                        return 1000;
                    }
                }
            }
        }
        return 500;
    }

    private boolean inventoryHasRawFood() {
        for (String fishName : fishNames) {
            if (Inventory.contains(fishName)) {
                return true;
            }
        }
        return false;
    }

    private void findBestFish() {
        Logger.log("Finding cookable fish based on level.");
        int cookingSkill = Skills.getRealLevel(Skill.COOKING);

        if (cookingSkill >= 25) {
            fishNames.add("Raw salmon");
        }
        if (cookingSkill >= 15) {
            fishNames.add("Raw trout");
        }
        fishNames.add("Raw shrimps");
        fishNames.add("Raw anchovies");
    }

    // run script when we have items to cook--block if we have not initialized "fishNames" yet.
    private boolean hasCookableItems() {
        if (!fishNames.isEmpty()) {
            for (String fish : fishNames) {
                if (Bank.contains(fish) || Inventory.contains(fish)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<String> getSellableResources() {
        List<String> resources = new ArrayList<>();
        int cookingSkill = Skills.getRealLevel(Skill.COOKING);
        if (cookingSkill >= 25) {
            resources.add("Salmon");
        }
        if (cookingSkill >= 15) {
            resources.add("Trout");
        }
        resources.add("Shrimps");
        resources.add("Anchovies");
        return resources;
    }

    @Override
    public List<String> getProducedItems() {
        List<String> producedItems = new ArrayList<>();
        producedItems.add("Salmon");
        producedItems.add("Shrimps");
        producedItems.add("Anchovies");
        producedItems.add("Trout");
        return producedItems;
    }
}
