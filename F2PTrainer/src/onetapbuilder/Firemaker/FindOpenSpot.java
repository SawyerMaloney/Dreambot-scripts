package onetapbuilder.Firemaker;

import onetapbuilder.BotUtils;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;

import java.util.List;

/**
 * Extracted logic from Firemaker.findOpenSpot() into its own class as requested.
 */
public class FindOpenSpot {
    public static int findOpenSpot() {
        GameObject campfire = GameObjects.closest("Forester's Campfire");
        if (campfire != null && campfire.distance() < 5) {
            Logger.log("FIND_FIRE");
            Firemaker.state = Firemaker.State.FIND_FIRE;
            return 0;
        } else {
            Logger.log("No nearby Forester's Campfire.");
        }

        List<GameObject> gos = GameObjects.all();
        List<Player> players = Players.all();
        Tile localTile = Players.getLocal().getTile();
        if (BotUtils.anyOnTile(gos, localTile) || BotUtils.anyOnTile(players, localTile)) {
            Logger.log("Tile occupied.");
            Tile newTile;
            if (Calculations.random(0, 2) == 0) {
                newTile = localTile.translate(-1, 0);
            } else {
                newTile = localTile.translate(0, 1);
            }

            if (Map.canReach(newTile)) {
                Logger.log("Moving to " + newTile + ".");
                Walking.walk(newTile);
            }
        } else {
            Logger.log("Unoccupied tile found.");
            Firemaker.state = Firemaker.State.FIND_FIRE;
        }
        return 500;
    }
}
