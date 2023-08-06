package git.nineseveneight.speechtotext;

import net.minecraftforge.fml.common.Loader;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Shared {
    public static final Path minecraftPath = Paths.get(Loader.instance().getConfigDir().getAbsolutePath()).getParent();
}
