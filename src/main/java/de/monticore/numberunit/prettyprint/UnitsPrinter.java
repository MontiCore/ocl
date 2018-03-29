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
package de.monticore.numberunit.prettyprint;

import javax.measure.unit.SI;
import javax.measure.unit.Unit;

/**
 * Created by Ferdinand Mehlan on 12.01.2018.
 */
public class UnitsPrinter {

    public static String unitToUnitName(Unit<?> unit) {
        String unitName = "";

        if(unit.isCompatible(SI.METER))
            unitName = "Length";
        if(unit.isCompatible(SI.METERS_PER_SECOND))
            unitName = "Velocity";
        if(unit.isCompatible(SI.METERS_PER_SQUARE_SECOND))
            unitName = "Acceleration";
        if(unit.isCompatible(SI.NEWTON))
            unitName = "Force";
        if(unit.isCompatible(SI.CELSIUS))
            unitName = "Temperature";
        if(unit.isCompatible(SI.CUBIC_METRE))
            unitName = "Volume";
        if(unit.isCompatible(SI.SECOND))
            unitName = "Duration";

        // Todo: add more units
        if(unitName.equals(""))
            unitName = "Amount";

        return unitName;
    }

}
