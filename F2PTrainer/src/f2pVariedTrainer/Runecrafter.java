package f2pVariedTrainer;

import org.dreambot.api.script.TaskNode;

public class Runecrafter extends TaskNode {
    @Override
    public boolean accept() {
        return AIO_Scheduler.valid("Runecrafter");
    }

    @Override
    public int execute() {
        return 0;
    }
}
