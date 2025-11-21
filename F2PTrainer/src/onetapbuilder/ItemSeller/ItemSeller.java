package onetapbuilder.ItemSeller;

import onetapbuilder.*;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemSeller extends TaskNode implements Resetable {
    private enum State {
        WALK_TO_BANK,
        RETRIEVE_ITEMS,
        CONFIRM_OPEN_SPOT,
        SELL_ITEM,
        WAIT_FOR_SELLING
    }
    private State state = State.WALK_TO_BANK;
    boolean init = false;
    List<String> sellableResources = new ArrayList<>();
    int sellingItems = 0;

    @Override
    public void reset() {
        state = State.WALK_TO_BANK;
    }

    private void init() {
        TaskScheduler.init("ItemSeller");
        sellableResources = ItemTracker.getSellableResources();
        OneTapBuilder.selling = true;
        init = true;
    }

    @Override
    public boolean accept() {
        return TaskScheduler.valid("ItemSeller");
    }

    @Override
    public int execute() {
        if (!init) {
            init();
        }
        switch (state) {
            case WALK_TO_BANK:
                return walkToBank();
            case RETRIEVE_ITEMS:
                return retrieveItems();
            case CONFIRM_OPEN_SPOT:
                return confirmOpenSpot();
            case SELL_ITEM:
                return sellItem();
            case WAIT_FOR_SELLING:
                return waitForSelling();
        }
        return 0;
    }

    private int waitForSelling() {
        if (GrandExchange.isReadyToCollect()) {
            GrandExchange.collectToBank();
            Logger.log("CONFIRM_OPEN_SPOT");
            state = State.CONFIRM_OPEN_SPOT;
        } else if (GrandExchange.getFirstOpenSlot() != -1) {
            Logger.log("CONFIRM_OPEN_SPOT");
            state = State.CONFIRM_OPEN_SPOT;
        } else {
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 10000);
        }
        return 500;
    }

    private int sellItem() {
        List<Item> inventoryItems = Inventory.all().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (inventoryItems.isEmpty()) {
            Logger.log("No items to sell left.");
            state = State.WAIT_FOR_SELLING;
            return 100;
        }
        Item itemToSell = inventoryItems.get(0);
        Logger.log("length: " + inventoryItems.size());

        if (itemToSell == null) {
            Logger.log("itemToSell is null.");
            return -1;
        }
        Logger.log("Selling " + itemToSell.getName() + " with live price " + itemToSell.getLivePrice() + " and amount " + itemToSell.getAmount());
        Sleep.sleepUntil(() -> {
            return GrandExchange.sellItem(itemToSell.getName(), itemToSell.getAmount(), (int) (itemToSell.getLivePrice() * .9));
        }, 5000);
        Logger.log("CONFIRM_OPEN_SPOT");
        state = State.CONFIRM_OPEN_SPOT;
        return 500;
    }

    private boolean doneSelling() {
        int usedSlots = GrandExchange.getUsedSlots();
        if (usedSlots == 0) {
            if (ItemTracker.getNumberSellableResources() == 0) {
                Logger.log("No more items to sell.");
                OneTapBuilder.selling = false;
                return true;
            } else {
                Logger.log("Waiting for selling to complete.");
                return false;
            }
        }
        return false;
    }

    private int confirmOpenSpot() {
        if (doneSelling()) {
            return 100;
        }
        if (GrandExchange.open()) {
            if (GrandExchange.getFirstOpenSlot() == -1) {
                Logger.log("WAIT_FOR_SELLING");
                state = State.WAIT_FOR_SELLING;
            } else {
                Logger.log("SELL_ITEM");
                state = State.SELL_ITEM;
            }
        }
        return 500;
    }

    private int retrieveItems() {
        if (Bank.open()) {
            Sleep.sleepUntil(Bank::depositAllItems, 5000);
            Bank.setWithdrawMode(BankMode.NOTE);
            for (String item : sellableResources) {
                if (Bank.contains(item)) {
                    Logger.log("Retrieving " + item + ".");
                    BotUtils.retrieveItem(item, true);
                    Sleep.sleepUntil(() -> Inventory.contains(item), 3000);
                    if (!Inventory.contains(item)) {
                        Logger.log("Failed to get item " + item);
                    }
                }
            }
            Logger.log("CONFIRM_OPEN_SPOT");
            state = State.CONFIRM_OPEN_SPOT;
        }
        return 500;
    }

    private int walkToBank() {
        if (BotUtils.geTile.distance() > 3) {
            if (Walking.shouldWalk()) {
                Walking.walk(BotUtils.geTile);
            }
        } else {
            Logger.log("RETRIEVE_ITEMS");
            state = State.RETRIEVE_ITEMS;
        }
        return 500;
    }
}
