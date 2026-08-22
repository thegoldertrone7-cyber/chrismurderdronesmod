package chris.murderdrones;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;

/**
 * By default, Vanilla skips a block's right-click interaction entirely whenever the
 * player is sneaking and holding ANY non-empty item (this is what lets you shift-click
 * to place a block against a chest instead of opening it). Since the screwdriver relies
 * on shift+right-click to cycle skins backwards (see PlushieBlock#useWithoutItem), it
 * needs to opt out of that default via doesSneakBypassUse — otherwise sneak-right-click
 * never reaches the block at all, forward OR backward.
 */
public class Screwdriveritem extends Item {

    public Screwdriveritem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}