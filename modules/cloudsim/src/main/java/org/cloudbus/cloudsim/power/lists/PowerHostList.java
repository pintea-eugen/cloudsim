package org.cloudbus.cloudsim.power.lists;

import org.cloudbus.cloudsim.Host;

import java.util.List;

public class PowerHostList {

    public static <T extends Host> void sortHostByCpuUtilization(List<T> hostList) {
        hostList.sort((a, b) -> {
            Double aUtilization = a.getAvailableMips();
            Double bUtilization = b.getAvailableMips();
            return bUtilization.compareTo(aUtilization);
        });
    }

}
