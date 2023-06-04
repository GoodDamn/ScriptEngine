package good.damn.scriptengine.adapters;

import android.annotation.SuppressLint;
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

    private File[] files;
    private final Click click;
    public final ArrayList<String> pages;

    public interface Click{
        void onClickedFolder(String prevFolder, String currentFolder);
        void onAudioFile(File file);
        void onImageFile(File file);
    }

    private static final String TAG = "FilesAdapter";
    public FilesAdapter(Click click){
        files = Environment.getExternalStorageDirectory().listFiles();
        Log.d(TAG, "FilesAdapter: " + Environment.getExternalStorageDirectory().getAbsolutePath() + " Files:" + files.length);
        pages = new ArrayList<>();
        this.click = click;
    }

    private String getCurrentPath(){
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        for (String p: pages){
            path += ("/"+p);
        }
        return path;
    }

    public void updatePath(){
        files = new File(getCurrentPath(),"").listFiles();
        String prevFolder = "";
        String currentFolder = "Device";

        if (pages.size() >= 1)
            currentFolder = pages.get(pages.size()-1);

        if (pages.size() == 1)
            prevFolder = "Device";
        else if (pages.size() > 1)
            prevFolder = pages.get(pages.size()-2);
        click.onClickedFolder(prevFolder,currentFolder);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FileItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileItem(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_file_row, parent,false));
    }

    private String getMimeType(File file){
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(Uri.parse(Uri.fromFile(file).toString()).getEncodedSchemeSpecificPart()));
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull FileItem holder, int position) {
        File f = files[position];
        holder.name_file.setText(f.getName());
        Log.d(TAG, "onBindViewHolder: " + f.isDirectory() + " " + f.getName());
        String mimeType = getMimeType(f);
        Log.d(TAG, "onBindViewHolder: " + mimeType);
        holder.isImageFile = mimeType != null && mimeType.contains("image");
        holder.isAudioFile = mimeType != null && mimeType.contains("audio/mpeg");
        holder.preview.setBackgroundResource(R.drawable.ic_file);

        if (f.isDirectory()){
            holder.preview.setBackgroundResource(R.drawable.ic_baseline_folder_24);
        }

        if (holder.isImageFile){
            try {
                holder.preview.setBackground(new BitmapDrawable(holder.itemView.getContext().getResources(),
                        MediaStore.Images.Media.getBitmap(
                                holder.itemView.getContext().getContentResolver(),
                                Uri.fromFile(f))
                ));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (holder.isAudioFile){
            holder.preview.setBackgroundResource(R.drawable.ic_music_note);
        }
    }

    @Override
    public int getItemCount() {
        if (files == null)
            return 0;
        return files.length;
    }

    class FileItem extends RecyclerView.ViewHolder{
        boolean isImageFile = false,
            isAudioFile = false;
        TextView name_file;
        ImageView preview;
        public FileItem(@NonNull View itemView) {
            super(itemView);
            name_file = itemView.findViewById(R.id.file_manager_tv_item);
            preview = itemView.findViewById(R.id.file_manager_iv_item);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pages.add(name_file.getText().toString());
                    if (isImageFile){
                        click.onImageFile(files[getAdapterPosition()]);
                        return;
                    }
                    if (isAudioFile) {
                        click.onAudioFile(files[getAdapterPosition()]);
                        return;
                    }
                    updatePath();
                }
            });
        }
    }
}
