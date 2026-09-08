package chris.murderdrones.client;

import chris.murderdrones.CharacterEntry;
import chris.murderdrones.PlushieCatalog;
import chris.murderdrones.block.ModBlocksCommon;
import chris.murderdrones.block.PlushieBlock;
import chris.murderdrones.block.PlushieBlockEntity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Both KeyMapping objects themselves and the "was it just pressed, what do we do
 * about it" logic are pure vanilla and shared here. Only two things are loader
 * specific and live outside this class: actually registering these KeyMappings
 * (NeoForge: RegisterKeyMappingsEvent, Fabric: KeyBindingHelper) and hooking a
 * client tick to call onClientTick() below (NeoForge: ClientTickEvent.Post,
 * Fabric: ClientTickEvents.END_CLIENT_TICK).
 */
public final class ModKeyBindings {
    private ModKeyBindings() {}

    private static final int COLOR_WARN = 0xFF8888;

    public static final KeyMapping OPEN_BROWSER_KEY = new KeyMapping(
            "key.chrismurderdronesmod.open_browser",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_P,
            "key.categories.chrismurderdronesmod");

    public static final KeyMapping OPEN_SKIN_PICKER_KEY = new KeyMapping(
            "key.chrismurderdronesmod.open_skin_picker",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K,
            "key.categories.chrismurderdronesmod");

    /** Call once per client tick from each loader's own tick-event hook. */
    public static void onClientTick() {
        Minecraft mc = Minecraft.getInstance();

        while (OPEN_BROWSER_KEY.consumeClick()) {
            if (mc.screen == null) {
                mc.setScreen(new PlushieBrowserScreen(PlushieCatalog.getCategories()));
            }
        }

        while (OPEN_SKIN_PICKER_KEY.consumeClick()) {
            if (mc.screen == null && mc.player != null && mc.level != null) {
                tryOpenSkinPicker(mc);
            }
        }
    }

    private static void tryOpenSkinPicker(Minecraft mc) {
        boolean holdingScrewdriver = mc.player.getMainHandItem().is(ModBlocksCommon.SCREWDRIVER.get())
                || mc.player.getOffhandItem().is(ModBlocksCommon.SCREWDRIVER.get());
        if (!holdingScrewdriver) {
            warn(mc, "Hold the screwdriver to pick a skin");
            return;
        }

        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK || !(hit instanceof BlockHitResult blockHit)) {
            warn(mc, "Look at a plushie first");
            return;
        }

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = mc.level.getBlockState(pos);
        if (!(state.getBlock() instanceof PlushieBlock plushieBlock)) {
            warn(mc, "Look at a plushie first");
            return;
        }

        String character = plushieBlock.getCharacter();
        if (!PlushieBlockEntity.hasSkins(character)) {
            warn(mc, "This plushie has no alternate skins");
            return;
        }

        CharacterEntry entry = PlushieCatalog.findEntry(character);
        if (entry == null) return;

        mc.setScreen(new PlushieSkinPickerScreen(pos, entry));
    }

    private static void warn(Minecraft mc, String message) {
        mc.player.displayClientMessage(
                Component.literal(message).withStyle(Style.EMPTY.withColor(COLOR_WARN)), true);
    }
}
