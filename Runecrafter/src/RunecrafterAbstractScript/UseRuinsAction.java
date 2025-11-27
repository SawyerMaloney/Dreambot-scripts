package RunecrafterAbstractScript;

import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

public class UseRuinsAction {
    public static int execute(Skilling ctx) {
        Sleep.sleepUntil(() -> {
            GameObject ruin = GameObjects.closest("Mysterious ruins");
            return ruin != null && ruin.exists();
        }, 2000);
        GameObject ruin = GameObjects.closest("Mysterious ruins");
        if (ruin != null) {
            if (ruin.interact()) {
                Sleep.sleepUntil(() -> ctx.getTeleportTile().distance() < 10, 3000);
                if (ctx.getTeleportTile().distance() < 10) {
                    Logger.log("CRAFT");
                    ctx.setStateCraft();
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
}
