package good.damn.scriptengine.engines.script.interfaces;


import android.graphics.Bitmap;
import android.graphics.Movie;
import android.media.MediaPlayer;
import android.media.SoundPool;

import java.util.List;

import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.traceview.models.FileSVC;

public interface OnReadCommandListener {

    void onBackground(int color);

    void onImage(Bitmap bitmap, ScriptGraphicsFile graphicsFile);

    void onGif(Movie movie, ScriptGraphicsFile gifScript);

    void onSFX(byte soundID, SoundPool soundPool);

    void onAmbient(MediaPlayer mediaPlayer);

    void onError(String errorMsg);

    void onVector(FileSVC fileSVC, String[] advancedText);
}
