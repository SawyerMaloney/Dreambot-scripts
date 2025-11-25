package onetapbuilder.Firemaker;

import onetapbuilder.ItemTracker;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.Arrays;
import java.util.List;

/**
 * Extracted logic from Firemaker.retrieveWood() into a standalone helper.
 * Behavior preserved; interacts via Firemaker.state and Firemaker.logName.
 */
public class RetrieveWood {

    // Local ordering for stepping down log types, from best to worst
    private static final List<String> LOG_ORDER = Arrays.asList(
            "Yew logs", "Maple logs", "Willow logs", "Oak logs", "Logs"
    );

    public static int retrieveWood() {
        if (Bank.open()) {
            Sleep.sleepUntil(() -> Bank.depositAllExcept(item ->
                    item.getName().equals("Tinderbox") || item.getName().equals(Firemaker.logName)
                    && !item.isNoted()), 5000);

            if (!Inventory.contains("Tinderbox")) {
                if (!Sleep.sleepUntil(() -> Bank.withdraw("Tinderbox"), 5000)) {
                    Logger.error("Failed to withdraw tinderbox.");
                    ItemTracker.addItem("Tinderbox", "Firemaker", 1);
                    return 500;
                }
                if (!Sleep.sleepUntil(() -> Inventory.contains("Tinderbox"), 5000)) {
                    Logger.error("Did not find tinderbox in inventory.");
                    return 0;
                }
            }

            // withdraw logs
            if (Sleep.sleepUntil(() -> Bank.withdrawAll(Firemaker.logName), 5000)) {
                Logger.log("Withdrew logs.");
                Bank.close();
                Logger.log("FIND_OPEN_SPOT");
                Firemaker.state = Firemaker.State.FIND_OPEN_SPOT;
            } else {
                Logger.log("Failed to withdraw logs " + Firemaker.logName + ".");
                if (!Bank.contains(Firemaker.logName)) {
                    // TODO get exact number of logs needed so we don't overbuy
                    ItemTracker.addItem(Firemaker.logName, "Firemaker", 100);
                    Firemaker.logName = stepDownOneLog(Firemaker.logName);
                    if (Firemaker.logName.isEmpty()) {
                        Logger.log("No usable logs.");
                    }
                }
            }
        }
        return 500;
    }

    private static String stepDownOneLog(String current) {
        int idx = LOG_ORDER.indexOf(current);
        if (idx >= 0 && idx < LOG_ORDER.size() - 1) {
            return LOG_ORDER.get(idx + 1);
        }
        return "";
    }
}
