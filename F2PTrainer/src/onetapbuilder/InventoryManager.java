package onetapbuilder;

import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

public class InventoryManager {
    public static void onInventoryItemAdded(Item item) {
        ItemBuyer.onInventoryItemAdded(item);

        if (ItemTracker.isItemToGather(item.getName())) {
            Logger.log("Item " + item.getName() + " removed from items to gather.");
            ItemTracker.removeGatherItem(item.getName());
        }

        if (ItemTracker.isTaskRequiredItem(item.getName())) {
            Logger.log("Item " + item.getName() + " removed from task required items.");
            ItemTracker.removeTaskRequiredItem(item.getName());
        }
    }

    public static void onInventoryItemChanged(Item incoming, Item existing) {
        ItemBuyer.onInventoryItemChanged(incoming, existing);
    }
}
