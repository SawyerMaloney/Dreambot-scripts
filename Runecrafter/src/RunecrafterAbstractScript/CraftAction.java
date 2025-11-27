package RunecrafterAbstractScript;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;

public class CraftAction {
    public static int execute(Skilling ctx) {
        Sleep.sleepUntil(() -> {
            GameObject altar = GameObjects.closest("Altar");
            return altar != null && altar.exists();
        }, 2000);
        GameObject altar = GameObjects.closest("Altar");
        if (altar != null && altar.exists() && altar.interact()) {
            if (!Inventory.contains("Pure essence")) {
                Logger.log("LEAVE_RUINS");
                ctx.setStateLeaveRuins();
            } else {
                Logger.log("Failed to convert all pure essence.");
            }
        } else {
            Logger.log("Failed to interact with altar.");
        }
        return 500;
    }
}
