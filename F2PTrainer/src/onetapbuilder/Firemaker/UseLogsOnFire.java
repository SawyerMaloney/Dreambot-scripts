package onetapbuilder.Firemaker;

import onetapbuilder.BotUtils;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;

/**
 * Extracted logic from Firemaker.useLogsOnFire(Item) into its own helper class.
 */
public class UseLogsOnFire {
    public static void useLogsOnFire(Item logs) {
        logs.useOn(Firemaker.fire);
        if (Sleep.sleepUntil(() -> {
            WidgetChild burn = Widgets.get(270, 15);
            return burn != null && burn.isVisible();
        }, 5000)) {
            Logger.log("Found WidgetChild, interacting.");
            WidgetChild burn = Widgets.get(270, 15);
            if (burn != null && burn.interact()) {
                Logger.log("Sleep on animating.");
                if (BotUtils.sleepWhileAnimating(() -> Inventory.contains(Firemaker.logName) && !BotUtils.isLevelUpVisible(), 30000, 500, 1000)) {
                    Logger.log("while statement evaluated to false.");
                } else {
                    Logger.log("Timeout hit.");
                }
            }
            if (Inventory.isEmpty()) {
                Logger.log("WALKING_TO_BANK");
                Firemaker.state = Firemaker.State.WALKING_TO_BANK;
            }
        } else {
            Logger.log("Timeout hit on finding burn widget.");
        }
    }
}
