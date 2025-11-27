package Runecrafter.Questing;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.walking.impl.Walking;

public class EnterTheAbyss {
    private final Tile monkOfZamorockTile = new Tile(3107, 3555);

    public static final int ABYSSAL_MINIQUEST_SEEN_SHOP = 13730;
    public static final int ABYSSAL_MINIQUEST_INTRO = 13731;
    public static final int ABYSSAL_MINIQUEST_CHOICE = 13732;
    public static final int ABYSSAL_MINIQUEST_ORB = 13733;
    public static final int ABYSSAL_MINIQUEST_REWARD = 13734;


    int onLoop() {
        if (PlayerSettings.getBitValue(ABYSSAL_MINIQUEST_SEEN_SHOP) == 0) {
            walkToMonkOfZam();
        }
        return 1000;
    }

    private void walkToMonkOfZam() {
        if (monkOfZamorockTile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(monkOfZamorockTile);
            }
            return;
        }
    }
}
