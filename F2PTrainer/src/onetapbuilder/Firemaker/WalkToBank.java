package onetapbuilder.Firemaker;

import onetapbuilder.BotUtils;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;

/**
 * Extracted logic from Firemaker.walkToBank() into its own helper class.
 */
public class WalkToBank {

    public static int walkToBank() {
        setLogName();
        if (BotUtils.geTile.distance() > 10) {
            if (Walking.shouldWalk()) {
                Walking.walk(BotUtils.geTile);
            }
        } else {
            Logger.log("RETRIEVE_WOOD");
            Firemaker.state = Firemaker.State.RETRIEVE_WOOD;
        }
        return 500;
    }

    // Mirrors Firemaker.setLogName() but only sets the chosen logName used by other steps
    private static void setLogName() {
        int firemakingSkill = Skills.getRealLevel(Skill.FIREMAKING);
        String chosen = "Logs";
        if (firemakingSkill >= 60) {
            chosen = "Yew logs";
        } else if (firemakingSkill >= 45) {
            chosen = "Maple logs";
        } else if (firemakingSkill >= 30) {
            chosen = "Willow logs";
        } else if (firemakingSkill >= 15) {
            chosen = "Oak logs";
        }
        Firemaker.logName = chosen;
        Logger.log("Current firemaking skill: " + firemakingSkill + ". Burning: " + Firemaker.logName + ".");
    }
}
