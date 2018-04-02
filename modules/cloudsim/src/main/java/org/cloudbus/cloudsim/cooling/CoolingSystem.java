package org.cloudbus.cloudsim.cooling;

import org.cloudbus.cloudsim.power.PowerHost;

import java.io.PipedOutputStream;
import java.util.List;

public abstract class CoolingSystem {

    protected double k;
    protected double[][] alpha;
    protected double[] sumAlpha;
    protected double[] tOut;
    protected double[] tIn;
    protected double maxTin = 0;
    protected double airFlow = 353;

    //TODO replace public with private
    public static final double T_SUPPLY = 24.0;

    public static final double A = 0.0068;
    public static final double B = 0.0008;
    public static final double C = 0.458;
    public static final double AIR_DENSITY = 1.2;
    public static final double AIR_SPECIFIC_HEAT = 1.005;

    public CoolingSystem() {
    }

    public abstract void initializeAlpha();

    public abstract void initializeTemperatureMatrixes(List<PowerHost> powerHostList);

    public abstract double[] computeToutTemperatures(List<PowerHost> powerHostList);

    public abstract void computeTinForServers(List<PowerHost> powerHostList);

}
