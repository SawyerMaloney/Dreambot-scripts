package onetapbuilder;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.script.TaskNode;
import org.dreambot.api.utilities.Logger;


public class Init extends TaskNode {
    @Override
    public boolean accept() {
        return TaskScheduler.valid("Init");
    }

    @Override
    public int execute() {
        if (Bank.open()) {
            Logger.log("Init complete.");
            TaskScheduler.init = true;
        }
        return 500;
    }
}
