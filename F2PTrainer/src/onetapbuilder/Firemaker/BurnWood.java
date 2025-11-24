package onetapbuilder.Firemaker;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

/**
 * Extracted logic from Firemaker.burnWood() into its own helper class.
 */
public class BurnWood {

    private static final String[] LOG_ORDER = new String[]{
            "Yew logs", "Maple logs", "Willow logs", "Oak logs", "Logs"
    };

    public static int burnWood() {
        if (Firemaker.fire == null) {
            Logger.log("Fire null, finding closest fire.");
            Firemaker.fire = GameObjects.closest("Fire");
        }

        Item logs = Inventory.get(Firemaker.logName);
        if (logs != null) {
            if (Firemaker.fire != null && Firemaker.fire.exists()) {
                Logger.log("Using logs on fire.");
                UseLogsOnFire.useLogsOnFire(logs);
            } else {
                Logger.log("Fire no longer exists.");
                Logger.log("FIND_OPEN_SPOT");
                Firemaker.state = Firemaker.State.FIND_OPEN_SPOT;
                return 0;
            }
        } else {
            Logger.log("Failed to find " + Firemaker.logName + " in inventory.");
            Firemaker.logName = findLogInInventory();
            if (Firemaker.logName.isEmpty()) {
                Firemaker.state = Firemaker.State.WALKING_TO_BANK;
            }
        }
        return 500;
    }

    private static String findLogInInventory() {
        for (String name : LOG_ORDER) {
            if (Inventory.contains(name)) {
                return name;
            }
        }
        return "";
    }
}
