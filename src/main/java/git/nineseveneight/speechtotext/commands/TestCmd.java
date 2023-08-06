package git.nineseveneight.speechtotext.commands;

import com.google.gson.Gson;
import git.nineseveneight.speechtotext.Shared;
import git.nineseveneight.speechtotext.json.VoskPartialJSON;
import git.nineseveneight.speechtotext.json.VoskResultJSON;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static git.nineseveneight.speechtotext.SpeechToText.model;

public class TestCmd extends CommandBase {

    private final Path testFilePath = Paths.get(Shared.minecraftPath.toString(), "test.wav");

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getCommandName() {
        return "test";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/test";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (sender instanceof EntityPlayerSP) {
            EntityPlayerSP player = (EntityPlayerSP) sender.getCommandSenderEntity();

            new Thread(() -> {
                LibVosk.setLogLevel(LogLevel.DEBUG);

                try (InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(testFilePath)));
                     Recognizer recognizer = new Recognizer(model, 48000)) {

                    int nbytes;
                    byte[] b = new byte[1024];
                    String lastPartialJSONString = "";
                    while ((nbytes = ais.read(b)) >= 0) {
                        if (recognizer.acceptWaveForm(b, nbytes)) {
                            System.out.println(recognizer.getResult());
                        } else {
                            String partialJsonString = recognizer.getPartialResult();
                            System.out.println(partialJsonString);
                            if (!lastPartialJSONString.equals(partialJsonString)) {
                                lastPartialJSONString = partialJsonString;
                                VoskPartialJSON voskPartialJSON = new Gson().fromJson(lastPartialJSONString, VoskPartialJSON.class);
                                if (voskPartialJSON.partial.length() > 0) {
                                    sender.addChatMessage(new ChatComponentText(voskPartialJSON.partial));
                                }
                            }
                        }
                    }

                    String finalResultJSONString = recognizer.getFinalResult();
                    System.out.println(finalResultJSONString);
                    VoskResultJSON voskResultJSON = new Gson().fromJson(finalResultJSONString, VoskResultJSON.class);
                    if (voskResultJSON.text.length() > 0) {
                        player.sendChatMessage(voskResultJSON.text);
                    }
                } catch (IOException | UnsupportedAudioFileException e) {
                    sender.addChatMessage(new ChatComponentText("Failed to read test file!"));
                }
            }).start();
        }
    }
}
