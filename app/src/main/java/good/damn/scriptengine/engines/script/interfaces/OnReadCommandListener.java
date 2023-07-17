package good.damn.scriptengine.engines.script.interfaces;


import java.util.List;

import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;

public interface OnReadCommandListener {

    void onBackground(int color);

    void onImage(byte[] img, ScriptGraphicsFile graphicsFile);

    void onGif(byte[] gif, ScriptGraphicsFile gifScript);

    void onSFX(byte[] sfx);

    void onAmbient(byte[] ambientMusic);

    void onError(String errorMsg);

    void onVector(byte[] vect, String[] advancedText);
}
