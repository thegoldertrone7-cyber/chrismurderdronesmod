package chris.murderdrones.client.services;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;

/**
 * Looks up a model that was registered as a "standalone extra model" (not tied to
 * any blockstate variant or item model) — used here for the alt-skin models.
 * <p>
 * This has to be loader-specific because NeoForge and Fabric key these models
 * completely differently internally:
 * - NeoForge patches vanilla's ModelResourceLocation to add a real
 *   STANDALONE_VARIANT ("standalone") and looks models up by
 *   ModelResourceLocation.standalone(id).
 * - Fabric stores them under its own internal "fabric_resource" variant and
 *   exposes a separate FabricBakedModelManager#getModel(ResourceLocation) to
 *   fetch them directly by plain id.
 * Looking a model up with the wrong loader's key silently returns null, which
 * is what caused the missing-model checkerboard on Fabric.
 */
public interface IStandaloneModelLookup {
    BakedModel getStandaloneModel(ResourceLocation id);
}
