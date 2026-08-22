package chris.murderdrones;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * A plain, non-functional item used for the creative-tab-only "filler" slots:
 * both the section header icons (HEADER_MURDER_DRONES etc.) and the blank
 * padding that surrounds them (ROW_SPACER) — see
 * {@link ChrisMurderDronesMod#addHeader} and {@link CreativeTabHeaderRenderer}.
 * <p>
 * Blanking {@link #getName} / {@link #getDescription} here (rather than just
 * leaving out a lang entry) means there's no display-name text for a
 * tooltip to show, for the vanilla creative-tab search bar to match against,
 * or for JEI/EMI/REI to list — combined with the
 * {@code #c:hidden_from_recipe_viewers} tag (see
 * data/c/tags/item/hidden_from_recipe_viewers.json) which additionally
 * removes these items from JEI/EMI/REI's own ingredient list entirely, and
 * {@link CreativeTabHeaderRenderer#onMouseButtonPressedPre} which stops them
 * from being clicked/dragged out of the creative tab in the first place.
 */
public class RowSpacerItem extends Item {

    public RowSpacerItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getDescription() {
        return Component.empty();
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.empty();
    }
}