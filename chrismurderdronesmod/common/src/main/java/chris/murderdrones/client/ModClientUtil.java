package chris.murderdrones.client;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

/**
 * ModelResourceLocation.standalone(ResourceLocation) doesn't actually exist in
 * 1.21.1's mappings — that was a mistake earlier in this port. The real API is
 * just the public two-arg constructor; this wraps it so every "standalone extra
 * model" call site (skin variants not referenced by a blockstate/item model)
 * only needed a one-line fix instead of four separate ones.
 */
public final class ModClientUtil {
    private ModClientUtil() {}

    public static ModelResourceLocation standaloneModel(ResourceLocation id) {
        return new ModelResourceLocation(id, "standalone");
    }
}
