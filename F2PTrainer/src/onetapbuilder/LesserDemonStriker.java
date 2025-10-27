package onetapbuilder;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.methods.grandexchange.LivePrices;

import java.util.List;

public class LesserDemonStriker extends TaskNode {

    private final Tile demon_tile = new Tile(3111, 3159, 2);

    @Override
    public boolean accept() {
        return OneTapBuilder.canCast;
    }

    @Override
    public int execute() {
        Magic.setAutocastSpell(Normal.FIRE_STRIKE);
        if (demon_tile.distance() > 1) {
            Logger.log("Walking to tile.");
            Walking.walk(demon_tile);
        } else {
            Logger.log("Finding lesser demon...");
            NPC lesser_demon = NPCs.closest("Lesser demon");
            if (lesser_demon != null && lesser_demon.exists()) {
                if (Magic.canCast(Normal.FIRE_STRIKE)) {
                    Logger.log("Found lesser demon.");
                    lesser_demon.interact("Attack");
                    Sleep.sleepUntil(() -> !Players.getLocal().isInCombat(), 30000);
                    // check if drops--if valuable, pick up
                    Sleep.sleep(Calculations.random(1000, 1500));
                    getDrops();
                    return 1000;
                } else {
                    Logger.log("Cannot cast spell.");
                    OneTapBuilder.canCast = false;
                }
            } else {
                Logger.log("Cannot find lesser demon. Sleep until found.");
                Sleep.sleepUntil(() -> NPCs.closest("Lesser demon") != null, 10000);
            }
            return 500;
        }
        return 1000;
    }

    private void getDrops() {
        List<GroundItem> ground_items = GroundItems.all();
        Logger.log("Starting getDrops with " + ground_items.size() + " items.");
        for (GroundItem ground_item : ground_items) {
            Logger.log("Checking item " +  ground_item.getName() + " with live price " + LivePrices.get(ground_item.getName()) + " and amount " + ground_item.getAmount());
            if (LivePrices.get(ground_item.getName()) * ground_item.getAmount() > LivePrices.get("Law rune")) {
                Logger.log("Casting spell on item " + ground_item.getName() + ".");
                Magic.castSpellOn(Normal.TELEKINETIC_GRAB, ground_item);
            }
        }
    }
}