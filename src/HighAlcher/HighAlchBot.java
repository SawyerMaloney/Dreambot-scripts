package HighAlcher;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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
    final boolean onlyAlch = false;
    final int staffOfFireID = 1387;
    final int natureRuneID = 561;

    private Timer natureRuneTimer;

    // script flags
    private boolean setup = true;  // get to varrock GE and deposit all items
    private boolean arrived = false;
    private boolean chooseItems = true;
    private boolean buyItems = true;
    private boolean buyNatureRunes = false;
    private boolean buyOrderPlacedNatureRunes = false;

    List<TradingAlchs> items = new ArrayList<>();
    List<TradingAlchs> alchables = new ArrayList<>();
    List<TradingAlchs> chosenAlchables = new ArrayList<>();
    List<Transaction> transactions = new ArrayList<>();
    Map<String, Integer> previousTradesByName = new HashMap<>();
    private final Tile varrockBank = new Tile(3164, 3487);
    int goldAmount = 0;
    Item natureRune = new Item(natureRuneID, 1);
    int natureRunesNeeded = 0;
    int currentNatureRunes = 0;



    @Override
    public void onStart() {

        try {
            getItemIds();
            findAlchables();
            transactions = TransactionsHelper.loadTransactions();
            getPreviousTrades();
        } catch (Exception e) {
            Logger.log("Failed to get item ids from server.");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onExit() {
        Logger.log("Exiting HighAlcher.");
        TransactionsHelper.saveTransactions(transactions);
    }

    @Override
    public int onLoop() {
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
        Logger.log("Entering claimItems. Sleeping until GE is ready to collect.");
        Sleep.sleepUntil(GrandExchange::isReadyToCollect, 15000);
        if (tryCollect()) {
            Logger.log("GrandExchange items ready.");
            updateAlchables();
        } else {
            Logger.log("GE not ready to collect yet.");
        }
        return 500;
    }

    private void updateAlchables() {
        for (Item item : getInventoryAlchables()) {
            for (TradingAlchs ta : chosenAlchables) {
                if (ta.getItemName().equals(item.getName())) {
                    ta.setAmtBought(ta.getAmtBought() + item.getAmount());
                    // TODO not perfect way to get live price here
                    Transaction trans = new Transaction(ta.getItemName(), item.getAmount(), item.getLivePrice());
                    transactions.add(trans);
                }
            }
        }
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
        Logger.log("Beginning to alch.");
        if (GrandExchange.isOpen()) {
            GrandExchange.close();
        }
        Sleep.sleep(Calculations.random(1000, 1500));
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

    // collect from the GE, then see if we got any alchables
    private boolean tryCollect() {
        if (GrandExchange.isOpen()) {
            GrandExchange.collect();
            return !getInventoryAlchables().isEmpty();
        } else {
            Logger.log("GE not open. Sleeping until open.");
            GrandExchange.open();
            Sleep.sleepUntil(GrandExchange::isOpen, 1500);
        }
        Logger.log("GE is not open for tryCollect.");
        return false;
    }

    private int buyNatureRunes() {
        Logger.log("Buying nature runes.");
        int natureRunePrice = natureRune.getLivePrice();
        if (GrandExchange.open()) {
            if (!buyOrderPlacedNatureRunes) {
                GrandExchange.buyItem(natureRuneID, natureRunesNeeded, natureRunePrice);
                buyOrderPlacedNatureRunes = true;
                natureRuneTimer = new Timer(60_000);
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
                Transaction trans = new Transaction("Nature rune", natureRunesInventory.getAmount(), natureRunePrice);
                transactions.add(trans);
            } else if (natureRuneTimer.finished()) {
                Logger.log("Timed out on buying nature runes.");
                GrandExchange.cancelAll();
                buyNatureRunes = false;
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
        int numItemsToSelect = Math.min(openTradingSlots, alchables.size());
        Logger.log("Choosing " + numItemsToSelect + " items to alch.");
        int i = 0;
        while (i < alchables.size() || (chosenAlchables.size() < numItemsToSelect)) {
            TradingAlchs ta = alchables.get(i);
            // skips item if it isn't at the buy limit
            if (previousTradesByName.containsKey(ta.getItemName()) && previousTradesByName.get(ta.getItemName()) < ta.getLimit()) {
                int amtToBuy = (goldAmount / openTradingSlots) / ta.getLivePrice();
                ta.setAmtToBuy(Math.min(amtToBuy, ta.getLimit()));  // ensure buying limit is not exceeded
                Logger.log("Adding: " + ta.toString());
                chosenAlchables.add(ta);
            }
            i++;
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
                Sleep.sleep(500);
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

        for (TradingAlchs ta : items) {
            Item item = new Item(ta.getId(), 1);
            int highAlchValue = item.getHighAlchValue();
            int lowAlchValue = item.getLowAlchValue();
            int alchValue = highAlchValue;
            if (lowAlch) alchValue = lowAlchValue;
            if (alchValue > LivePrices.get(ta.getId()) + nature_rune_price)  {
                if (!item.isMembersOnly() || members) {
                    alchables.add(ta);
                }
            }
        }
    }

    private void getPreviousTrades() {
        Logger.log("Collecting previous trades.");
        for (Transaction trans : transactions) {
            if (trans.isWithinLastFourHours()) {
                String itemName = trans.getItemName();
                if (previousTradesByName.containsKey(itemName)) {
                    previousTradesByName.put(itemName, previousTradesByName.get(itemName) + trans.getQuantity());
                } else {
                    previousTradesByName.put(itemName, trans.getQuantity());
                }
            }
        }

        Logger.log(previousTradesByName);
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
            if (!obj.has("limit")) {
                continue;
            }
            int id = obj.getInt("id");
            String name = obj.getString("name");
            int limit = obj.getInt("limit");
            TradingAlchs ta = new TradingAlchs(id, name,0, 0, 0, limit);
            this.items.add(ta);
        }
    }
}

class TradingAlchs {
    private int id;
    private int amtToBuy;
    private int amtBought;
    private int livePrice;
    private String itemName;
    private int limit;

    public TradingAlchs(int id, String itemName, int amtToBuy, int amtBought, int livePrice, int limit) {
        this.id = id;
        this.amtToBuy = amtToBuy;
        this.amtBought = amtBought;
        this.livePrice = livePrice;
        this.itemName = itemName;
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getLivePrice() {
        Item item = new Item(id, 1);
        livePrice = item.getLivePrice();
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
        return itemName + " (" + id + "): " + livePrice + " gp. Buying: " + amtToBuy + ". Bought: " + amtBought + " (Limit: " +  limit + ").";
    }
}

class TransactionsHelper {
    private static final String FILE_PATH = "transactions.dat";

    public static void saveTransactions(List<Transaction> transactions) {
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(Paths.get(FILE_PATH)))) {
            out.writeObject(transactions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Transaction> loadTransactions() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(Paths.get(FILE_PATH)))) {
            return (List<Transaction>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}

class Transaction {
    private final String itemName;
    private final int quantity;
    private final int price;
    private final LocalDateTime time;

    public Transaction(String itemName, int quantity, int price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.time = LocalDateTime.now(); // record time automatically
    }

    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
    public int getPrice() { return price; }
    public LocalDateTime getTime() { return time; }

    /**
     * Returns true if this transaction occurred within the past four hours.
     */
    public boolean isWithinLastFourHours() {
        LocalDateTime fourHoursAgo = LocalDateTime.now().minusHours(4);
        return time.isAfter(fourHoursAgo);
    }

    @Override
    public String toString() {
        return itemName + " x" + quantity + " for " + price + "gp at " + time;
    }
}