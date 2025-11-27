package RunecrafterAbstractScript;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

public final class Skilling {
    // Skilling-specific state machine
    public enum State {WALK_TO_BANK, BANKING, WALK_TO_ALTAR, USE_RUINS, CRAFT, LEAVE_RUINS, BUY_RUNES}

    private State state = State.WALK_TO_BANK;

    // Skilling-specific configuration and runtime data
    private final Tile air_altar_tile = new Tile(2987, 3292);
    private final Tile falador_bank_tile = new Tile(3012, 3355);
    private final Tile air_teleport_tile = new Tile(2841, 4830);

    private final Tile body_altar_tile = new Tile(3055, 3445);
    private final Tile edge_bank_tile = new Tile(3094, 3489);
    private final Tile body_teleport_tile = new Tile(2519, 4847);

    private Tile altar_tile = air_altar_tile;
    private Tile bank_tile = falador_bank_tile;
    private Tile teleport_tile = air_teleport_tile;

    private String tiara_name = "Air tiara";
    private String talisman_name = "Air talisman";

    private boolean placedOrder = false;
    private int openSlot = 0;

    public Skilling() { }

    // Instance loop for skilling
    public int onLoop() {
        switch (state) {
            case WALK_TO_BANK:
                return WalkToBankAction.execute(this);
            case BANKING:
                return BankAction.execute(this);
            case WALK_TO_ALTAR:
                return WalkToAltarAction.execute(this);
            case USE_RUINS:
                return UseRuinsAction.execute(this);
            case CRAFT:
                return CraftAction.execute(this);
            case LEAVE_RUINS:
                return LeaveRuinsAction.execute(this);
            case BUY_RUNES:
                return BuyRunesAction.execute(this);
        }
        return 0;
    }

    // Skilling utilities and configuration
    public void chooseRune() {
        int rc = Skills.getRealLevel(Skill.RUNECRAFTING);

        if (rc >= 20) {
            altar_tile = body_altar_tile;
            bank_tile = edge_bank_tile;
            teleport_tile = body_teleport_tile;

            tiara_name = "Body tiara";
            talisman_name = "Body talisman";
        } else {
            altar_tile = air_altar_tile;
            bank_tile = falador_bank_tile;
            teleport_tile = air_teleport_tile;

            tiara_name = "Air tiara";
            talisman_name = "Air talisman";
        }

        Logger.log("Skill: " + rc + ". Tiara name: " + tiara_name);
    }

    public static int retrieveItem(String item, boolean all) {
        if (all) {
            Sleep.sleepUntil(() -> Bank.withdrawAll(item), 5000);
        } else {
            Sleep.sleepUntil(() -> Bank.withdraw(item), 5000);
        }
        Sleep.sleepUntil(() -> Inventory.contains(item), 5000);
        if (!Inventory.contains(item)) {
            Logger.log("Failed to get item " + item);
            return -1;
        }
        return 1;
    }

    // Getters and setters for actions
    public Tile getAltarTile() { return altar_tile; }
    public Tile getBankTile() { return bank_tile; }
    public Tile getTeleportTile() { return teleport_tile; }

    public void setAltarTile(Tile t) { this.altar_tile = t; }
    public void setBankTile(Tile t) { this.bank_tile = t; }
    public void setTeleportTile(Tile t) { this.teleport_tile = t; }

    public String getTiaraName() { return tiara_name; }
    public String getTalismanName() { return talisman_name; }
    public void setTiaraName(String name) { this.tiara_name = name; }
    public void setTalismanName(String name) { this.talisman_name = name; }

    public boolean isPlacedOrder() { return placedOrder; }
    public void setPlacedOrder(boolean placedOrder) { this.placedOrder = placedOrder; }
    public int getOpenSlot() { return openSlot; }
    public void setOpenSlot(int openSlot) { this.openSlot = openSlot; }

    public State getState() { return state; }
    public void setStateWalkToBank() { this.state = State.WALK_TO_BANK; }
    public void setStateBanking() { this.state = State.BANKING; }
    public void setStateWalkToAltar() { this.state = State.WALK_TO_ALTAR; }
    public void setStateUseRuins() { this.state = State.USE_RUINS; }
    public void setStateCraft() { this.state = State.CRAFT; }
    public void setStateLeaveRuins() { this.state = State.LEAVE_RUINS; }
    public void setStateBuyRunes() { this.state = State.BUY_RUNES; }
}
