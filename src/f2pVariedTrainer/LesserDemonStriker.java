package f2pVariedTrainer;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
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

public class LesserDemonStriker extends TaskNode {

    private final Tile demon_tile = new Tile(3111, 3159, 2);

    @Override
    public boolean accept() {
        return AIO_Scheduler.canCast;
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
                    Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), 5000);
                    return 1000;
                } else {
                    Logger.log("Cannot cast spell.");
                    AIO_Scheduler.canCast = false;
                }
            } else {
                Logger.log("Cannot find lesser demon. Sleep until found.");
                Sleep.sleepUntil(() -> NPCs.closest("Lesser demon") != null, 10000);
            }
            return 500;
        }
        return 1000;
    }
}