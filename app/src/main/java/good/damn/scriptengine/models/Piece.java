package good.damn.scriptengine.models;

import android.text.SpannableString;

public class Piece {

    private SpannableString mString;
    private byte[] mChunk;

    public Piece(byte[] chunk, SpannableString spannableString) {
        mChunk = chunk;
        mString = spannableString;
    }

    public void setChunk(byte[] mChunk) {
        this.mChunk = mChunk;
    }

    public void setString(SpannableString mString) {
        this.mString = mString;
    }

    public byte[] getChunk() {
        return mChunk;
    }

    public SpannableString getString() {
        return mString;
    }
}
