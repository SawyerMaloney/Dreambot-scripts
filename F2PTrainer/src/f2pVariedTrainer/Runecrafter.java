package f2pVariedTrainer;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
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

    private final Tile body_altar_tile = new Tile(3055, 3445);
    private final Tile edge_bank_tile = new Tile(3094, 3489);
    private final Tile body_teleport_tile = new Tile(2519, 4847);

    private Tile altar_tile = air_altar_tile;
    private Tile bank_tile = falador_bank_tile;
    private Tile teleport_tile = air_teleport_tile;

    String tiara_name = "Air tiara";
    String talisman_name = "Air talisman";

    private boolean initialized = false;

    private State state = State.WALK_TO_BANK;

    @Override
    public boolean accept() {
        return AIO_Scheduler.valid("Runecrafter");
    }

    @Override
    public int execute() {
        if (!initialized) {
            chooseRune();
            initialized = true;
        }

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

    private void chooseRune() {
        int rc = Skills.getRealLevel(Skill.RUNECRAFTING);

        if (rc >= 20) {
            altar_tile = body_altar_tile;
            bank_tile = edge_bank_tile;
            teleport_tile = body_teleport_tile;

            tiara_name = "Body tiara";
            talisman_name = "Body talisman";
        }

        Logger.log("Skill: " + rc + ". Tiara name: " + tiara_name);
    }

    private int leave_ruins() {
        Sleep.sleepUntil(() -> {
            GameObject portal = GameObjects.closest("Portal");
            return portal != null && portal.exists();
        }, 2000);
        GameObject portal =  GameObjects.closest("Portal");
        if ((portal != null && portal.exists() && portal.interact()) || altar_tile.distance() < 10) {
            Sleep.sleepUntil(() -> altar_tile.distance() < 10, 5000);
            if (altar_tile.distance() < 10) {
                AIO_Scheduler.updateInventories("Runecrafter");
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
                Sleep.sleepUntil(() -> teleport_tile.distance() < 10, 3000);
                if (teleport_tile.distance() < 10) {
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
        if (altar_tile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(altar_tile);
            }
        } else {
            state = State.USE_RUINS;
        }
        return 500;
    }

    private int walk_to_bank() {
        if (bank_tile.distance() > 5) {
            if (Walking.shouldWalk()) {
                Walking.walk(bank_tile);
            }
        } else {
            state = State.BANKING;
        }
        return 500;
    }
    private int bank() {
        if (Bank.open()) {
            Bank.depositAllItems();
            if (!Inventory.contains(tiara_name) && !Inventory.contains(talisman_name) && !Equipment.contains(tiara_name)) {
                if (Bank.contains(tiara_name)) {
                    Logger.log("Retrieving " + tiara_name + ".");
                    AIO_Scheduler.retrieveItem(tiara_name, false);
                    Sleep.sleepUntil(() -> Inventory.contains(tiara_name), 1500);
                    Item air_tiara = Inventory.get(tiara_name);
                    if (air_tiara != null) {
                        air_tiara.interact("Wear");
                    }
                } else if (Bank.contains(talisman_name)) {
                    Logger.log("Retrieving " +  talisman_name + ".");
                    AIO_Scheduler.retrieveItem(talisman_name, false);
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
