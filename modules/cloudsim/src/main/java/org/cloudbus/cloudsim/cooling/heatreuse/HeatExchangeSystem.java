package org.cloudbus.cloudsim.cooling.heatreuse;

import static org.cloudbus.cloudsim.cooling.constants.ThermoDynamicConstants.AIR_DENSITY;
import static org.cloudbus.cloudsim.cooling.constants.ThermoDynamicConstants.AIR_SPECIFIC_HEAT;

//Rename HeatExchangeSystem
public class HeatExchangeSystem {
//Extend

    protected double copHeating;
    protected double copCooling;

    public HeatExchangeSystem() {
    }

    public HeatExchangeSystem(double copHeating, double copCooling) {
        this.copHeating = copHeating;
        this.copCooling = copCooling;
    }

    public double getCopHeating() {
        return copHeating;
    }

    public void setCopHeating(double copHeating) {
        this.copHeating = copHeating;
    }

    public double getCopCooling() {
        return copCooling;
    }

    public void setCopCooling(double copCooling) {
        this.copCooling = copCooling;
    }

    public double calculateEnergyCooling(double energyIT) {
        return energyIT / copCooling;
    }

    public double calculateRemovedHeat(double airFlow, double tIn, double tOut) {
        return airFlow * AIR_DENSITY * AIR_SPECIFIC_HEAT * (tOut - tIn);
    }

    public double calculateSuppliedHeat(double energyIT) {
        //energyIT == heatRemoved
        return ((getCopHeating() - 1) * energyIT) / getCopCooling();
    }
}
