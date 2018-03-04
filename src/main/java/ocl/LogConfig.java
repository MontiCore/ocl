/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
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
    boolean disableOutput = true;

    public static void init() {
        log = new LogConfig();
        Log.setLog(log);
        Log.enableFailQuick(false);
    }

    @Override
    protected void doInfo(String msg, Throwable t, String logName) {
        if (!disableOutput) {
            super.doInfo(msg, t, logName);
        }
    }

    @Override
    protected void doInfo(String msg, String logName) {
        if (!disableOutput) {
            super.doInfo(msg, logName);
        }
    }


    @Override
    protected void doDebug(String msg, Throwable t, String logName) {
        if (!disableOutput) {
            super.doDebug(msg, t, logName);
        }
    }

    @Override
    protected void doDebug(String msg, String logName) {
        if (!disableOutput) {
            super.doDebug(msg, logName);
        }
    }


    @Override
    protected void doTrace(String msg, Throwable t, String logName) {
        if (!disableOutput) {
            super.doTrace(msg, t, logName);
        }
    }


    @Override
    protected void doError(String msg, SourcePosition pos) {
        Finding error = Finding.error(msg, pos);
        addFinding(error);
        doErrPrint("[ERROR] " + error.getMsg());
        terminateIfErrors();
    }

    @Override
    protected void doError(String msg, SourcePosition start, SourcePosition end) {
        Finding error = Finding.error(msg, start, end);
        addFinding(error);
        doErrPrint("[ERROR] " + error.getMsg());
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
