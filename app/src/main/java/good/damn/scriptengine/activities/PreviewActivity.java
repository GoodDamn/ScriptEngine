package good.damn.scriptengine.activities;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.engines.script.ScriptReader;
import good.damn.scriptengine.engines.script.interfaces.OnCreateScriptTextViewListener;
import good.damn.scriptengine.engines.script.interfaces.OnReadCommandListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptTextConfig;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.GifView;
import good.damn.scriptengine.views.TextViewPhrase;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";
    private static final Random sRandom = new Random();

    private TextViewPhrase mCurrentViewPhrase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: INITIALIZING CONTENT FILE .SKC");

        Context context = this;

        String path = getIntent().getStringExtra("dumbPath");

        if (path == null) {
            Utilities.showMessage("INVALID DUMB PATH",
                    this);
            return;
        }

        Log.d(TAG, "onCreate: PATH TO CONTENT: " + path);

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        ScriptEngine scriptEngine = new ScriptEngine();
        ScriptReader scriptReader = new ScriptReader(scriptEngine, new File(path));

        FrameLayout root_FrameLayout = new FrameLayout(this);

        scriptEngine.setReadCommandListener(new OnReadCommandListener() {

            @Override
            public void onBackground(int color) {

            }

            @Override
            public void onImage(byte[] img, ScriptGraphicsFile scriptImage) {
                ImageView imageView = new ImageView(context);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(img, 0, img.length));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params.width = (int) (metrics.density * scriptImage.width);
                params.height = (int) (metrics.density * scriptImage.height);
                params.gravity = Gravity.START | Gravity.TOP;
                imageView.setScaleX(.0f);
                imageView.setScaleY(.0f);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                root_FrameLayout.addView(imageView,params);

                imageView.setX(scriptImage.x * metrics.widthPixels);
                imageView.setY(scriptImage.y * metrics.heightPixels);

                imageView.animate().scaleY(1.0f).scaleX(1.0f).withEndAction(() ->
                                imageView.animate().scaleX(.0f).scaleY(.0f).setStartDelay(1250).withEndAction(() ->
                                        root_FrameLayout.removeView(imageView)).start())
                        .start();
            }

            @Override
            public void onGif(byte[] gif, ScriptGraphicsFile gifScript) {
                GifView gifView = new GifView(context);
                gifView.setSource(gif);

                FrameLayout.LayoutParams par =
                        new FrameLayout.LayoutParams(gifView.width(), gifView.height());

                par.leftMargin = (int) (gifScript.x * metrics.widthPixels);
                par.topMargin = (int) (gifScript.y * metrics.heightPixels);

                root_FrameLayout.addView(gifView, par);
                gifView.play();

                gifView.animate()
                        .setStartDelay(5500)
                        .alpha(0.0f)
                        .withEndAction(()-> root_FrameLayout.removeView(gifView)).start();
            }

            @Override
            public void onSFX(byte[] sfx) {
                try {
                    File tempSFX = File.createTempFile(String.valueOf(System.currentTimeMillis()),".mp3",context.getCacheDir());

                    FileOutputStream fos = new FileOutputStream(tempSFX);
                    fos.write(sfx);
                    fos.close();

                    MediaPlayer mediaPlayer = MediaPlayer.create(context, Uri.fromFile(tempSFX));

                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            Log.d(TAG, "onCompletion: MEDIA_PLAYER_SFX: " + tempSFX.getName());
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            if (tempSFX.delete()) {
                                Log.d(TAG, "onCompletion: FILE HAS BEEN DELETED!");
                            }
                        }
                    });

                    mediaPlayer.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAmbient(byte[] ambientMusic) {

            }

            @Override
            public void onError(String errorMsg) {
                Utilities.showMessage(errorMsg, context);
            }
        });

        scriptEngine.setOnCreateViewListener(new OnCreateScriptTextViewListener() {
            @Override
            public void onCreate(ScriptTextConfig textConfig) {


                /*textViewPhrase.animate()
                        .alpha(1.0f)
                        .setDuration(1500)
                        .start();
                mCurrentViewPhrase = textViewPhrase;*/
            }
        });

        setContentView(root_FrameLayout);


        root_FrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentViewPhrase != null) {
                    mCurrentViewPhrase.fadeOutTransition(sRandom, 2.1f);
                }
                scriptReader.next();
            }
        });

        scriptReader.next();
    }
}
