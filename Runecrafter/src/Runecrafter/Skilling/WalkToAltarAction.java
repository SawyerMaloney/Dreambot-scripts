package Runecrafter.Skilling;

import org.dreambot.api.methods.walking.impl.Walking;

public class WalkToAltarAction {
    public static int execute(Skilling ctx) {
        if (ctx.getAltarTile().distance() > 1) {
            if (Walking.shouldWalk()) {
                Walking.walk(ctx.getAltarTile());
            }
        } else {
            ctx.setStateUseRuins();
        }
        return 500;
    }
}
