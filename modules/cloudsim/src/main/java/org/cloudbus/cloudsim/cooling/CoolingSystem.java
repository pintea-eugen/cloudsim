package org.cloudbus.cloudsim.cooling;

import org.cloudbus.cloudsim.cooling.heatreuse.HeatExchangeSystem;
import org.cloudbus.cloudsim.power.PowerHost;

import java.io.PipedOutputStream;
import java.util.List;

public abstract class CoolingSystem {

    protected double k;
    protected double[][] alpha;
    protected double[] sumAlpha;
    protected double[] tOut;
    protected double[] tIn;
    protected double airFlow = 353;

    //TODO replace public with private
    public static final double INITIAL_SUPPLY_TEMPERATURE = 18.0;
    public static final double MAX_ACCEPTABLE_TEMPERATURE = 29.5;

    protected HeatExchangeSystem heatExchangeSystem;

    public CoolingSystem() {
    }

    public CoolingSystem(HeatExchangeSystem heatExchangeSystem) {
        this.heatExchangeSystem = heatExchangeSystem;
    }

    public abstract void initializeAlpha();

    public abstract void initializeTemperatureMatrices(List<PowerHost> powerHostList);

    public abstract double[] computeToutTemperatures(List<PowerHost> powerHostList, double supplyTemperature);

    public abstract double computeMaxTinForServers(List<PowerHost> powerHostList, double supplyTemperature);

    public double calculateRemovedHeat(double airFlow, double tIn, double tOut) {
        return heatExchangeSystem.calculateRemovedHeat(airFlow, tIn, tOut);
    }

    public double calculateSuppliedHeat(double powerIT) {
        return heatExchangeSystem.calculateSuppliedHeat(powerIT);
    }
}
