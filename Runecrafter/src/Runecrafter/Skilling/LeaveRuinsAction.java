package Runecrafter.Skilling;

import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

public class LeaveRuinsAction {
    public static int execute(Skilling ctx) {
        Sleep.sleepUntil(() -> {
            GameObject portal = GameObjects.closest("Portal");
            return portal != null && portal.exists();
        }, 2000);
        GameObject portal = GameObjects.closest("Portal");
        Tile altarTile = ctx.getAltarTile();
        if ((portal != null && portal.exists() && portal.interact()) || altarTile.distance() < 10) {
            Sleep.sleepUntil(() -> altarTile.distance() < 10, 5000);
            if (altarTile.distance() < 10) {
                Logger.log("WALK_TO_BANK");
                ctx.setStateWalkToBank();
            } else {
                Logger.log("Failed to go through portal.");
            }
        } else {
            Logger.log("Failed to find portal.");
        }
        return 500;
    }
}
