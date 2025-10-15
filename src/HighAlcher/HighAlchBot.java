package HighAlcher;

import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
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

    final boolean lowAlch = true;
    final boolean members = false;

    private Timer minuteTimer;

    @Override
    public void onStart() {
        minuteTimer = new Timer(60_000);
        try {
            getItemIds();
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



        return 10000;
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