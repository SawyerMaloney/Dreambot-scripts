package f2pVariedTrainer;

import org.dreambot.api.Client;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.impl.TaskScript;
import org.dreambot.api.utilities.Logger;

@ScriptManifest(name = "AIO", description = "Main controller to run the other scripts.", author = "sawyerm",
        version = 1.0, category = Category.MISC)


public class AIO_Scheduler extends TaskScript {

    public static int inventories = 0;
    public static int inventory_limit = 1;

    public static int fisher_inv = 0;
    public static int miner_inv = 0;
    public static int tree_inv = 0;

    public static final int individual_inventory_limit = 1;

    public static boolean canCast = true;

    public final static String axe_name = null;
    public final static String tree_name = null;

    @Override
    public void onStart() {
        Logger.log("Scheduler starting.");
        addNodes(new TreeCutter());
        setFailLimit(3);
    }

    @Override
    public void onExit() {
        Logger.log("Script ended");

        // if script reached its endpoint (not stopped by user)
        if (inventories == inventory_limit) {
            Client.logout();
        }
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

        // reset all inventory counts if we've 'gone through' all of them.
        if (fisher_inv == inventory_limit && miner_inv == inventory_limit && tree_inv == inventory_limit) {
            fisher_inv = 0;
            miner_inv = 0;
            tree_inv = 0;
        }
    }
}
