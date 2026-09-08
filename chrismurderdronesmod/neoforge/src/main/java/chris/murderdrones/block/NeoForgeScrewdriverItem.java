package chris.murderdrones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

/** NeoForge-only: adds back doesSneakBypassUse (a NeoForge IItemExtension method, not vanilla). */
public class NeoForgeScrewdriverItem extends ScrewdriverItem {
    public NeoForgeScrewdriverItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}
