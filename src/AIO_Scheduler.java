import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.skills.Skills;

@ScriptManifest(name = "AIO", description = "Main controller to run the other scripts.", author = "sawyerdm",
        version = 1.0, category = Category.MISC)


public class AIO_Scheduler extends AbstractScript {
    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
    }

    @Override
    public int onLoop() {
        return 0;
    }

    @Override
    public void onExit() {
        Logger.log("Scheduler exiting.");
    }
}
