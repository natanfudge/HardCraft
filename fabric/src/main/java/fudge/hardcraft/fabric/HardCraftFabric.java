package fudge.hardcraft.fabric;

import fudge.hardcraft.HardCraft;
import net.fabricmc.api.ModInitializer;

public class HardCraftFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        HardCraft.initialize();
    }


}
