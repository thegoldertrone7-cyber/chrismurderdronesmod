package chris.murderdrones.block;

import net.minecraft.world.item.Item;

/**
 * Base screwdriver item shared by both loaders.
 * <p>
 * The original also overrode doesSneakBypassUse so that shift+right-click (used to
 * cycle skins backwards, see PlushieBlock#useWithoutItem) wasn't swallowed by the
 * default "sneaking + holding a non-empty item skips block interaction" behavior.
 * That override can't live here: doesSneakBypassUse comes from NeoForge's
 * IItemExtension, which isn't part of vanilla Item at all, so common code (compiled
 * against plain vanilla) can never reference it. NeoForge's platform/NeoForgeScrewdriverItem
 * subclass adds it back; vanilla/Fabric never had that sneak-skip behavior to begin
 * with, so the plain base class here is already correct on Fabric.
 * <p>
 * Renamed from the original "Screwdriveritem" to "ScrewdriverItem" during the port —
 * the old lowercase-i name had caused git rename headaches on case-insensitive
 * filesystems before.
 */
public class ScrewdriverItem extends Item {
    public ScrewdriverItem(Properties properties) {
        super(properties);
    }
}
