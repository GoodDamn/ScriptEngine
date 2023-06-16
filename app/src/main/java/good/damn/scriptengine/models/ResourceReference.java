package good.damn.scriptengine.models;

import java.util.LinkedList;

public class ResourceReference {
    private String mResName;
    private int mResPosition;

    public ResourceReference(String resName, int resPosition) {
        mResName = resName;
        mResPosition = resPosition;
    }

    public String getResName() {
        return mResName;
    }

    public int getResPosition() {
        return mResPosition;
    }
}
