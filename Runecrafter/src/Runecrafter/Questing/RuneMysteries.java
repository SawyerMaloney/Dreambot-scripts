package Runecrafter.Questing;

import Runecrafter.BotUtils;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;

public class RuneMysteries {
    private final Quest quest = FreeQuest.RUNE_MYSTERIES;

    private static final int RUNEMYSTERIES_TALISMAN = 13720;
    private static final int RUNEMYSTERIES_TALISMAN_GIVE = 13721;
    private static final int RUNEMYSTERIES_BACKSTORY = 13722;
    private static final int RUNEMYSTERIES_PACKAGE = 13723;
    private static final int RUNEMYSTERIES_KNOWNAME = 13724;
    private static final int RUNEMYSTERIES_NOTES = 13725;
    private static final int RUNEMYSTERIES_NOTES_GIVEN = 13726;
    private static final int RUNEMYSTERIES_OWED_TALISMAN = 13727;
    private static final int RUNEMYSTERIES_KNOW_OTHERS = 13728;
    private static final int DONE_RUNECRAFTING = 13729;

    private final Tile dukeHoracioTile = new Tile(3209, 3222, 1);
    private final Tile sedridorTile = new Tile(3105, 9572, 0);
    private final Tile auburyTile = new Tile(3253, 3400);

    int onLoop() {
        Logger.log("In Rune Mysteries quest loop.");
        if (PlayerSettings.getBitValue(RUNEMYSTERIES_TALISMAN) == 0) {
            talkToDuke();
        } else if (PlayerSettings.getBitValue(RUNEMYSTERIES_TALISMAN_GIVE) == 0) {
            giveToSedridor();
        } else if (PlayerSettings.getBitValue(RUNEMYSTERIES_NOTES) == 0) {
            deliverPackage();
        } else if (PlayerSettings.getBitValue(RUNEMYSTERIES_NOTES_GIVEN) == 0) {
            deliverNotes();
        }
        return 1000;
    }

    private void deliverNotes() {
        if (sedridorTile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(sedridorTile);
            }
        }

        NPC sedridor = NPCs.closest(5034);
        if (sedridor == null) {
            return;
        }
        sedridor.interact();

        BotUtils.executeConversation(
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{193, 0, 2},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{153, 16}
        );
    }

    private void deliverPackage() {
        if (auburyTile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(auburyTile);
            }
            return;
        }

        NPC aubury = NPCs.closest(2886);
        if (aubury == null) {
            return;
        }

        aubury.interact("Talk-to");

        BotUtils.executeConversation(
                new int[]{231, 5},
                new int[]{219, 1, 2},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{193, 0, 2},
                new int[]{231, 5},
                new int[]{193, 0, 2},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{193, 0, 2}
        );
    }

    private void giveToSedridor() {
        if (sedridorTile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(sedridorTile);
            }
            return;
        }
        NPC sedridor = NPCs.closest(5034);
        if (sedridor == null) {
            return;
        }
        sedridor.interact();

        BotUtils.executeConversation(
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{219, 1, 1},
                new int[]{217, 5},
                new int[]{193, 0, 2},
                new int[]{231, 5},
                new int[]{193, 0, 2},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{219, 1, 1},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{231, 5},
                new int[]{219, 1, 1},
                new int[]{217, 5},
                new int[]{231, 5},
                new int[]{193, 0, 2},
                new int[]{231, 5},
                new int[]{217, 5},
                new int[]{231, 5}
                );
    }

    private void talkToDuke() {
        if (dukeHoracioTile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(dukeHoracioTile);
            }
            return;
        }

        // find Duke and talk to him
        NPC dukeHoracio = NPCs.closest(815);
        if (dukeHoracio != null) {
            Sleep.sleepUntil(() -> {
                return dukeHoracio.interact("Talk-to");
            }, 5000);
        } else {
            return;
        }

        if (!BotUtils.executeConversation(
                new int[]{231, 5},  // click here
                new int[]{219, 1, 1},  // have quests?
                new int[]{217, 5},  // click here
                new int[]{231, 5},  // click here
                new int[]{217, 5},  // click here
                new int[]{231, 5},  // click here
                new int[]{193, 0, 2},  // click here
                new int[]{231, 5},  // click here
                new int[]{231, 5},  // click here
                new int[]{219, 1, 1},  // 'yes'
                new int[]{217, 5}, // click here
                new int[]{231, 5},  // click here
                new int[]{193, 0, 2} // click here
        )) {
            return;
        }
    }
}