package onetapbuilder;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Locatable;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

public class BotUtils {
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


    public static boolean sleepWhileAnimating(BooleanSupplier returnPredicate, int timeout, int randomLower, int randomUpper) {
        AtomicLong lastAnimationTime = new AtomicLong(System.currentTimeMillis());
        return Sleep.sleepWhile(() -> {
            if (Players.getLocal().isAnimating()) {
                lastAnimationTime.set(System.currentTimeMillis());
            }
            return returnPredicate.getAsBoolean() && (Players.getLocal().isAnimating() || System.currentTimeMillis() - lastAnimationTime.get() < 2000);
        }, timeout + Calculations.random(randomLower, randomUpper));
    }

    public static boolean anyOnTile(List<? extends Locatable> entities, Tile tile) {
        for (Locatable entity : entities) {
            if (entity.getTile().equals(tile) && !entity.equals(Players.getLocal())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isLevelUpVisible() {
        WidgetChild w = Widgets.get(233, 1);
        return w != null && w.isVisible();
    }

}
