package RunecrafterAbstractScript;

import org.dreambot.api.methods.walking.impl.Walking;

public class WalkToBankAction {
    public static int execute(Skilling ctx) {
        if (ctx.getBankTile().distance() > 5) {
            if (Walking.shouldWalk()) {
                Walking.walk(ctx.getBankTile());
            }
        } else {
            ctx.setStateBanking();
        }
        return 500;
    }
}
