package good.damn.scriptengine.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import good.damn.scriptengine.R;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileItem> {

    private static final String TAG = "FilesAdapter";

    public final ArrayList<String> mPages;

    protected File[] mFiles;
    protected final String mPath;

    private final OnFileClickListener onFileClickListener;

    public interface OnFileClickListener {
        void onClickedFolder(String prevFolder, String currentFolder);
        void onAudioFile(File file);
        void onImageFile(File file);
    }

    private String getMimeType(File file){
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.parse(Uri.fromFile(file).toString()).getEncodedSchemeSpecificPart()));
    }

    private String getCurrentPath(){
        String st = new String(mPath);
        for (String p: mPages){
            st += ("/"+p);
        }
        return st;
    }

    public void updatePath() {
        String prevFolder = "";
        String currentFolder = "Device";

        Log.d(TAG, "updatePath: FILES: "+ mFiles.length);

        if (mPages.size() >= 1)
            currentFolder = mPages.get(mPages.size()-1);

        if (mPages.size() == 1)
            prevFolder = "Device";
        else if (mPages.size() > 1)
            prevFolder = mPages.get(mPages.size()-2);
        onFileClickListener.onClickedFolder(prevFolder,currentFolder);
        notifyDataSet();
    }

    public void notifyDataSet() {
        mFiles = new File(mPath).listFiles();
        notifyDataSetChanged();
    }

    public FilesAdapter(OnFileClickListener onFileClickListener){
        File dirEx = Environment.getExternalStorageDirectory();
        mPath = dirEx.getAbsolutePath();
        mFiles = dirEx.listFiles();
        Log.d(TAG, "FilesAdapter: PATH: " + mPath + " FILES:" + mFiles.length);
        mPages = new ArrayList<>();
        this.onFileClickListener = onFileClickListener;
    }

    public FilesAdapter(OnFileClickListener onFileClickListener, String path) {
        mPath = path;
        mFiles = new File(path).listFiles();
        Log.d(TAG, "FilesAdapter: PATH: " + path);
        Log.d(TAG, "FilesAdapter: FILES: " + mFiles.length);
        mPages = new ArrayList<>();
        this.onFileClickListener = onFileClickListener;
    }

    @NonNull
    @Override
    public FileItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_file_row, parent,false));
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull FileItem holder, int position) {
        File f = mFiles[position];
        holder.mTextView.setText(f.getName());
        Log.d(TAG, "onBindViewHolder: " + f.isDirectory() + " " + f.getName());
        String mimeType = getMimeType(f);
        Log.d(TAG, "onBindViewHolder: " + mimeType);
        if (mimeType != null) {
            holder.isImageFile = mimeType.contains("image");
            holder.isAudioFile = mimeType.contains("audio/mpeg");
        }

        holder.mPreview.setBackgroundResource(R.drawable.ic_file);

        if (f.isDirectory()){
            holder.mPreview.setBackgroundResource(R.drawable.ic_folder);
        }

        if (holder.isImageFile){
                holder.mPreview.post(()->{
                    try {
                        holder.mPreview.setBackground(new BitmapDrawable(
                                    Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(
                                            holder.itemView.getContext().getContentResolver(),
                                            Uri.fromFile(f)),
                                    holder.mPreview.getWidth(),
                                    holder.mPreview.getHeight(),
                                    false)
                                )
                        );
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }

        if (holder.isAudioFile){
            holder.mPreview.setBackgroundResource(R.drawable.ic_music_note);
        }
    }

    @Override
    public int getItemCount() {
        if (mFiles == null)
            return 0;
        return mFiles.length;
    }

    public class FileItem extends RecyclerView.ViewHolder{
        boolean isImageFile = false,
            isAudioFile = false;
        TextView mTextView;
        ImageView mPreview;

        public String getFileFullName() {
            return mTextView.getText().toString();
        }

        public FileItem(@NonNull View itemView) {
            super(itemView);
            mTextView = itemView.findViewById(R.id.file_manager_tv_item);
            mPreview = itemView.findViewById(R.id.file_manager_iv_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mPages.add(mTextView.getText().toString());
                    if (isImageFile){
                        onFileClickListener.onImageFile(mFiles[getAdapterPosition()]);
                        return;
                    }
                    if (isAudioFile) {
                        onFileClickListener.onAudioFile(mFiles[getAdapterPosition()]);
                        return;
                    }
                    updatePath();
                }
            });
        }
    }
}
