package xyz.nikitacartes.restrictedcrafting.integrations;

//? if fabric {
import net.fabricmc.loader.api.FabricLoader;
//?} else {
/*import net.neoforged.fml.loading.FMLPaths;
*///?}

import java.nio.file.Path;

/** The handful of loader-specific lookups RestrictedCrafting needs, behind one stable API. */
public final class Platform {
    private Platform() {}

    public static Path gameDir() {
        //? if fabric {
        return FabricLoader.getInstance().getGameDir();
        //?} else
        /*return FMLPaths.GAMEDIR.get();*/
    }
}
