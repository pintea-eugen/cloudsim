package org.cloudbus.cloudsim.cooling;

import com.sun.istack.internal.NotNull;
import org.cloudbus.cloudsim.cooling.entities.PowerCoolingSystemType;
import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

public class PowerCoolingSystemFactory {

    public static PowerCoolingSystem createPowerCoolingSystem(@NotNull PowerCoolingSystemType powerCoolingSystemType, List<PowerHost> powerHostList) {
        if (powerCoolingSystemType.equals(PowerCoolingSystemType.BLUE_SIM)) {
            return new BlueSimCoolingSystem(powerHostList);
        } else if (powerCoolingSystemType.equals(PowerCoolingSystemType.ASU_HPCI)) {
            return new AsuHpciCoolingSystem(powerHostList);
        } else {
            throw new RuntimeException("Unsupported cooling system type " + powerCoolingSystemType.toString());
        }
    }
}
