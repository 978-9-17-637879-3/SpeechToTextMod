package git.nineseveneight.speechtotext;

import git.nineseveneight.speechtotext.commands.TestCmd;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.vosk.Model;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mod(modid = SpeechToText.MODID, version = SpeechToText.VERSION)
public class SpeechToText {
    public static final String MODID = "speechtotext";
    public static final String VERSION = "0.0.1";

    private final Path modelPath = Paths.get(Shared.minecraftPath.toString(), "vosk-model-small-en-us-0.15");
    public static Model model;

    private Detector detector;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        try {
            model = new Model(modelPath.toString());
            detector = new Detector();
        } catch (IOException e) {
            System.out.println("Failed to initialize vosk model!");
        }
        MinecraftForge.EVENT_BUS.register(this);

        ClientCommandHandler.instance.registerCommand(new TestCmd());
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent clientConnectedToServerEvent) {
        if (detector != null) {
            System.out.println("Starting detector!");
            detector.start();
        }
    }

}