package onetapbuilder.ItemSeller;

import onetapbuilder.Resetable;
import onetapbuilder.TaskScheduler;
import org.dreambot.api.script.TaskNode;

public class ItemSeller extends TaskNode implements Resetable {

    @Override
    public void reset() {

    }

    @Override
    public boolean accept() {
        return TaskScheduler.valid("ItemSeller");
    }

    @Override
    public int execute() {
        return 0;
    }
}
