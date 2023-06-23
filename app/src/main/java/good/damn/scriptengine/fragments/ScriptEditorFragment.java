package good.damn.scriptengine.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.models.ResourceReference;
import good.damn.scriptengine.engines.script.models.ScriptBuildResult;
import good.damn.scriptengine.utils.ArrayUtils;
import good.damn.scriptengine.utils.FileUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.utils.Utilities;
import good.damn.scriptengine.views.TextViewPhrase;

public class ScriptEditorFragment extends Fragment {

    private static final String TAG = "ScriptEditorFragment";
    private static final Random sRandom = new Random();

    private boolean isEdited = false;

    private EditText et_phrase;
    private EditText et_script;

    private Piece mPiece;

    public void startScript(Piece piece) {
        mPiece = piece;
        et_phrase.setText(piece.getString());
        et_script.setText(piece.getSourceCode());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_script_editor, container,false);

        Context context = getContext();

        assert context != null;

        Log.d(TAG, "onCreateView: CREATING THE VIEW...");
        et_phrase = v.findViewById(R.id.personalEditor_editText_phrase);
        et_script = v.findViewById(R.id.personalEditor_editText_script);

        ScriptEngine scriptEngine = new ScriptEngine();
        scriptEngine.setSourceEditText(et_phrase);

        v.findViewById(R.id.personalEditor_button_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spannable spannable = et_phrase.getText();
                et_phrase.setText(spannable.toString());
                String[] arr = et_script.getText().toString().split("\n");
                byte[] script = new byte[0];
                byte scriptLength = 0;

                LinkedList<ResourceReference> resPositions = null;

                for (byte i = 0; i < arr.length; i++) {
                    ScriptBuildResult result = scriptEngine.compile(arr[i]);
                    byte[] t = result.getCompiledScript();
                    if (t == null)
                        continue;
                    Log.d(TAG, "onClick: SCRIPT: " + Arrays.toString(t));
                    if (t[1] >= 3 && t[1] <= 6) { // if it's an image, gif or SFX
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

                String t = et_phrase.getText().toString().trim();
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

                mPiece.setString(et_phrase.getText());
                mPiece.setChunk(total);
                mPiece.setResRef(resPositions);
                mPiece.setSourceCode(et_script.getText());

                /*TextViewPhrase textViewPhrase = new TextViewPhrase(context);
                textViewPhrase.setTypeface(et_phrase.getTypeface());
                Log.d(TAG, "onClick: Result: SCRIPT_LENGTH: " + scriptLength + " TEXT_LENGTH: " + text.length + " TEXT_SIZE_BYTES: "+ t.getBytes(StandardCharsets.UTF_8).length + " TOTAL_LENTGH: " + total.length);
                scriptEngine.read(total, textViewPhrase);
                Log.d(TAG, "onClick: isEdited: COMPLETED: " + isEdited);
                isEdited = false;*/
            }
        });
        et_phrase.setText("Simple a piece of text");

        /*findViewById(R.id.view_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: isEdited: " + isEdited);
                if (isEdited) {
                    Utilities.showMessage(this, "Click on START button");
                    return;
                }

                mContainer.setVisibility(View.INVISIBLE);
                textView.setText(et_phrase.getText());
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,et_phrase.getTextSize());
            }
        });*/

        et_phrase.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: isEdited: " + isEdited);
                isEdited = true;
            }
        });

        v.findViewById(R.id.personalEditor_selectFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToolsUtilities.startFileManager(getActivity(), new FilesAdapter.OnFileClickListener() {
                    @Override public void onClickedFolder(String prevFolder, String currentFolder) { }
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
}
