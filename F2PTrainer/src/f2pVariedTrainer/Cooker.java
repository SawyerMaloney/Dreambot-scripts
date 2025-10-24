package f2pVariedTrainer;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;

import java.util.ArrayList;
import java.util.List;

public class Cooker extends TaskNode {

    private final Tile varrock_bank_tile = new Tile(3164, 3487);
    private boolean initialized = false;
    private List<String> fishNames = new ArrayList<>();
    @Override
    public boolean accept() {
        return AIO_Scheduler.valid("Cooker");
    }

    @Override
    public int execute() {
        if (!initialized) {
            findBestFish();
        }
        if (varrock_bank_tile.distance() > 1) {
            if (Walking.shouldWalk()) {
                Logger.log("Walking to varrock");
                Walking.walk(varrock_bank_tile);
                return 1000;
            }
        } else {
            return -1;
        }
        return 0;
    }

    private void findBestFish() {
        int cookingSkill = Skills.getRealLevel(Skill.COOKING);

        if (cookingSkill < 15) {
            fishNames.add("Raw shrimp");
            fishNames.add("Raw anchovies");
        } else {
            fishNames.add("Raw trout");
            if (cookingSkill >= 25) {
                fishNames.add("Raw salmon");
            }
        }
    }
}
