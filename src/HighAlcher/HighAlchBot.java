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
import java.util.*;
import java.util.stream.Collectors;

@ScriptManifest(name = "HighAlcher", description = "Smart High/low Alch bot to find profitable alchs, buy them on the GE, and then alch them.",
        author = "sawyerm",
        version = 1.0, category = Category.MAGIC)

public class HighAlchBot extends AbstractScript {
    // manifest flags/values
    final boolean lowAlch = true;
    final boolean members = false;
    final int openTradingSlots = 3;
    final int maxGoldUsage = 0;
    final boolean onlyAlch = true;
    final int staffOfFireID = 1387;
    final int natureRuneID = 561;

    private Timer minuteTimer;

    // script flags
    private boolean setup = true;  // get to varrock GE and deposit all items
    private boolean arrived = false;
    private boolean chooseItems = true;
    private boolean buyItems = true;
    private boolean buyNatureRunes = false;
    private boolean buyOrderPlacedNatureRunes = false;

    Map<Integer, String> items = new HashMap<>();
    List<Integer> alchables = new ArrayList<>();
    List<TradingAlchs> chosenAlchables = new ArrayList<>();
    private final Tile varrockBank = new Tile(3164, 3487);
    int goldAmount = 0;
    Item natureRune = new Item(natureRuneID, 1);
    int natureRunesNeeded = 0;
    int currentNatureRunes = 0;



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

        // skip buying--assume has runes and staff, just alch all non-runes

        if (onlyAlch) {
            return alch();
        } else {
            if (setup) {
                return setup();
            } else if (chooseItems) {
                return chooseItems();
            } else if (buyNatureRunes) {
                return buyNatureRunes();
            } else if (buyItems) {
                return buyItems();
            } else {
                List<Item> items = getInventoryAlchables();
                if (items != null && !items.isEmpty()) {
                    return alch(items);
                } else {
                    return claimItems();
                }
            }
        }
    }

    private int claimItems() {
        Sleep.sleepUntil(GrandExchange::isReadyToCollect, 15000);
        if (GrandExchange.isReadyToCollect()) {
            if (GrandExchange.open()) {
                GrandExchange.collect();
            }
        } else {
            Logger.log("GE not ready to collect yet.");
        }
        return 500;
    }

    private List<Item> getInventoryAlchables() {
        return Inventory.all().stream()
                .filter(Objects::nonNull)
                .filter(item -> !item.getName().equals("Nature rune"))
                .filter(item -> !item.getName().equals("Fire rune"))
                .filter(item -> !item.getName().equals("Coins"))
                .collect(Collectors.toList());
    }

    private int alch() {
        List<Item> items = getInventoryAlchables();
        return alch(items);
    }

    private int alch(List<Item> items) {

        Normal alchSpell = Normal.HIGH_LEVEL_ALCHEMY;
        if (lowAlch) alchSpell = Normal.LOW_LEVEL_ALCHEMY;

        if (items.isEmpty()) {
            Logger.log("Finished high alching.");
            return -1;
        }

        for (Item item : items) {
            for (int i = 0; i < item.getAmount(); i++) {
                if (Magic.canCast(alchSpell)) {
                    Logger.log("Casting " + alchSpell.toString() + " on " + item.getName());
                    Magic.castSpellOn(alchSpell, item);
                    Sleep.sleepUntil(() -> !Players.getLocal().isAnimating(), () -> false,5000, 50, 10);
                    Sleep.sleep(Calculations.random(100, 300));
                } else {
                    Logger.log("Cannot cast spell.");
                    return -1;
                }
            }
        }
        return 1000;
    }

    private int buyNatureRunes() {
        Logger.log("Buying nature runes.");
        if (GrandExchange.open()) {
            if (!buyOrderPlacedNatureRunes) {
                GrandExchange.buyItem(natureRuneID, natureRunesNeeded, natureRune.getLivePrice());
                buyOrderPlacedNatureRunes = true;
            }
            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 5000);
            if (GrandExchange.isReadyToCollect()) {
                GrandExchange.collect();
                Sleep.sleep(Calculations.random(1000, 1500));
                Item natureRunesInventory = Inventory.get("Nature rune");
                if (natureRunesInventory == null) {
                    Logger.log("In buyNatureRunes, failed to get nature runes.");
                    return -1;
                }
                natureRunesNeeded -= natureRunesInventory.getAmount() - currentNatureRunes;
                currentNatureRunes = natureRunesInventory.getAmount();
                if (natureRunesNeeded == 0) {
                    buyNatureRunes = false;
                }
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
            return 1000;
        } else {
            return 500;
        }
    }

    private int chooseItems() {
        int numItemsToSelect = openTradingSlots;
        if (openTradingSlots > alchables.size()) {
            numItemsToSelect = alchables.size();
        }
        Logger.log("Choosing " + numItemsToSelect + " items to alch.");
        for (int id : alchables.subList(0, numItemsToSelect)) {
            Item item = new Item(id, 1);
            TradingAlchs ta = new TradingAlchs(id, 0, 0, item.getLivePrice(), item.getName());
            int amtToBuy = (goldAmount / openTradingSlots) / ta.getLivePrice();
            ta.setAmtToBuy(amtToBuy);
            Logger.log("Adding: " + ta.toString());
            chosenAlchables.add(ta);
        }
        chooseItems = false;
        calculateNatureRunes();
        return 1000;
    }

    // figure out how many nature runes we'll need and subtract their cost from the items
    private void calculateNatureRunes() {
        int totalCost = 0;
        int totalCasts = 0;

        for (TradingAlchs ta : chosenAlchables) {
            totalCost += ta.getLivePrice() * ta.getAmtToBuy();
            totalCasts += ta.getAmtToBuy();
        }
        int leftover = goldAmount - totalCost;
        if (totalCasts < currentNatureRunes) {
            Logger.log("Already have enough Nature runes.");
            return;
        } else {
            buyNatureRunes = true;
            // calculate how many nature runes we need, subtracting the amount from the rest of the items
            natureRunesNeeded = totalCasts - currentNatureRunes;
            int runesCost = natureRunesNeeded * natureRune.getLivePrice();
            for (TradingAlchs ta : chosenAlchables) {
                int amtToBuy = ta.getAmtToBuy();
                amtToBuy -= (runesCost / chosenAlchables.size()) / ta.getLivePrice();
                ta.setAmtToBuy(amtToBuy);
            }
        }
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
                    Bank.withdrawAll("Nature rune");
                } else {
                    Logger.log("Failed to find Nature rune from bank.");
                }
                Sleep.sleep(Calculations.random(1000, 1500));
                Item nrItem = Inventory.get("Nature rune");
                if (nrItem == null) {
                    Logger.log("Failed to get nature runes from inventory.");
                } else {
                    currentNatureRunes += nrItem.getAmount();
                }

                Logger.log("Exiting setup with " + goldAmount + " gp and " + currentNatureRunes + " nature runes.");
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