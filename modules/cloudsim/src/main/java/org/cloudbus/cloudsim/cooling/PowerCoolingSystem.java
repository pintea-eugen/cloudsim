package org.cloudbus.cloudsim.cooling;

import org.cloudbus.cloudsim.cooling.heatreuse.HeatExchangeSystem;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

public abstract class PowerCoolingSystem extends CoolingSystem {

    private static final double A = 0.0068;
    private static final double B = 0.0008;
    private static final double C = 0.458;

    private static final double TEMPERATURE_INCREMENT = 0.1;

    protected int NUM_SERVERS_INFLUENCED_THERMALLY;

    public PowerCoolingSystem() {
    }

    public PowerCoolingSystem(HeatExchangeSystem heatExchangeSystem) {
        this.heatExchangeSystem = heatExchangeSystem;
    }

    public double getCop(double supplyTemperature) {
        return A * supplyTemperature * supplyTemperature + B * supplyTemperature + C;
    }

    public double getCoolingPower(double supplyTemperature, double powerIT) {
        return powerIT / getCop(supplyTemperature);
    }

    public void initializeTemperatureMatrices(List<PowerHost> powerHostList) {
        tIn = new double[powerHostList.size()];
        tOut = new double[powerHostList.size()];
    }

    public void computeSumAlpha() {
        for (int i = 0; i < NUM_SERVERS_INFLUENCED_THERMALLY; i++) {
            for (int j = 0; j < NUM_SERVERS_INFLUENCED_THERMALLY; j++) {
                sumAlpha[i] += alpha[j][i];
            }
        }
    }

    public double[] computeToutTemperatures(List<PowerHost> powerHostList, double supplyTemperature) {
        double powerConsumed;

        for (int j = 0; j < powerHostList.size(); j++) {
            powerConsumed = powerHostList.get(j).getPower(powerHostList.get(j).getUtilizationOfCpu());
            tOut[j] = supplyTemperature + powerConsumed / ((1 - sumAlpha[j % NUM_SERVERS_INFLUENCED_THERMALLY]) * k);
        }

        return tOut;
    }

    public double[] computeSumAlphaTout(List<PowerHost> powerHostList, double supplyTemperature) {
        double sumAlphaTout[] = new double[powerHostList.size()];
        computeToutTemperatures(powerHostList, supplyTemperature);

        for (PowerHost powerHost : powerHostList) {
            int serverId = powerHost.getId();

            for (int i = 0; i < NUM_SERVERS_INFLUENCED_THERMALLY; i++) {
                sumAlphaTout[serverId] += alpha[i][serverId % NUM_SERVERS_INFLUENCED_THERMALLY] *
                        tOut[((serverId / NUM_SERVERS_INFLUENCED_THERMALLY) * NUM_SERVERS_INFLUENCED_THERMALLY) + i];
            }
        }

        return sumAlphaTout;
    }

    public double computeMaxTinForServers(List<PowerHost> powerHostList, double supplyTemperature) {
        double[] tIn = new double[powerHostList.size()];
        double maxTin = Double.MIN_VALUE;

        computeToutTemperatures(powerHostList, supplyTemperature);
        double[] sumAlphaTout = computeSumAlphaTout(powerHostList, supplyTemperature);

        for (PowerHost powerHost : powerHostList) {
            int serverId = powerHost.getId();

            if (tOut[serverId] == supplyTemperature) {
                tIn[serverId] = supplyTemperature;
            } else {
                tIn[serverId] = sumAlphaTout[serverId] +
                        supplyTemperature * (1 - sumAlpha[serverId % NUM_SERVERS_INFLUENCED_THERMALLY]);
            }

            if (tIn[serverId] > maxTin) {
                maxTin = tIn[serverId];
            }
        }

        return maxTin;
    }

    public double calculateMaximumSupplyTemperature(List<PowerHost> powerHostList) {
        double supplyTemperature = INITIAL_SUPPLY_TEMPERATURE;
        double maxTin = Double.MIN_VALUE;

        while (maxTin < MAX_ACCEPTABLE_TEMPERATURE) {
            maxTin = computeMaxTinForServers(powerHostList, supplyTemperature);
            supplyTemperature += TEMPERATURE_INCREMENT;
        }

        supplyTemperature -= TEMPERATURE_INCREMENT;

        return supplyTemperature;
    }
}
