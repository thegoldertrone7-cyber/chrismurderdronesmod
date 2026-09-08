package chris.murderdrones.platform;

import chris.murderdrones.Constants;
import chris.murderdrones.config.ICommonConfig;
import chris.murderdrones.platform.services.INetworkHelper;
import chris.murderdrones.platform.services.IPlatformHelper;
import chris.murderdrones.platform.services.IRegistryHelper;

import java.util.ServiceLoader;

/**
 * Loads the loader-specific implementation of each service interface at runtime via
 * java.util.ServiceLoader. Each loader module provides its implementation's fully
 * qualified class name in a META-INF/services file — see
 * neoforge/src/main/resources/META-INF/services/ and
 * fabric/src/main/resources/META-INF/services/.
 */
public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);
    public static final IRegistryHelper REGISTRY = load(IRegistryHelper.class);
    public static final ICommonConfig CONFIG = load(ICommonConfig.class);
    public static final INetworkHelper NETWORK = load(INetworkHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T loadedService = ServiceLoader.load(clazz)
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}
