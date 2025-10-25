package f2pVariedTrainer;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

public class Runecrafter extends TaskNode {
    private enum State {WALK_TO_BANK, BANKING, WALK_TO_ALTAR, USE_RUINS, CRAFT, LEAVE_RUINS}
    private final Tile air_altar_tile = new Tile(2987, 3292);
    private final Tile falador_bank_tile = new Tile(3012, 3355);
    private final Tile air_teleport_tile = new Tile(2841, 4830);
    private State state = State.WALK_TO_BANK;
    @Override
    public boolean accept() {
        return AIO_Scheduler.valid("Runecrafter");
    }

    @Override
    public int execute() {
        switch (state) {
            case WALK_TO_BANK:
                return walk_to_bank();

            case BANKING:
                return bank();

            case WALK_TO_ALTAR:
                return walk_to_altar();

            case USE_RUINS:
                return use_ruins();

            case CRAFT:
                return craft();

            case LEAVE_RUINS:
                return leave_ruins();
        }
        return 0;
    }

    private int leave_ruins() {
        Sleep.sleepUntil(() -> {
            GameObject portal = GameObjects.closest("Portal");
            return portal != null && portal.exists();
        }, 2000);
        GameObject portal =  GameObjects.closest("Portal");
        if (portal != null && portal.exists() && portal.interact()) {
            Sleep.sleepUntil(() -> air_altar_tile.distance() < 10, 5000);
            if (air_altar_tile.distance() < 10) {
                Logger.log("WALK_TO_BANK");
                state = State.WALK_TO_BANK;
            } else {
                Logger.log("Failed to go through portal.");
            }
        } else {
            Logger.log("Failed to find portal.");
        }
        return 500;
    }

    private int craft() {
        Sleep.sleepUntil(() -> {
            GameObject altar = GameObjects.closest("Altar");
            return altar != null && altar.exists();
        }, 2000);
        GameObject altar = GameObjects.closest("Altar");
        if (altar != null && altar.exists() && altar.interact()) {
            if (!Inventory.contains("Pure essence")) {
                Logger.log("LEAVE_RUINS");
                state = State.LEAVE_RUINS;
            } else {
                Logger.log("Failed to convert all pure essence.");
            }
        } else {
            Logger.log("Failed to interact with altar.");
        }
        return 500;
    }

    private int use_ruins() {
        Sleep.sleepUntil(() -> {
            GameObject ruin = GameObjects.closest("Mysterious ruins");
            return ruin != null && ruin.exists();
        }, 2000);
        GameObject ruin = GameObjects.closest("Mysterious ruins");
        if (ruin != null) {
            if (ruin.interact()) {
                Sleep.sleepUntil(() -> air_teleport_tile.distance() < 10, 3000);
                if (air_teleport_tile.distance() < 10) {
                    Logger.log("CRAFT");
                    state = State.CRAFT;
                } else {
                    Logger.log("Failed to teleport.");
                }
            } else {
                Logger.log("Failed to interact with mysterious ruins.");
            }
        } else {
            Logger.log("Failed to find mysterious ruins.");
        }
        return 500;
    }

    private int walk_to_altar() {
        if (air_altar_tile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(air_altar_tile);
            }
        } else {
            state = State.USE_RUINS;
        }
        return 500;
    }

    private int walk_to_bank() {
        if (falador_bank_tile.distance() > 5) {
            if (Walking.shouldWalk()) {
                Walking.walk(falador_bank_tile);
            }
        } else {
            state = State.BANKING;
        }
        return 500;
    }
    private int bank() {
        if (Bank.open()) {
            Bank.depositAllItems();
            if (!Inventory.contains("Air tiara") && !Inventory.contains("Air talisman") && !Equipment.contains("Air tiara")) {
                if (Bank.contains("Air tiara")) {
                    Logger.log("Retrieving air tiara.");
                    AIO_Scheduler.retrieveItem("Air tiara", false);
                    Sleep.sleepUntil(() -> Inventory.contains("Air tiara"), 1500);
                    Item air_tiara = Inventory.get("Air tiara");
                    if (air_tiara != null) {
                        air_tiara.interact("Wear");
                    }
                } else if (Bank.contains("Air talisman")) {
                    Logger.log("Retrieving Air talisman");
                    AIO_Scheduler.retrieveItem("Air talisman", false);
                } else {
                    Logger.log("Doesn't have talisman or tiara");
                    return -1;
                }
            }
            if (Bank.contains("Pure essence")) {
                AIO_Scheduler.retrieveItem("Pure essence", true);
                Logger.log("WALK_TO_ALTAR");
                state = State.WALK_TO_ALTAR;
            } else {
                Logger.log("No pure essence.");
                return -1;
            }
        }
        return 500;
    }
}
