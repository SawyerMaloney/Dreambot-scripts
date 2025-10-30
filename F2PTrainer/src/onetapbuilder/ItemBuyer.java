package onetapbuilder;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.script.listener.ItemContainerListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.items.Item;

import java.util.Iterator;
import java.util.Map;

public class ItemBuyer extends TaskNode {
    private enum State {
        GO_TO_VARROCK,
        GET_GOLD_AMOUNT,
        CANCEL_ALL_ORDERS,
        PLACE_ORDER,
        CHECK_FOR_NEXT_ORDER,
        WAIT_FOR_ORDERS
    }
    private static State state = State.GO_TO_VARROCK;
    private final Timer timer = new Timer(300_000);

    @Override
    public boolean accept() {
        return OneTapBuilder.valid("ItemBuyer");
    }

    @Override
    public int execute() {
        switch (state) {
            case GO_TO_VARROCK:
                return goToVarrock();
            case GET_GOLD_AMOUNT:
                return getGoldAmount();
            case CANCEL_ALL_ORDERS:
                return cancelAllOrders();
            case PLACE_ORDER:
                return placeOrder();
            case WAIT_FOR_ORDERS:
                return waitForOrders();
        }
        return 500;
    }

    private int waitForOrders() {
        if (!OneTapBuilder.areOrderedItems()) {
            Logger.log("No items to collect left.");
            return 500;
        }
        if (GrandExchange.isReadyToCollect()) {
            Logger.log("Order ready to collect.");
            GrandExchange.collect();
            Logger.log("PLACE_ORDER");
            state = State.PLACE_ORDER;
        } else {
            Logger.log("Nothing to collect. Sleeping...");
            if (GrandExchange.isReadyToCollect()) {
                return 10000;
            }
        }
        return 500;
    }

    private int placeOrder() {
        Iterator<Map.Entry<String, Integer>> iter = OneTapBuilder.neededItems();
        while (iter.hasNext() && GrandExchange.getFirstOpenSlot() != -1) {
            Map.Entry<String, Integer> item = iter.next();
            String itemName = item.getKey();
            int amount = item.getValue();
            int livePrice = LivePrices.getHigh(itemName);
            // going to increase the price so we don't get stuck
            int newPrice = (int) Math.ceil(livePrice * 1.10);

            if (Sleep.sleepUntil(() -> GrandExchange.buyItem(itemName, amount, newPrice), 10000)) {
                Logger.log("Order placed for " + itemName + " (" + amount + ") @ " + newPrice + " gp each.");
                OneTapBuilder.addOrderedItem(itemName);
            } else {
                Logger.log("Failed to put in buy order for " + itemName +".");
            }
        }
        Logger.log("WAIT_FOR_ORDERS");
        state = State.WAIT_FOR_ORDERS;
        return 500;
    }

    private int cancelAllOrders() {
        if (GrandExchange.open()) {
            if (Sleep.sleepUntil(GrandExchange::cancelAll, 5000)) {
                Logger.log("Canceled all GE orders.");
            } else {
                Logger.log("Failed to cancel all GE orders.");
            }
            if (Sleep.sleepUntil(() -> GrandExchange.collect() || GrandExchange.getFirstOpenSlot() == 0, 5000)) {
                Logger.log("Collected all GE orders.");
            } else {
                Logger.log("Failed to collect all GE orders.");
            }
            Logger.log("PLACE_ORDER");
            state = State.PLACE_ORDER;
        }
        return 500;
    }

    private int getGoldAmount() {
        if (Bank.open()) {
            int inv_gold = Inventory.count("Coins");
            int gold = Bank.count("Coins") + inv_gold;
            if (Bank.contains("Coins")) {
                Sleep.sleepUntil(() -> Bank.withdrawAll("Coins"), 5000);
            }
            Logger.log("You have " + gold + " coins.");
            OneTapBuilder.setGoldAmount(gold);
            Logger.log("CANCEL_ALL_ORDERS");
            state = State.CANCEL_ALL_ORDERS;
        }
        return 500;
    }

    private int goToVarrock() {
        if (OneTapBuilder.geTile.distance() > 10) {
            if (Walking.shouldWalk()) {
                Walking.walk(OneTapBuilder.geTile);
            }
        } else {
            Logger.log("GET_GOLD_AMOUNT");
            state = State.GET_GOLD_AMOUNT;
        }
        return 500;
    }


    public static void onInventoryItemAdded(Item item) {
        if (state == State.WAIT_FOR_ORDERS) {
            if (OneTapBuilder.isOrderedItem(item.getName())) {
                Logger.log("Item " + item.getName() + " removed from ordered items.");
                OneTapBuilder.removeOrderedItem(item.getName());
            }
        }
    }


    public static void onInventoryItemChanged(Item incoming, Item existing) {
        if (state == State.WAIT_FOR_ORDERS) {
            if (OneTapBuilder.isOrderedItem(existing.getName())) {
                Logger.log("Item " + existing.getName() + " removed from ordered items.");
                OneTapBuilder.removeOrderedItem(existing.getName());
            }
        }
    }
}
