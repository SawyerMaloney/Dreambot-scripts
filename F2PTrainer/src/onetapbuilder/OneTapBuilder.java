package onetapbuilder;

import org.dreambot.api.Client;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

@ScriptManifest(name = "[One Tap] F2P Account Builder", description = "Main controller to run the other scripts.", author = "sawyerm",
        version = 1.0, category = Category.MISC)


public class OneTapBuilder extends TaskScript implements ItemContainerListener {
    public static boolean canCast = true;

    public static final Tile geTile = new Tile(3162, 3487);
    private static int gold = 0;
    public final static boolean needGold = false;


    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
        setFailLimit(3);
        addNodes(new Init(), new ItemBuyer(), new Cooker(), new Fisher());
    }


    @Override
    public void onExit() {
        Logger.log("Script ended");
    }

    public static void setGoldAmount(int gold) {
        OneTapBuilder.gold = gold;
    }

    @Override
    public void onInventoryItemAdded(Item item) {
        InventoryManager.onInventoryItemAdded(item);
    }

    @Override
    public void onInventoryItemChanged(Item incoming, Item existing) {
        InventoryManager.onInventoryItemChanged(incoming, existing);
    }
}