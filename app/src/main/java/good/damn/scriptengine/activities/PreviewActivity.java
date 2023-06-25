package good.damn.scriptengine.activities;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.engines.script.ScriptReader;
import good.damn.scriptengine.engines.script.interfaces.OnCreateScriptTextViewListener;
import good.damn.scriptengine.engines.script.interfaces.OnReadCommandListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptTextConfig;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.ColorRevealView;
import good.damn.scriptengine.views.GifView;
import good.damn.scriptengine.views.TextViewPhrase;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";
    private static final Random sRandom = new Random();

    private TextViewPhrase mCurrentViewPhrase;

    private MediaPlayer mAmbientPlayer;

    private SoundPool mSFXPool;

    private final short[] mToARGB = new short[4];
    private final short[] mFromARGB = new short[4];

    private int mCurrentBackColor = 0;

    private void toARGB(int input, short[] result) {
        result[0] = (short) ((input >> 24) & 0xff);
        result[1] = (short) ((input >> 16) & 0xff);
        result[2] = (short) ((input >> 8) & 0xff);
        result[3] = (short) (input & 0xff);
    }

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


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build();

            mSFXPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSFXPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        }

        mSFXPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int soundID, int status) {
                Log.d(TAG, "onLoadComplete: SOUND_ID: " + soundID + " STATUS: " + status);
                mSFXPool.play(soundID,
                        1.0f,
                        1.0f,
                        1,
                        0,
                        1.0f);
            }
        });

        DisplayMetrics metrics = getResources().getDisplayMetrics();

        ScriptEngine scriptEngine = new ScriptEngine();
        ScriptReader scriptReader = new ScriptReader(scriptEngine, new File(path));

        FrameLayout root_FrameLayout = new FrameLayout(this);

        ColorRevealView mColorRevealView = new ColorRevealView(this);

        /*ValueAnimator mAnimatorColor = new ValueAnimator();
        mAnimatorColor.setIntValues(0,1);
        mAnimatorColor.setDuration(1550);

        mAnimatorColor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
                float frac = valueAnimator.getAnimatedFraction();

                mCurrentBackColor =
                        (((int)(mFromARGB[0] + (mToARGB[0] - mFromARGB[0]) * frac) & 0xff) << 24) |
                        (((int)(mFromARGB[1] + (mToARGB[1] - mFromARGB[1]) * frac) & 0xff) << 16) |
                        (((int)(mFromARGB[2] + (mToARGB[2] - mFromARGB[2]) * frac) & 0xff) << 8 )|
                        ((int)( mFromARGB[3] + (mToARGB[3] - mFromARGB[3]) * frac) & 0xff);

                Log.d(TAG, "onAnimationUpdate: BACK_COLOR: " + mCurrentBackColor);
                root_FrameLayout.setBackgroundColor(mCurrentBackColor);
            }
        });*/

        scriptEngine.setReadCommandListener(new OnReadCommandListener() {

            @Override
            public void onBackground(int color) {
                /*toARGB(color, mToARGB);
                toARGB(mCurrentBackColor, mFromARGB);
                Log.d(TAG, "onBackground: CURRENT_BACK_COLOR: " + mCurrentBackColor + " FROM: "
                        + Arrays.toString(mFromARGB) + " TO: " + Arrays.toString(mToARGB));
                mAnimatorColor.start();*/
                mColorRevealView.start(color);
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
                    File tempSFX = ScriptEngine.createTempFile(
                            sfx,
                            ".mp3",
                            context.getCacheDir()
                    );

                    mSFXPool.load(tempSFX.getPath(), 1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAmbient(byte[] ambientMusic) {
                try {

                    File tempAmbient = ScriptEngine.createTempFile(
                            ambientMusic,
                            ".mp3",
                            context.getCacheDir());

                    if (mAmbientPlayer == null) { // First start
                        mAmbientPlayer = MediaPlayer.create(context, Uri.fromFile(tempAmbient));
                        mAmbientPlayer.setLooping(true);
                    } else {

                    }

                    mAmbientPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String errorMsg) {
                Utilities.showMessage(errorMsg, context);
            }
        });

        Typeface defTypeface = Typeface.createFromAsset(getAssets(), "mplus_rounded1c_thin.ttf");

        scriptEngine.setOnCreateViewListener(new OnCreateScriptTextViewListener() {
            @Override
            public void onCreate(ScriptTextConfig textConfig) {
                TextViewPhrase phrase = new TextViewPhrase(context);

                phrase.config(textConfig.spannableString,
                        textConfig.textSize,
                        defTypeface);


                root_FrameLayout.addView(phrase,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);

                phrase.fadeIn();

                mCurrentViewPhrase = phrase;
            }
        });


        root_FrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentViewPhrase != null) {
                    mCurrentViewPhrase.fadeOutTransition(sRandom, 2.1f);
                }
                scriptReader.next();
            }
        });

        root_FrameLayout.addView(mColorRevealView,
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        // Show view after configured options for activity
        setContentView(root_FrameLayout);

        scriptReader.next();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAmbientPlayer != null) {
            mAmbientPlayer.stop();
            mAmbientPlayer.release();
        }

        mSFXPool.autoPause();
        mSFXPool.release();
    }
}
