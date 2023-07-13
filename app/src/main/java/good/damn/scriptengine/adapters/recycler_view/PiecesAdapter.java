package good.damn.scriptengine.adapters.recycler_view;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import good.damn.scriptengine.interfaces.OnClickTextPiece;
import good.damn.scriptengine.models.Piece;
import good.damn.scriptengine.utils.ArrayUtils;

public class PiecesAdapter extends RecyclerView.Adapter<PiecesAdapter.PiecesViewHolder> {

    private static final String TAG = "PiecesAdapter";

    private ArrayList<Piece> mPieces;

    private final OnClickTextPiece mOnClickTextPiece;

    public PiecesAdapter(ArrayList<Piece> pieces, OnClickTextPiece textPiece) {
        Log.d(TAG, "PiecesAdapter: ADAPTER_SIZE:" + pieces.size());
        mPieces = pieces;
        mOnClickTextPiece = textPiece;
    }

    public void setPieces(ArrayList<Piece> pieces) {
        mPieces = pieces;
    }

    @NonNull
    @Override
    public PiecesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200));
        return new PiecesViewHolder(textView);
    }

    @Override
    public void onBindViewHolder(@NonNull PiecesViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: POSITION: " + position);
        holder.mTextView.setText(mPieces.get(position).getString());
    }

    @Override
    public int getItemCount() {
        return mPieces.size();
    }

    public class PiecesViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextView;

        public PiecesViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextView = (TextView) itemView;

            mTextView.setOnClickListener((view) -> {
                int position = getAdapterPosition();
                mOnClickTextPiece.onClick(mPieces.get(position),position);
            });
        }
    }
}
