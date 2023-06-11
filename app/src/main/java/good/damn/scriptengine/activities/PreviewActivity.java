package good.damn.scriptengine.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;

import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.engines.script.ScriptReader;
import good.damn.scriptengine.engines.script.interfaces.OnConfigureViewListener;
import good.damn.scriptengine.utils.FileReaderUtils;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";
    private static final Random sRandom = new Random();

    private byte[] mContent;

    private TextViewPhrase mCurrentViewPhrase;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Log.d(TAG, "onCreate: INITIALIZING CONTENT FILE .SKC");

        String path = getIntent().getStringExtra("dumbPath");

        if (path == null) {
            Utilities.showMessage("INVALID DUMB PATH",
                    this);
            return;
        }

        try {
            mContent = FileReaderUtils.getBytesFromFile(new FileInputStream(path));
        } catch (IOException exception) {
            exception.printStackTrace();
            Utilities.showMessage("ERROR WITH GETTING A CONTENT FILE",
                    this);
            return;
        }

        ScriptEngine scriptEngine = new ScriptEngine(this);
        ScriptReader scriptReader = new ScriptReader(scriptEngine, mContent);

        FrameLayout root_FrameLayout = new FrameLayout(this);

        scriptEngine.setRootViewGroup(root_FrameLayout);

        scriptEngine.setOnConfigureView(new OnConfigureViewListener() {
            @Override
            public void onConfigured(TextViewPhrase textViewPhrase) {
                textViewPhrase.animate()
                        .alpha(1.0f)
                        .setDuration(1500)
                        .start();
                mCurrentViewPhrase = textViewPhrase;
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
