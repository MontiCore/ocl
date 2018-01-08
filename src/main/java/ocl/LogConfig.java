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

import de.se_rwth.commons.logging.Log;

/**
 * @author Sascha Schneiders
 */
public class LogConfig extends Log {
    static Log log;
    boolean disableOutput = true;

    public static void init() {
        log = new LogConfig();
        Log.setLog(log);
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
    protected void doTrace(String msg, String logName) {
        if (!disableOutput) {
            super.doTrace(msg, logName);
        }
    }

}
