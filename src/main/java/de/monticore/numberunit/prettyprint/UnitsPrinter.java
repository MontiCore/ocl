/* (c) https://github.com/MontiCore/monticore */
package de.monticore.numberunit.prettyprint;

import org.jscience.economics.money.Currency;

import javax.measure.unit.*;

/**
 * Created by Ferdinand Mehlan on 12.01.2018.
 */
public class UnitsPrinter {

    public static String unitToUnitName(Unit<?> unit) {
        String unitName = "Number";

        if(unit.isCompatible(SI.METERS_PER_SQUARE_SECOND)) { unitName = "Acceleration"; }
        else if(unit.isCompatible(SI.RADIAN)) { unitName = "Angle"; }
        else if(unit.isCompatible(SI.MOLE)) { unitName = "AmountOfSubstance"; }
        else if(unit.isCompatible(Unit.valueOf("rad/s*s"))) { unitName = "AngularAcceleration"; }
        else if(unit.isCompatible(Unit.valueOf("rad/s"))) { unitName = "AngularVelocity"; }
        else if(unit.isCompatible(SI.SQUARE_METRE)) { unitName = "Area"; }
        else if(unit.isCompatible(SI.KATAL)) { unitName = "CatalyticActivity"; }
        else if(unit.isCompatible(SI.BIT)) { unitName = "DataAmount"; }
        else if(unit.isCompatible(Unit.valueOf("bit/s"))) { unitName = "DataRate"; }
        else if(unit.isCompatible(Unit.ONE)) { unitName = "Dimensionless"; }
        else if(unit.isCompatible(SI.SECOND)) { unitName = "Duration"; }
        else if(unit.isCompatible(Unit.valueOf("Pa*s"))) { unitName = "DynamicViscosity"; }
        else if(unit.isCompatible(SI.FARAD)) { unitName = "ElectricCapacitance"; }
        else if(unit.isCompatible(SI.COULOMB)) { unitName = "ElectricCharge"; }
        else if(unit.isCompatible(SI.AMPERE)) { unitName = "ElectricCurrent"; }
        else if(unit.isCompatible(SI.HENRY)) { unitName = "ElectricInductance"; }
        else if(unit.isCompatible(SI.VOLT)) { unitName = "ElectricPotential"; }
        else if(unit.isCompatible(SI.OHM)) { unitName = "ElectricResistance"; }
        else if(unit.isCompatible(SI.JOULE)) { unitName = "Energy"; }
        else if(unit.isCompatible(SI.NEWTON)) { unitName = "Force"; }
        else if(unit.isCompatible(SI.HERTZ)) { unitName = "Frequency"; }
        else if(unit.isCompatible(SI.LUX)) { unitName = "Illuminance"; }
        else if(unit.isCompatible(SI.METER)) { unitName = "Length"; }
        // else if(unit.isCompatible(SI.LUMEN)) { unitName = "LuminousFlux"; }
        // else if(unit.isCompatible(SI.CANDELA)) { unitName = "LuminousIntensity"; }
        else if(unit.isCompatible(SI.WEBER)) { unitName = "MagneticFlux"; }
        else if(unit.isCompatible(SI.TESLA)) { unitName = "MagneticFluxDensity"; }
        else if(unit.isCompatible(SI.GRAM)) { unitName = "Mass"; }
        else if(unit.isCompatible(Unit.valueOf("kg/s"))) { unitName = "MassFlowRate"; }
        else if(unit.isCompatible(Currency.EUR)) { unitName = "Money"; }
        else if(unit.isCompatible(Currency.USD)) { unitName = "Money"; }
        else if(unit.isCompatible(Currency.JPY)) { unitName = "Money"; }
        else if(unit.isCompatible(SI.WATT)) { unitName = "Power"; }
        else if(unit.isCompatible(SI.PASCAL)) { unitName = "Pressure"; }
        else if(unit.isCompatible(SI.GRAY)) { unitName = "RadiationDoseAbsorbed"; }
        else if(unit.isCompatible(SI.SIEVERT)) { unitName = "RadiationDoseEffective"; }
        else if(unit.isCompatible(SI.KATAL)) { unitName = "RadioactiveActivity"; }
        else if(unit.isCompatible(SI.STERADIAN)) { unitName = "SolidAngle"; }
        else if(unit.isCompatible(SI.CELSIUS)) { unitName = "Temperature"; }
        else if(unit.isCompatible(NonSI.FAHRENHEIT)) { unitName = "Temperature"; }
        else if(unit.isCompatible(Unit.valueOf("N*m"))) { unitName = "Torque"; }
        else if(unit.isCompatible(SI.METRES_PER_SECOND)) { unitName = "Velocity"; }
        else if(unit.isCompatible(SI.CUBIC_METRE)) { unitName = "Volume"; }
        else if(unit.isCompatible(Unit.valueOf("kg/m*m*m"))) { unitName = "VolumetricDensity"; }
        else if(unit.isCompatible(Unit.valueOf("m*m*m/s"))) { unitName = "VolumetricFlowRate"; }

        return unitName;
    }

    public static boolean isSupported (String unit) {
        boolean result = false;
        switch(unit) {
            case "Acceleration": result = true;
            case "Duration": result = true;
        }
        return result;
    }
}
