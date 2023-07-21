package fudge.hardcraft;

import net.minecraft.server.world.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HardCraft {
    public static final SavedD

    public static final String NAME = "HardCraft";
    public static final String MOD_ID = "hardcraft";

    public static Logger getLogger() {
        ServerWorld x;
        x.getPersistentStateManager().
        // Create a new logger every time because Forge blocks loggers created in one context from working in another context.
        return LogManager.getLogger(NAME);
    }

    public static void initialize() {
    }
}
