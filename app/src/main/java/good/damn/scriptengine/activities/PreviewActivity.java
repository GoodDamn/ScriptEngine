package good.damn.scriptengine.activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ViewAnimator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import java.io.ByteArrayInputStream;
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
import good.damn.scriptengine.interfaces.ScriptReaderListener;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.ColorRevealView;
import good.damn.scriptengine.views.GifView;
import good.damn.scriptengine.views.TextViewPhrase;
import good.damn.traceview.interfaces.OnTraceFinishListener;
import good.damn.traceview.models.FileSVC;
import good.damn.traceview.utils.FileUtils;
import good.damn.traceview.views.TraceView;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";
    private static final Random sRandom = new Random();

    private int mHorizontalPadding = -1;
    private int mTextYWithTraceView = 50;
    private int mCurrentTextColor = 0xffffffff;
    private boolean mHasTraceView = false;

    private TextViewPhrase mCurrentViewPhrase;

    private MediaPlayer mAmbientPlayer;

    private SoundPool mSFXPool;

    private void releaseResources() {
        if (mAmbientPlayer != null) {
            mAmbientPlayer.stop();
            mAmbientPlayer.release();
        }

        mSFXPool.autoPause();
        mSFXPool.release();
    }

    @SuppressLint("ClickableViewAccessibility")
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

        Typeface defTypeface = Typeface.createFromAsset(getAssets(), "mplus_rounded1c_bold.ttf");

        scriptReader.setScriptReaderListener(new ScriptReaderListener() {
            @Override
            public void onReadFinish() {
                root_FrameLayout.setEnabled(false);
                new Handler().postDelayed(() -> {
                    releaseResources();
                    finish();
                }, 3000);
            }
        });

        scriptEngine.setReadCommandListener(new OnReadCommandListener() {
            @Override
            public void onBackground(int color) {
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
                gifView.setId(ViewCompat.generateViewId());

                gifView.setGifListener(new GifView.GifListener() {
                    @Override
                    public void onFinish() {
                        root_FrameLayout.removeView(gifView);
                    }
                });

                FrameLayout.LayoutParams par =
                        new FrameLayout.LayoutParams(gifView.width(), gifView.height());

                par.gravity = Gravity.START | Gravity.TOP;
                par.leftMargin = (int) (gifScript.x * metrics.widthPixels);
                par.topMargin = (int) (gifScript.y * metrics.heightPixels);

                root_FrameLayout.addView(gifView, par);
                gifView.play();
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
            public void onVector(byte[] vect) {
                root_FrameLayout.setEnabled(false);

                mHasTraceView = true;

                TraceView traceView = new TraceView(PreviewActivity.this);
                traceView.setId(ViewCompat.generateViewId());
                traceView.setBackgroundColor(0);

                FileSVC fileSVC = FileUtils.retrieveSVCFile(vect);

                traceView.setVectorsSource(fileSVC);
                traceView.setOnTraceFinishListener(new OnTraceFinishListener() {
                    @Override
                    public void onFinish() {
                        root_FrameLayout.setEnabled(true);

                        if (mCurrentViewPhrase != null) {
                            mCurrentViewPhrase.fadeOutTransition(sRandom, 2.1f);
                        }

                        scriptReader.next();

                        traceView.animate()
                                .alpha(0.0f)
                                .withEndAction(()-> root_FrameLayout.removeView(traceView))
                                .start();
                    }
                });

                root_FrameLayout.addView(traceView,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);

                traceView.setAlpha(0);

                traceView.animate()
                        .alpha(1.0f)
                        .withEndAction(traceView::startAnimation)
                        .setDuration(1650)
                        .start();


            }

            @Override
            public void onError(String errorMsg) {
                Utilities.showMessage(errorMsg, context);
            }
        });

        scriptEngine.setOnCreateViewListener(new OnCreateScriptTextViewListener() {
            @Override
            public void onCreate(ScriptTextConfig textConfig) {
                TextViewPhrase phrase = new TextViewPhrase(context);

                if (mHorizontalPadding == -1) {
                    phrase.post(() -> {
                        mTextYWithTraceView = (int) (phrase.getHeight() * 0.35f);
                        mHorizontalPadding = (int) (phrase.getWidth() * 0.1f);
                    });
                }

                if (textConfig.textColor != 0xff000000) {
                    mCurrentTextColor = textConfig.textColor;
                }

                phrase.setTextColor(mCurrentTextColor);
                phrase.setTypeface(defTypeface);
                phrase.setTextSize(textConfig.textSize);
                phrase.setText(textConfig.spannableString);
                phrase.setGravity(Gravity.CENTER);
                phrase.setAlpha(0.0f);

                root_FrameLayout.addView(phrase,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT);

                phrase.setPadding(mHorizontalPadding,0,mHorizontalPadding,0);

                ViewPropertyAnimator animator =
                        phrase.animate()
                                .alpha(1.0f)
                                .setDuration(1500);

                if (mHasTraceView) {
                    animator = animator.y(mTextYWithTraceView);
                    mHasTraceView = false;
                }

                animator.start();
                mCurrentViewPhrase = phrase;
            }
        });

        root_FrameLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:
                        mColorRevealView.setCenterPoint(motionEvent.getX(),
                                motionEvent.getY());
                        if (mCurrentViewPhrase != null) {
                            mCurrentViewPhrase.fadeOutTransition(sRandom, 2.1f);
                        }
                        scriptReader.next();
                        break;
                }

                return true;
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
        releaseResources();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView()
                    .setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_IMMERSIVE |
                                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    );
        }
    }
}
