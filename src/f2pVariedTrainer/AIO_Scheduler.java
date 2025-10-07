package f2pVariedTrainer;

import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(name = "AIO", description = "Main controller to run the other scripts.", author = "sawyerm",
        version = 1.0, category = Category.MISC)


public class AIO_Scheduler extends TaskScript {

    public static int inventories = 0;
    public static int inventory_limit = 10;

    public static int fisher_inv = 0;
    public static int miner_inv = 0;
    public static int tree_inv = 0;

    public static final int individual_inventory_limit = 3;

    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
        // addNodes(new f2pVariedTrainer.BonesCollector(), new f2pVariedTrainer.Fisher(), new f2pVariedTrainer.Miner(), new f2pVariedTrainer.TreeCutter());
        addNodes(new Fisher(), new Miner(), new TreeCutter());
    }

    public static void updateInventories(int task) {
        if (task == 0) {
            // fishing
            fisher_inv++;
            inventories++;
            Logger.log("Fishing. Fishing inv complete: " + fisher_inv + ". Total inventories: " + inventories);
        } else if (task == 1) {
            // miner
            miner_inv++;
            inventories++;
            Logger.log("Mining. Mining inv complete: " + miner_inv + ". Total inventories: " + inventories);
        } else if (task == 2) {
            // tree cutting
            tree_inv++;
            inventories++;
            Logger.log("Tree cutting. Tree cutting inv complete: " + tree_inv + ". Total inventories: " + inventories);
        }
    }
}
