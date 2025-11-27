package Runecrafter;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;

import java.util.Arrays;

public class BotUtils {
    public static boolean interactWidget(int ... ids) {
        Logger.log("interactWidget widgetId: " + Arrays.toString(ids));
        Sleep.sleepUntil(() -> {
            WidgetChild wc = Widgets.get(ids);
            return wc != null && wc.isVisible();
        }, 5000);
        WidgetChild wc = Widgets.get(ids);
        if (wc == null) {
            Logger.log("failed to find widget " + Arrays.toString(ids));
            return false;
        }
        Sleep.sleepUntil(wc::interact, 5000);
        Sleep.sleep(Calculations.random(500, 1000));
        return true;
    }

    public static boolean executeConversation(int[]... steps) {
        for (int[] ids : steps) {
            if (!interactWidget(ids)) {
                return false;
            }
        }
        return true;
    }
}
