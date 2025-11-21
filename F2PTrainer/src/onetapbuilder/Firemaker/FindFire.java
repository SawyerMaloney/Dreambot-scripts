package onetapbuilder.Firemaker;

import onetapbuilder.BotUtils;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;

public class FindFire {
    public static int findFire() {
        GameObject fire = GameObjects.closest("Forester's Campfire");
        if (fire != null && fire.exists() && fire.distance() < 5) {
            Logger.log("Nearby Forester's Campfire found.");
            Firemaker.fire = fire;
            Logger.log("BURN_WOOD");
            Firemaker.state = Firemaker.State.BURN_WOOD;
            return 0;
        } else {
            if (fire == null) {
                Logger.log("Fire null");
            } else {
                Logger.log("null: false. exists: " + fire.exists() + ". distance: " + fire.distance());
            }
        }
        Item tinderbox = Inventory.get("Tinderbox");
        if (tinderbox != null) {
            Logger.log("Got tinderbox. Using on " + Firemaker.logName + ".");
            tinderbox.useOn(Firemaker.logName);
            BotUtils.sleepWhileAnimating(() -> true, 10000, 500, 1000);
            fire = GameObjects.closest("Fire");
            Logger.log("BURN_WOOD");
            Firemaker.state = Firemaker.State.BURN_WOOD;
        }
        return 500;
    }
}
