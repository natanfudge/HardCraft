package fudge.hardcraft.platform;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.file.Path;

public interface NecPlatform {
    static NecPlatform instance() {
        return NecPlatformStorage.INSTANCE_SET_ONLY_BY_SPECIFIC_PLATFORMS_VERY_EARLY;
    }

    boolean isForge();

    boolean isModLoaded(String modId);

    Path getGameDirectory();

    Path getConfigDirectory();

    boolean isDevelopmentEnvironment();
}
