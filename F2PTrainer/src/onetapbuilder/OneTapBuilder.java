package onetapbuilder;

import onetapbuilder.ItemSeller.ItemSeller;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;

@ScriptManifest(name = "[One Tap] F2P Account Builder", description = "Main controller to run the other scripts.", author = "sawyerm",
        version = 1.0, category = Category.MISC)


public class OneTapBuilder extends TaskScript implements ItemContainerListener {
    public static boolean canCast = true;

    private static int gold = 0;
    public static boolean selling = false;

    public static List<TaskNode> nodes = new ArrayList<>();


    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
        setFailLimit(3);
        nodes.add(new Init());
        nodes.add(new ItemSeller());
        nodes.add(new ItemBuyer());
        nodes.add(new Fisher());
        nodes.add(new Cooker());
        nodes.add(new TaskClearer());

        for (TaskNode node : nodes) {
            addNodes(node);
        }
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