package HighAlcher;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.filter.impl.ItemFilter;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.items.Item;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ScriptManifest(name = "HighAlcher", description = "Smart High/low Alch bot to find profitable alchs, buy them on the GE, and then alch them.",
        author = "sawyerm",
        version = 1.0, category = Category.MAGIC)

public class HighAlchBot extends AbstractScript {

    Map<Integer, String> items = new HashMap<>();
    List<Integer> alchables = new ArrayList<>();
    List<TradingAlchs> chosenAlchables = new ArrayList<>();
    private final Tile varrockBank = new Tile(3164, 3487);
    int goldAmount = 0;

    // manifest flags/values
    final boolean lowAlch = true;
    final boolean members = false;
    final int openTradingSlots = 3;
    final int maxGoldUsage = 0;
    private final int staffOfFireID = 1387;
    private final int natureRuneID = 561;
    private final int fireRuneID = 554;
    private boolean onlyAlch = true; // skip buying--assume has runes and staff, just alch all non-runes

    private Timer minuteTimer;

    // script flags
    private boolean setup = true;  // get to varrock GE and deposit all items
    private boolean arrived = false;
    private boolean chooseItems = true;
    private boolean buyItems = true;
    private boolean hasFireStaff = false;


    @Override
    public void onStart() {
        minuteTimer = new Timer(60_000);
        try {
            getItemIds();
            findAlchables();
        } catch (Exception e) {
            Logger.log("Failed to get item ids from server.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public int onLoop() {
        if (minuteTimer.finished()) {
            findAlchables();
            minuteTimer.reset();
        }

        if (onlyAlch) {
            return alch();
        } else {
            if (setup) {
                return setup();
            } else if (chooseItems) {
                return chooseItems();
            } else if (buyItems) {
                return buyItems();
            } else {
                Logger.log("End of script");
                return -1;
            }
        }
    }

    private int alch() {
        Normal alchSpell = Normal.HIGH_LEVEL_ALCHEMY;
        if (lowAlch) alchSpell = Normal.LOW_LEVEL_ALCHEMY;

        List<Item> items = Inventory.all();
        for (Item item : items) {
            if (Magic.canCast(alchSpell)) {
                Logger.log("Casting " + alchSpell.toString() + " on " + item.toString());
                Magic.castSpellOn(alchSpell, item);
                Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 1500);
                Sleep.sleep(Calculations.random(100, 300));
            } else {
                Logger.log("Cannot cast spell.");
                return -1;
            }
        }
        return 1000;
    }

    private int buyItems() {
        Logger.log("In buyItems.");
        if (GrandExchange.open()) {
            for (TradingAlchs alch : chosenAlchables) {
                Logger.log("Buying item " + alch.toString());
                GrandExchange.buyItem(alch.getItemName(), alch.getAmtToBuy(), alch.getLivePrice());
                Sleep.sleep(Calculations.random(2000, 2500));
            }

            buyItems = false;
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 15000);
            return 1000;
        } else {
            return 500;
        }
    }

    private int chooseItems() {
        for (int id : alchables.subList(0, openTradingSlots)) {
            Item item = new Item(id, 1);
            TradingAlchs ta = new TradingAlchs(id, 0, 0, item.getLivePrice(), item.getName());
            int amtToBuy = (goldAmount / openTradingSlots) / ta.getLivePrice();
            ta.setAmtToBuy(amtToBuy);
            Logger.log("Adding: " + ta.toString());
            chosenAlchables.add(ta);
        }
        chooseItems = false;
        return 1000;
    }

    // Get to Varrock GE, empty inventory and get amount of gold
    private int setup() {
        if (varrockBank.distance() > 1 && !arrived) {
            if (Walking.shouldWalk()) {
                Logger.log("Walking to Varrock GE");
                Walking.walk(varrockBank);
            }
        } else {
            Logger.log("Arrived.");
            arrived = true;
            if (Bank.open()) {
                Bank.depositAllItems();
                Sleep.sleep(Calculations.random(1000, 1500));
                if (maxGoldUsage != 0) Bank.withdraw("Coins", maxGoldUsage);
                else Bank.withdrawAll("Coins");
                Sleep.sleep(Calculations.random(1000, 1500));
                Item coins = Inventory.get("Coins");
                if (coins == null) {
                    Logger.log("Didn't get any coins! Broke :)");
                    return -1;
                }
                goldAmount = coins.getAmount();
                if (!Players.getLocal().getEquipment().contains(new Item(staffOfFireID, 1))) {
                    Logger.log("Player does not have staff of fire equipped. Will look for SoF or runes.");
                    if (Bank.contains("Staff of fire")) {
                        Bank.withdraw("Staff of fire");
                    } else if (Bank.contains("Fire rune")) {
                        Bank.withdraw("Fire rune");
                    } else {
                        Logger.log("Failed to find Staff of fire or fire rune.");
                    }
                } else Logger.log("Has Staff of fire equipped.");
                Sleep.sleep(Calculations.random(1000, 1500));
                if (Bank.contains("Nature rune")) {
                    Bank.withdraw("Nature rune");
                } else {
                    Logger.log("Failed to find Nature rune.");
                }
                Logger.log("Exiting setup with " + goldAmount + " gp.");
                setup = false;
            }
        }
        return 1000;
    }

    // search our list of items to find profitable alchables
    private void findAlchables() {
        alchables.clear();
        int nature_rune_price = LivePrices.get("Nature rune");

        for (int id : items.keySet()) {
            Item item = new Item(id, 1);
            int highAlchValue = item.getHighAlchValue();
            int lowAlchValue = item.getLowAlchValue();
            int alchValue = highAlchValue;
            if (lowAlch) alchValue = lowAlchValue;
            if (alchValue > LivePrices.get(id) + nature_rune_price)  {
                if (!item.isMembersOnly() || members) {
                    alchables.add(id);
                }
            }
        }
    }

    private void getItemIds() throws Exception {
        URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/mapping");
        HttpURLConnection conn =  (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("User-Agent", "item tracker");
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) sb.append(line);
        in.close();

        JSONArray items = new JSONArray(sb.toString());
        for (int i = 0; i < items.length(); i++) {
            JSONObject obj = items.getJSONObject(i);
            int id = obj.getInt("id");
            String name = obj.getString("name");
            this.items.put(id, name);
        }
    }
}

class TradingAlchs {
    private int id;
    private int amtToBuy;
    private int amtBought;
    private int livePrice;
    private String itemName;

    public TradingAlchs(int id, int amtToBuy, int amtBought, int livePrice, String itemName) {
        this.id = id;
        this.amtToBuy = amtToBuy;
        this.amtBought = amtBought;
        this.livePrice = livePrice;
        this.itemName = itemName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getLivePrice() {
        return livePrice;
    }

    public void setLivePrice(int livePrice) {
        this.livePrice = livePrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAmtToBuy() {
        return amtToBuy;
    }

    public void setAmtToBuy(int amtToBuy) {
        this.amtToBuy = amtToBuy;
    }

    public int getAmtBought() {
        return amtBought;
    }

    public void setAmtBought(int amtBought) {
        this.amtBought = amtBought;
    }

    @Override
    public String toString() {
        return itemName + " (" + id + "): " + livePrice + " gp. Buying: " + amtToBuy + ". Bought: " + amtBought + ".";
    }
}