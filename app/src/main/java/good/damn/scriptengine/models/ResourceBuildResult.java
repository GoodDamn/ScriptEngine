package good.damn.scriptengine.models;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class ResourceBuildResult {

    private final String[] mCompiledResources;
    private final File mOutFile;

    public ResourceBuildResult(String[] res, File file) {
        mCompiledResources = res;
        mOutFile = file;
    }

    public File getOutFile() {
        return mOutFile;
    }

    public String[] getCompiledResources() {
        return mCompiledResources;
    }
}
