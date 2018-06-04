package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.List;

public class PowerVmSelectionPolicyMaximumUtilization extends PowerVmSelectionPolicy{
    @Override
    public Vm getVmToMigrate(PowerHost host) {
        List<PowerVm> migratableVms = getMigratableVms(host);
        if (migratableVms.isEmpty()) {
            return null;
        }
        Vm vmToMigrate = null;
        double maxMetric = Double.MIN_VALUE;
        for (Vm vm : migratableVms) {
            if (vm.isInMigration()) {
                continue;
            }
            double metric = vm.getTotalUtilizationOfCpuMips(CloudSim.clock()) / vm.getMips();
            if (metric > maxMetric) {
                maxMetric = metric;
                vmToMigrate = vm;
            }
        }
        return vmToMigrate;
    }
}
