package good.damn.scriptengine.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Movie;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import good.damn.scriptengine.R;
import good.damn.scriptengine.adapters.FilesAdapter;
import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.engines.script.interfaces.OnCreateScriptTextViewListener;
import good.damn.scriptengine.engines.script.interfaces.OnReadCommandListener;
import good.damn.scriptengine.engines.script.models.ScriptGraphicsFile;
import good.damn.scriptengine.engines.script.models.ScriptTextConfig;
import good.damn.scriptengine.engines.script.utils.ToolsScriptEngine;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.models.ResourceReference;
import good.damn.scriptengine.engines.script.models.ScriptBuildResult;
import good.damn.scriptengine.utils.ArrayUtils;
import good.damn.scriptengine.utils.FileUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.utils.Utilities;
import good.damn.traceview.activities.VectorActivity;
import good.damn.traceview.models.FileSVC;

public class ScriptEditorFragment extends Fragment {

    private static final String TAG = "ScriptEditorFragment";
    private static final Random sRandom = new Random();

    private boolean isEdited = false;
    private int mAdapterPosition;

    private EditText et_phrase;
    private EditText et_script;

    private Piece mPiece;

    public void startScript(Piece piece, int adapterPosition) {
        mPiece = piece;
        et_phrase.setText(piece.getString());
        Log.d(TAG, "startScript: CODE: " + piece.getSourceCode());
        et_script.setText(piece.getSourceCode());
        mAdapterPosition = adapterPosition;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_script_editor, container,false);

        Context context = getContext();

        assert context != null;

        Log.d(TAG, "onCreateView: CREATING THE VIEW...");
        et_phrase = v.findViewById(R.id.scriptEditor_editText_phrase);
        et_script = v.findViewById(R.id.scriptEditor_editText_script);

        TextView textViewHelper = v.findViewById(R.id.script_editor_tv_helper);

        ToolsScriptEngine scriptEngine = new ToolsScriptEngine(new OnReadCommandListener() {
            @Override public void onBackground(int color) {}
            @Override public void onImage(Bitmap bitmap, ScriptGraphicsFile graphicsFile) {}
            @Override public void onGif(Movie movie, ScriptGraphicsFile gifScript) {}
            @Override public void onSFX(byte soundID, SoundPool soundPool, String fileName) {}
            @Override public void onAmbient(MediaPlayer mediaPlayer, String fileName) {}
            @Override public void onError(String errorMsg) {}
            @Override public void onVector(FileSVC fileSVC, String[] advancedText, String fileName) {}
        });

        scriptEngine.setOnCreateViewListener(new OnCreateScriptTextViewListener() {
            @Override
            public void onCreate(ScriptTextConfig config) {
                Log.d(TAG, "onCreate: ScriptTextConfig: " + config.textColor + " " + config.spannableString.toString());
                if (config.textColor == 0xff000000) {
                    config.textColor = 0xffffffff;
                }
                et_phrase.setTextColor(config.textColor);
                et_phrase.setTextSize(config.textSize);
                et_phrase.setText(config.spannableString);
            }
        });

        v.findViewById(R.id.scriptEditor_button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spannable spannable = et_phrase.getText();
                et_phrase.setText(spannable.toString());

                CompileScript(
                        et_phrase.getText().toString(),
                        et_script.getText().toString(),
                        mPiece,
                        scriptEngine,
                        v.getContext());

                int length = Utilities.gn(mPiece.getChunk(),0);
                byte[] chunk = new byte[length+2];
                System.arraycopy(mPiece.getChunk(),4, chunk,0,chunk.length);
                scriptEngine.read(chunk);
            }
        });
        et_phrase.setText("Simple a piece of text");

        et_phrase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewHelper.setText(et_phrase.getSelectionStart() + ";" + et_phrase.getText().length());
            }
        });

        et_phrase.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textViewHelper.setText(start + " " + et_phrase.getText().length());
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: isEdited: " + isEdited);
                isEdited = true;
            }
        });

        v.findViewById(R.id.scriptEditor_vectorEditor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, VectorActivity.class);
                String t = et_phrase.getText().toString();
                byte n = 15;
                if (t.length() < n) {
                    n = (byte) t.length();
                }
                intent.putExtra("fileName", mAdapterPosition+"_"+t.substring(0,n).replace(" ","_")+".svc");
                startActivity(intent);
            }
        });

        v.findViewById(R.id.scriptEditor_selectFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToolsUtilities.startFileManager(getActivity(), new FilesAdapter.OnFileClickListener() {
                    @Override
                    public void onVectorFile(File file) {
                        onImageFile(file);
                    }

                    @Override
                    public void onAudioFile(File file) {
                        onImageFile(file);
                    }

                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onImageFile(File file) {
                        et_script.setText(et_script.getText().toString().trim() + " " + file.getName());
                        try {
                            Log.d(TAG, "onImageFile: CANON: " + file.getCanonicalPath() + " ABS:" + file.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, context.getCacheDir() + FileUtils.RES_DIR);
            }
        });

        return v;
    }

    public static void CompileScript(String textPiece,
                                     String scriptString,
                                     Piece piece,
                                     ToolsScriptEngine scriptEngine,
                                     Context context) {
        String[] arr = scriptString.split("\n");
        byte[] script = new byte[0];
        byte scriptLength = 0;

        LinkedList<ResourceReference> resPositions = null;

        for (byte i = 0; i < arr.length; i++) {
            ScriptBuildResult result = scriptEngine.compile(arr[i],context);
            byte[] t = result.getCompiledScript();
            if (t == null)
                continue;
            Log.d(TAG, "onClick: SCRIPT: " + Arrays.toString(t));
            if (result.hasResource()) {
                if (resPositions == null) {
                    resPositions = new LinkedList<>();
                }

                resPositions.add(new ResourceReference(result.getResName(),
                        scriptLength+t[0]));
            }
            script = ArrayUtils.concatByteArrays(script,t);
            scriptLength += t.length;
            Log.d(TAG, "onClick: PARSED_INFO: FOR SCRIPT: " + arr[i] + " SCRIPT_BYTE_LENGTH: " + t.length + " TOTAL_LENGTH:" + script.length);
        }

        Log.d(TAG, "onClick: SCRIPTS: " + Arrays.toString(script));

        Log.d(TAG, "onClick: RES_POSITIONS: " + resPositions);

        String t = textPiece.trim();
        byte[] text = ArrayUtils.concatByteArrays(t.getBytes(StandardCharsets.UTF_8),
                new byte[]{0});

        int length = text.length+1+script.length;

        Log.d(TAG, "onClick: CHUNK_LENGTH: FACT: " + length);

        byte[] chunkLength = Utilities.gbInt(length);
        Log.d(TAG, "onClick: OWN: " + Arrays.toString(chunkLength));

        byte[] total = ArrayUtils.concatByteArrays(
                chunkLength,
                Utilities.gb((short) text.length),
                text,
                new byte[]{scriptLength},
                script);

        piece.setString(textPiece);
        piece.setChunk(total);
        piece.setResRef(resPositions);
        piece.setSourceCode(scriptString);
    }
}
