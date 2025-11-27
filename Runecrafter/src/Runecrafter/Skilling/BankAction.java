package Runecrafter.Skilling;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import Runecrafter.Runecrafter;

public class BankAction {
    public static int execute(Skilling ctx) {
        ctx.chooseRune();
        if (Bank.open()) {
            Bank.depositAllItems();
            String tiaraName = ctx.getTiaraName();
            String talismanName = ctx.getTalismanName();
            if (!Inventory.contains(tiaraName) && !Inventory.contains(talismanName) && !Equipment.contains(tiaraName)) {
                if (Bank.contains(tiaraName)) {
                    Logger.log("Retrieving " + tiaraName + ".");
                    Runecrafter.retrieveItem(tiaraName, false);
                    Item tiara = Inventory.get(tiaraName);
                    if (tiara != null) {
                        tiara.interact("Wear");
                    }
                } else if (Bank.contains(talismanName)) {
                    Logger.log("Retrieving " + talismanName + ".");
                    Runecrafter.retrieveItem(talismanName, false);
                } else {
                    Logger.log("Doesn't have talisman or tiara");
                    return -1;
                }
            }
            if (Bank.contains("Pure essence")) {
                Runecrafter.retrieveItem("Pure essence", true);
                Logger.log("WALK_TO_ALTAR");
                ctx.setStateWalkToAltar();
            } else {
                Logger.log("No pure essence.");
                ctx.setStateBuyRunes();
                return 500;
            }
        }
        return 500;
    }
}
