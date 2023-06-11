package good.damn.scriptengine.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileInputStream;
import java.io.IOException;

import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.engines.script.ScriptReader;
import good.damn.scriptengine.utils.FileReaderUtils;
import good.damn.scriptengine.utils.Utilities;

public class PreviewActivity extends AppCompatActivity {

    private static final String TAG = "PreviewActivity";

    private byte[] mContent;

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

        setContentView(root_FrameLayout);

        root_FrameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scriptReader.next();
            }
        });

        scriptReader.next();
    }
}
