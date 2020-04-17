/**
 *
 * (c) https://github.com/MontiCore/monticore
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/**
 *
 * /* (c) https://github.com/MontiCore/monticore
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/* (c) https://github.com/MontiCore/monticore */
package ocl;

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;

import javax.xml.transform.Source;

/**
 * @author Sascha Schneiders
 */
public class LogConfig extends Log {
    static Log log;
    boolean disableInfo = false;
    boolean disableDebug = true;
    boolean disableTrace = true;


    public static void init() {
        log = new LogConfig();
        Log.setLog(log);
        Log.enableFailQuick(false);
    }

    /**
     * Disable output for debug, trace and info if disable options are set
     */

    @Override
    protected void doInfo(String msg, String logName) {
        if (!disableInfo) {
            super.doInfo(msg, logName);
        }
    }

    @Override
    protected void doDebug(String msg, String logName) {
        if (!disableDebug) {
            super.doDebug(msg, logName);
        }
    }

    @Override
    protected void doTrace(String msg, String logName) {
        if (!disableTrace) {
            super.doTrace(msg, logName);
        }
    }

    /**
     * Reformat printing of error and warn
     */

    @Override
    protected void doError(String msg, SourcePosition pos) {
        Finding error = Finding.error(msg, pos);
        addFinding(error);
        doErrPrint("[ERROR: " + pos.toString() + "] " + error.getMsg());
        terminateIfErrors();
    }

    @Override
    protected void doError(String msg, SourcePosition start, SourcePosition end) {
        Finding error = Finding.error(msg, start, end);
        addFinding(error);
        doErrPrint("[ERROR: " + start.toString() + " - " + end.toString() + "]" +  error.getMsg());
        terminateIfErrors();
    }

    @Override
    protected void doWarn(String msg, SourcePosition pos) {
        Finding warn = Finding.warning(msg, pos);
        addFinding(warn);
        doErrPrint("[WARN] " + warn.getMsg());
        terminateIfErrors();
    }

    @Override
    protected void doWarn(String msg, SourcePosition start, SourcePosition end) {
        Finding warn = Finding.warning(msg, start, end);
        addFinding(warn);
        doErrPrint("[WARN] " + warn.getMsg());
        terminateIfErrors();
    }
}
