import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlchScraper {

    private static final String PRICES_URL = "https://prices.runescape.wiki/api/v1/osrs/latest";
    private static final String MAPPING_URL = "https://prices.runescape.wiki/api/v1/osrs/mapping";
    private static final int NATURE_RUNE_ID = 561;
    private static final int NATURE_RUNE_COST_MULTIPLIER = 1; // cost per cast

    public static void main(String[] args) {
        try {
            // Fetch both datasets
            JsonNode latestPrices = fetchJson(PRICES_URL);
            JsonNode mapping = fetchJson(MAPPING_URL);

            // Get nature rune price
            double natureRunePrice = latestPrices.path("data").path(String.valueOf(NATURE_RUNE_ID)).path("high").asDouble();
            System.out.println("Nature rune price: " + natureRunePrice + " gp");

            // Prepare item lists
            List<ItemProfit> f2pProfits = new ArrayList<>();
            List<ItemProfit> p2pProfitsHigh = new ArrayList<>();
            List<ItemProfit> p2pProfitsLow = new ArrayList<>();

            // Build a lookup map for price data
            JsonNode priceData = latestPrices.path("data");
            ObjectMapper mapper = new ObjectMapper();

            for (JsonNode item : mapping) {
                int id = item.path("id").asInt();
                String name = item.path("name").asText();
                boolean members = item.path("members").asBoolean(false);
                int highAlch = item.path("highalch").asInt(0);
                int lowAlch = item.path("lowalch").asInt(0);

                JsonNode priceNode = priceData.path(String.valueOf(id));
                if (priceNode.isMissingNode()) continue;

                double highPrice = priceNode.path("high").asDouble(0);
                double lowPrice = priceNode.path("low").asDouble(0);

                if (highPrice <= 0) continue;

                double highProfit = highAlch - (highPrice + natureRunePrice * NATURE_RUNE_COST_MULTIPLIER);
                double lowProfit = lowAlch - (lowPrice + natureRunePrice * NATURE_RUNE_COST_MULTIPLIER);

                ItemProfit profit = new ItemProfit(name, members, highProfit, lowProfit);

                if (!members) {
                    if (highProfit > 0 && lowProfit > 0)
                        f2pProfits.add(profit);
                } else {
                    p2pProfitsHigh.add(profit);
                    p2pProfitsLow.add(profit);
                }
            }

            // Sort lists
            p2pProfitsHigh.sort((a, b) -> Double.compare(b.highProfit, a.highProfit));
            p2pProfitsLow.sort((a, b) -> Double.compare(b.lowProfit, a.lowProfit));

            // Print results
            System.out.println("\n--- F2P Profitable Items (High+Low positive) ---");
            f2pProfits.stream().limit(20).forEach(System.out::println);

            System.out.println("\n--- Top 20 P2P Items by High Alch Profit ---");
            p2pProfitsHigh.stream().limit(20).forEach(System.out::println);

            System.out.println("\n--- Top 20 P2P Items by Low Alch Profit ---");
            p2pProfitsLow.stream().limit(20).forEach(System.out::println);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JsonNode fetchJson(String urlStr) throws IOException {
        HttpURLConnection conn = null;
        InputStream inputStream = null;

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "OSRSProfitChecker/1.0 (example@example.com)");
            conn.setRequestProperty("Accept", "application/json");

            inputStream = conn.getInputStream();
            String encoding = conn.getContentEncoding();
            if (encoding != null && encoding.contains("gzip")) {
                inputStream = new GZIPInputStream(inputStream);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(response.toString());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static class ItemProfit {
        String name;
        boolean members;
        double highProfit;
        double lowProfit;

        public ItemProfit(String name, boolean members, double highProfit, double lowProfit) {
            this.name = name;
            this.members = members;
            this.highProfit = highProfit;
            this.lowProfit = lowProfit;
        }

        @Override
        public String toString() {
            return String.format("%-30s | Members: %-5s | HighProfit: %8.2f | LowProfit: %8.2f",
                    name, members, highProfit, lowProfit);
        }
    }
}
