package onetapbuilder.Firemaker;

import onetapbuilder.*;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.wrappers.interactive.GameObject;

import java.util.*;

public class Firemaker extends TaskNode implements Resetable, ResourceNode {
    @Override
    public List<String> getProducedItems() {
        return Collections.emptyList();
    }

    enum State {
        WALKING_TO_BANK,
        RETRIEVE_WOOD,
        FIND_OPEN_SPOT,
        FIND_FIRE,
        BURN_WOOD
    }

    static String logName = "";
    static State state = State.WALKING_TO_BANK;

    static GameObject fire;

    private final List<String> logNames = Arrays.asList("Yew logs", "Maple logs", "Willow logs", "Oak logs", "Logs");
    private final List<String> burnableLogs = new ArrayList<>();

    private boolean init = false;

    @Override
    public boolean accept() {
        return TaskScheduler.valid("Firemaker");
    }

    @Override
    public void reset() {
        state = State.WALKING_TO_BANK;
    }

    private void init() {
        TaskScheduler.init("Firemaker");
        init = true;
    }

    @Override
    public int execute() {
        if (!init) {
            init();
        }
        switch (state) {
            case WALKING_TO_BANK:
                return WalkToBank.walkToBank();
            case RETRIEVE_WOOD:
                return RetrieveWood.retrieveWood();
            case FIND_OPEN_SPOT:
                return FindOpenSpot.findOpenSpot();
            case FIND_FIRE:
                return FindFire.findFire();
            case BURN_WOOD:
                return BurnWood.burnWood();
        }
        return 500;
    }
}
