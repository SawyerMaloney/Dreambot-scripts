package Runecrafter.Skilling;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

public class BuyRunesAction {
    public static int execute(Skilling ctx) {
        if (GrandExchange.open()) {
            if (!ctx.isPlacedOrder()) {
                int price = LivePrices.get("Pure essence");
                Item coinsItem = Bank.get("Coins");
                int coins = 0;
                if (coinsItem != null) {
                    coins += coinsItem.getAmount();
                }
                int runesToBuy = 500;
                if (coins / price < runesToBuy * price) {
                    runesToBuy = coins / price;
                }
                int openSlot = GrandExchange.getFirstOpenSlot();
                ctx.setOpenSlot(openSlot);
                GrandExchange.buyItem("Pure essence", runesToBuy, price);
                ctx.setPlacedOrder(true);
            } else {
                if (Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(ctx.getOpenSlot()), 15000)) {
                    ctx.setPlacedOrder(false);
                    GrandExchange.collect();
                    ctx.setStateWalkToBank();
                }
            }
        }
        return 500;
    }
}
