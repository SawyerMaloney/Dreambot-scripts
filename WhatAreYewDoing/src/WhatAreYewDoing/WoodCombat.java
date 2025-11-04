package WhatAreYewDoing;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.world.World;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;

public class WoodCombat {
    public int onLoop() {
        // run to nearest bank and switch worlds
        Logger.log("Pathing to bank to switch worlds.");
        if (Bank.open()) {
            Bank.close();
            World world = Worlds.getRandomWorld(w -> w.isF2P() && !w.isPVP() && w.getMinimumLevel() == 0);
            Sleep.sleepUntil(() -> WorldHopper.hopWorld(world), 5000);
            WoodCutter.updateState(WoodCutter.getPreviousState());
        }
        return 500;
    }
}
