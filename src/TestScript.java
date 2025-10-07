import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.utilities.Sleep;


@ScriptManifest(name = "First Script", description = "My First Script's First Description!", author = "sawyerdm",
                version = 1.0, category = Category.WOODCUTTING, image="")

public class TestScript extends AbstractScript {

    private final Tile destination = new Tile(3260, 3277);
    private boolean needs_bank = false;
    private boolean walking_back = false;

    @Override
    public void onStart() {
        Logger.log("F2P Bone and cowhide collector!");
    }

    @Override
    public int onLoop() {
        if (!Inventory.isFull()) {
            if (destination.distance() > 10 && Walking.shouldWalk()) {
                Logger.log("Walking back.");
                Walking.walk(destination);
            } else {
                GroundItem item = GroundItems.closest("Coins", "Cowhide", "Bones");
                if (item != null && item.exists()) {
                    Logger.log("Found item " + item.getName());
                    item.interact("Take");
                    Inventory.dropAll("Raw Beef");
                    Sleep.sleepUntil(() -> !item.exists(), 5000);
                } else {
                    Logger.log("No item found.");
                }
            }
        } else {
            if (Bank.open()) {
                Logger.log("Bank is open.");
                Bank.depositAllItems();
            }
        }
        return 1000;
    }

    @Override
    public void onExit() {
        Logger.log("Exiting Script.");
    }
}