package git.nineseveneight.speechtotext;

import com.google.gson.Gson;
import git.nineseveneight.speechtotext.json.VoskPartialJSON;
import git.nineseveneight.speechtotext.json.VoskResultJSON;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.IOException;

import static git.nineseveneight.speechtotext.SpeechToText.model;

public class Detector {

    private final AudioFormat format = new AudioFormat(16000f, 16, 1, true, false);
    private final DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

    public Detector() {
        LibVosk.setLogLevel(LogLevel.DEBUG);
    }

    public void start() {
        new Thread(() -> {
            // https://stackoverflow.com/a/75052331 | Microphone usage with vosk
            try (Recognizer recognizer = new Recognizer(model, 16000f);
                 TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info)) {
                microphone.open(format);
                microphone.start();

                boolean readyForAnalysis = false;

                int bytesRead;
                byte[] b = new byte[1024];
                String lastPartialJSONString = "";
                while ((bytesRead = microphone.read(b, 0, 1024)) >= 0) {
                    if (Minecraft.getMinecraft().theWorld != null) {
                        readyForAnalysis = true;
                        if (recognizer.acceptWaveForm(b, bytesRead)) {
                            String finalResultJSONString = recognizer.getFinalResult();
                            System.out.println(finalResultJSONString);
                            VoskResultJSON voskResultJSON = new Gson().fromJson(finalResultJSONString, VoskResultJSON.class);
                            if (voskResultJSON.text.length() > 0) {
                                Minecraft.getMinecraft().thePlayer.sendChatMessage(voskResultJSON.text);
                            }

                            System.out.println(recognizer.getResult());
                            recognizer.reset();
                        } else {
                            String partialJsonString = recognizer.getPartialResult();
                            if (!lastPartialJSONString.equals(partialJsonString)) {
                                System.out.println(partialJsonString);
                                lastPartialJSONString = partialJsonString;
                                VoskPartialJSON voskPartialJSON = new Gson().fromJson(lastPartialJSONString, VoskPartialJSON.class);
                                if (voskPartialJSON.partial.length() > 0) {
                                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(voskPartialJSON.partial));
                                }
                            }
                        }
                    } else if (readyForAnalysis) {
                        System.out.println("World null");
                        break;
                    }
                }

                System.out.println("Closing detector!");
            } catch (LineUnavailableException | IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
