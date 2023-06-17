package good.damn.scriptengine.engines.script.models;

public class ScriptBuildResult {

    private String mResName;
    private byte[] mCompiledScript;

    public ScriptBuildResult() {}

    public ScriptBuildResult(String resName, byte[] compiledScript) {
        mResName = resName;
        mCompiledScript = compiledScript;
    }

    public byte[] getCompiledScript() {
        return mCompiledScript;
    }

    public String getResName() {
        return mResName;
    }

    public void setResName(String resName) {
        mResName = resName;
    }

    public void setCompiledScript(byte[] script) {
        mCompiledScript = script;
    }
}
