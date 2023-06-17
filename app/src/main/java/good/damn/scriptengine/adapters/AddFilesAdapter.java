package good.damn.scriptengine.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import good.damn.scriptengine.R;
import good.damn.scriptengine.utils.FileUtils;
import good.damn.scriptengine.utils.ToolsUtilities;
import good.damn.scriptengine.utils.Utilities;

public class AddFilesAdapter extends FilesAdapter {

    private static final String TAG = "AddFilesAdapter";

    private final Activity mActivity;

    public AddFilesAdapter(OnFileClickListener onFileClickListener, Activity activity) {
        super(onFileClickListener);
        mActivity = activity;
    }

    public AddFilesAdapter(OnFileClickListener onFileClickListener, String path, Activity activity) {
        super(onFileClickListener, path);
        mActivity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: POSITION: " + position + " FILES: " + mFiles.length);
        if (mFiles.length == position) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + super.getItemCount());
        return super.getItemCount() + 1;
    }

    @NonNull
    @Override
    public FileItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: VIEW TYPE: " + viewType);
        if (viewType == 1) {
            return new AddFileItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_file_row, parent, false));
        }
        return super.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull FileItem holder, int position) {
        if (mFiles.length == position) {
            return;
        }
        super.onBindViewHolder(holder, position);
    }

    private void copyFile(File sourceFile) {
        Utilities.showMessage("COPYING FILE...", mActivity);

        File resFile = new File(mActivity.getCacheDir()+ FileUtils.RES_DIR+"/"+sourceFile
                .getName().replace(" ","-"));

        FileInputStream fis;
        FileOutputStream fos;

        try {

            if (!resFile.exists() && resFile.createNewFile()) {
                Log.d(TAG, "copyFile: " + resFile.getName() + " HAS BEEN CREATED!");
            }


            fis = new FileInputStream(sourceFile);
            fos = new FileOutputStream(resFile);

            byte[] buffer = new byte[2048];
            int n;
            while ((n = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, n);
            }

            fis.close();
            fos.close();

            Utilities.showMessage(sourceFile.getName() + " HAS BEEN COPIED!", mActivity);
            notifyDataSet();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class AddFileItem extends FileItem {

        public AddFileItem(@NonNull View itemView) {
            super(itemView);
            mTextView.setText("Add file");
            mPreview.setBackgroundResource(R.drawable.ic_add);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToolsUtilities.startFileManager(mActivity, new OnFileClickListener() {
                        @Override
                        public void onClickedFolder(String prevFolder, String currentFolder) {}

                        @Override
                        public void onAudioFile(File file) {
                            Log.d(TAG, "onAudioFile: FILE: " + file);
                            copyFile(file);
                        }

                        @Override
                        public void onImageFile(File file) {
                            Log.d(TAG, "onImageFile: " + file);
                            copyFile(file);
                        }
                    });
                }
            });
        }
    }
}
