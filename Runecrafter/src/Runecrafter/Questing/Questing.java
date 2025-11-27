package Runecrafter.Questing;

import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.MiniQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;

import java.util.Arrays;
import java.util.List;

public class Questing {
    private enum State {
        SELECT_QUEST,
        EXECUTE_QUEST
    }
    private State state = State.SELECT_QUEST;

    private final List<String> implementedQuests = Arrays.asList("Rune Mysteries", "Temple of the Eye");
    private Quest currentQuest = FreeQuest.DORICS_QUEST;
    private final RuneMysteries rm = new RuneMysteries();
    private final EnterTheAbyss eta = new EnterTheAbyss();

    public int onLoop() {
        switch (state) {
            case SELECT_QUEST:
                selectQuest();
                state = State.EXECUTE_QUEST;
                break;
            case EXECUTE_QUEST:
                executeQuest();
                break;
        }
        return 500;
    }

    private void executeQuest() {
        if (currentQuest.equals(FreeQuest.RUNE_MYSTERIES)) {
            rm.onLoop();
        } else if (currentQuest.equals(MiniQuest.ENTER_THE_ABYSS)) {
            eta.onLoop();
        }
        else {
            Logger.log("Unimplemented Quest: " + currentQuest + ".");
        }
    }

    private void selectQuest() {
        Logger.log("Selecting quest.");
        int rc = Skills.getRealLevel(Skill.RUNECRAFTING);
        boolean runeMysteriesComplete = Quests.isFinished(FreeQuest.RUNE_MYSTERIES);
        boolean enterTheAbyssComplete = Quests.isFinished(MiniQuest.ENTER_THE_ABYSS);

        if (!runeMysteriesComplete) {
            currentQuest = FreeQuest.RUNE_MYSTERIES;
        } else if (!enterTheAbyssComplete) {
            currentQuest = MiniQuest.ENTER_THE_ABYSS;
        } else if (rc >= 10) {
            currentQuest = PaidQuest.TEMPLE_OF_THE_EYE;
            // TODO change this once implemented
            currentQuest = FreeQuest.RUNE_MYSTERIES;
        }
    }

    public boolean canQuest() {
        boolean runeMysteriesComplete = Quests.isFinished(FreeQuest.RUNE_MYSTERIES);
        boolean templeOfTheEyeComplete = Quests.isFinished(PaidQuest.TEMPLE_OF_THE_EYE);
        boolean enterTheAbyssComplete = Quests.isFinished(MiniQuest.ENTER_THE_ABYSS);
        int rc = Skills.getRealLevel(Skill.RUNECRAFTING);

        return !runeMysteriesComplete ||
                (rc >= 10 && !templeOfTheEyeComplete) ||
                !enterTheAbyssComplete;
    }
}
