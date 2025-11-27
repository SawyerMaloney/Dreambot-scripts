package RunecrafterAbstractScript;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(name = "[One Tap] F2P Runecrafting Dreambot Script", description = "Simple F2P Runecrafting Script.", author = "sawyerm",
        version = 1.0, category = Category.MISC, image = "8Zl24YQ")


public class Runecrafter extends AbstractScript {
    // High-level mode of the script
    private enum Mode {QUESTING, SKILLING, MINIGAME}
    private Mode mode = Mode.SKILLING; // Default to current behavior

    // Skilling mode handler encapsulates skilling-specific state and logic
    private final Skilling skilling = new Skilling();

    // Mode getters/setters to allow switching top-level behavior
    public void setModeQuesting() { this.mode = Mode.QUESTING; }
    public void setModeSkilling() { this.mode = Mode.SKILLING; }
    public void setModeMinigame() { this.mode = Mode.MINIGAME; }
    public String getModeName() { return mode.name(); }

    @Override
    public void onStart() {
        skilling.chooseRune();
    }

    @Override
    public int onLoop() {
        switch (mode) {
            case SKILLING:
                return skilling.onLoop();
            case QUESTING:
                return onQuestingLoop();
            case MINIGAME:
                return onMinigameLoop();
        }
        return 0;
    }

    // Delegated action methods have been moved to their own classes/files.

    // === Top-level mode loops ===

    private int onQuestingLoop() {
        // Placeholder: implement questing logic here
        // For now, idle lightly
        Logger.log("[Mode=QUESTING] No questing logic implemented yet.");
        return 600;
    }

    private int onMinigameLoop() {
        // Placeholder: implement minigame logic here
        // For now, idle lightly
        Logger.log("[Mode=MINIGAME] No minigame logic implemented yet.");
        return 600;
    }
}