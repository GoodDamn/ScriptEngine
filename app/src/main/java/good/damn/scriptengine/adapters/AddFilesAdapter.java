package good.damn.scriptengine.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import good.damn.scriptengine.R;

public class AddFilesAdapter extends FilesAdapter {

    private static final String TAG = "AddFilesAdapter";

    public AddFilesAdapter(OnFileClickListener onFileClickListener) {
        super(onFileClickListener);
    }

    public AddFilesAdapter(OnFileClickListener onFileClickListener, String path) {
        super(onFileClickListener, path);
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "getItemViewType: POSITION: " + position + " FILES: " + mFiles.length);
        if (mFiles.length - 1 == position || mFiles.length == 0) {
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
        if (mFiles.length - 1 == position || mFiles.length == 0) {
            return;
        }
        super.onBindViewHolder(holder, position);
    }

    class AddFileItem extends FileItem {

        public AddFileItem(@NonNull View itemView) {
            super(itemView);
            mTextView.setText("Add file");
            mPreview.setBackgroundColor(0xFFFF0000);
        }
    }
}
