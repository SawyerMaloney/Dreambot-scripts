package Runecrafter;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import Runecrafter.Questing.Questing;
import Runecrafter.Skilling.Skilling;

@ScriptManifest(name = "[One Tap] F2P Runecrafting Dreambot Script", description = "Simple F2P Runecrafting Script.", author = "sawyerm",
        version = 1.0, category = Category.MISC, image = "8Zl24YQ")


public class Runecrafter extends AbstractScript {
    // High-level mode of the script
    private enum Mode {QUESTING, SKILLING, MINIGAME}
    private Mode mode = Mode.SKILLING; // Default to current behavior

    // Skilling mode handler encapsulates skilling-specific state and logic
    private final Skilling skilling = new Skilling();
    private final Questing questing = new Questing();

    // Mode getters/setters to allow switching top-level behavior
    public void setModeQuesting() {
        Logger.log("Setting mode to Questing.");
        this.mode = Mode.QUESTING;
    }
    public void setModeSkilling() { this.mode = Mode.SKILLING; }
    public void setModeMinigame() { this.mode = Mode.MINIGAME; }
    public String getModeName() { return mode.name(); }

    public Skilling getSkilling() {
        return skilling;
    }

    public Questing getQuesting() {
        return questing;
    }

    @Override
    public void onStart() {
        skilling.chooseRune();
    }

    @Override
    public int onLoop() {
        switch (mode) {
            case SKILLING:
                return skilling.onLoop(this);
            case QUESTING:
                return questing.onLoop();
            case MINIGAME:
                return onMinigameLoop();
        }
        return 0;
    }

    // Delegated action methods have been moved to their own classes/files.

    // === Top-level mode loops ===

    private int onMinigameLoop() {
        // Placeholder: implement minigame logic here
        // For now, idle lightly
        Logger.log("[Mode=MINIGAME] No minigame logic implemented yet.");
        return 600;
    }

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
}