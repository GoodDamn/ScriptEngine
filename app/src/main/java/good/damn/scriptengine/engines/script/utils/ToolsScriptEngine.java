package good.damn.scriptengine.engines.script.utils;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.HashMap;

import good.damn.scriptengine.engines.script.ScriptEngine;
import good.damn.scriptengine.engines.script.interfaces.OnReadCommandListener;
import good.damn.scriptengine.engines.script.models.ScriptBuildResult;
import good.damn.scriptengine.utils.Utilities;

public class ToolsScriptEngine extends ScriptEngine {

    private final HashMap<String, ReadCommand> mReadCommands = new HashMap<>();

    public ToolsScriptEngine(@NonNull OnReadCommandListener rr) {
        super(rr);
        initCommands();
    }

    public ScriptBuildResult compile(String line, Context context) {
        String[] argv = line.split("\\s+");

        ScriptBuildResult scriptBuildResult = new ScriptBuildResult();

        argv[0] = argv[0].trim();

        if (argv[0].isEmpty())
            return scriptBuildResult;

        ReadCommand command = mReadCommands.get(argv[0].toLowerCase());

        if (command == null) {
            Utilities.showMessage("Invalid command: " + argv[0], context);
        } else {
            scriptBuildResult.setCompiledScript(
                    command.read(argv, context, scriptBuildResult)
            );
        }

        return scriptBuildResult;
    }

    private void initCommands() {
        mReadCommands.put("textSize", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.TextSize(argv, context));

        mReadCommands.put("font", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.Font(argv, context));

        mReadCommands.put("bg", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.Background(argv, context));

        mReadCommands.put("img", ScriptCommandsUtils::Image);
        mReadCommands.put("gif", ScriptCommandsUtils::Gif);

        mReadCommands.put("sfx", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.SFX(argv, scriptBuildResult));

        mReadCommands.put("amb",
                (argv, context, scriptBuildResult) ->
                        ScriptCommandsUtils.Ambient(argv, scriptBuildResult));

        mReadCommands.put("vect", (argv, context, scriptBuildResult) ->
                ScriptCommandsUtils.Vector(argv, scriptBuildResult));
    }

    private interface ReadCommand {
        byte[] read(String[] argv, Context context, ScriptBuildResult scriptBuildResult);
    }
}
