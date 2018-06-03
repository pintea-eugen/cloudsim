/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim.power;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.cooling.CoolingSystem;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.LinkedList;
import java.util.List;

/**
 * PowerHost class enables simulation of power-aware hosts.
 *
 * <br/>If you are using any algorithms, policies or workload included in the power package please cite
 * the following paper:<br/>
 *
 * <ul>
 * <li><a href="http://dx.doi.org/10.1002/cpe.1867">Anton Beloglazov, and Rajkumar Buyya, "Optimal Online Deterministic Algorithms and Adaptive
 * Heuristics for Energy and Performance Efficient Dynamic Consolidation of Virtual Machines in
 * Cloud Data Centers", Concurrency and Computation: Practice and Experience (CCPE), Volume 24,
 * Issue 13, Pages: 1397-1420, John Wiley & Sons, Ltd, New York, USA, 2012</a>
 * </ul>
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class PowerHost extends HostDynamicWorkload {

	/** The power model used by the host. */
	private PowerModel powerModel;

	/**
	 * Exhaust temperature of the host
	 */
	private double tout;

	/**
	 * Intake temperature of the host
	 */
	private double tin;

    /** The thermal host utilization state history. */
    private final List<ThermalHostStateHistoryEntry> thermalStateHistory = new LinkedList<ThermalHostStateHistoryEntry>();
	/**
	 * Instantiates a new PowerHost.
	 *
	 * @param id the id of the host
	 * @param ramProvisioner the ram provisioner
	 * @param bwProvisioner the bw provisioner
	 * @param storage the storage capacity
	 * @param peList the host's PEs list
	 * @param vmScheduler the VM scheduler
	 */
	public PowerHost(
			int id,
			RamProvisioner ramProvisioner,
			BwProvisioner bwProvisioner,
			long storage,
			List<? extends Pe> peList,
			VmScheduler vmScheduler,
			PowerModel powerModel) {
		super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
		setPowerModel(powerModel);
	}

	/**
	 * Gets the power. For this moment only consumed by all PEs.
	 *
	 * @return the power
	 */
	public double getPower() {
//		double totalPower = getPower(getUtilizationOfCpu());// + getCoolingPower(CoolingSystem.tSupply);
//		System.out.println("The power consumed by host "+getId()+ " is "+totalPower);
//		//System.out.println("The electrical power by host "+getId()+ " is "+getPower(getUtilizationOfCpu()));
//		//System.out.println("The cooling power by host "+getId()+ " is "+ getCoolingPower(CoolingSystem.tSupply));
//		return totalPower;
		return getPower(getUtilizationOfCpu());
	}

	private double getCoolingPower(double tSupply, double electricalPower) {
		double COP = CoolingSystem.tSupply * CoolingSystem.tSupply * CoolingSystem.a + CoolingSystem.tSupply * CoolingSystem.b + CoolingSystem.c;
		return (electricalPower/COP);
	}

	/**
	 * Gets the current power consumption of the host. For this moment only consumed by all PEs.
	 *
	 * @param utilization the utilization percentage (between [0 and 1]) of a resource that
	 * is critical for power consumption
	 * @return the power consumption including the cooling power
	 */
	protected double getPower(double utilization) {
		double power = 0;
		try {
			//calculam doar la sfarsit cooling power
			power = getPowerModel().getPower(utilization);// + getCoolingPower(CoolingSystem.tSupply, getPowerModel().getPower(utilization));

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}
    public double getElectricalPower(double utilization) {
        double power = 0;
        try {
            power = getPowerModel().getPower(utilization);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        return power;
    }

	/**
	 * Gets the max power that can be consumed by the host.
	 *
	 * @return the max power
	 */
	public double getMaxPower() {
		double power = 0;
		try {
			power = getPowerModel().getPower(1);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return power;
	}

	/**
	 * Gets the energy consumption using linear interpolation of the utilization change.
	 *
	 * @param fromUtilization the initial utilization percentage
	 * @param toUtilization the final utilization percentage
	 * @param time the time
	 * @return the energy
	 */
	public double getEnergyLinearInterpolation(double fromUtilization, double toUtilization, double time) {
		if (fromUtilization == 0) {
			return 0;
		}
		double fromPower = getPower(fromUtilization);
		double toPower = getPower(toUtilization);
		return (fromPower + (toPower - fromPower) / 2) * time;
	}

	/**
	 * Sets the power model.
	 *
	 * @param powerModel the new power model
	 */
	protected void setPowerModel(PowerModel powerModel) {
		this.powerModel = powerModel;
	}

	/**
	 * Gets the power model.
	 *
	 * @return the power model
	 */
	public PowerModel getPowerModel() {
		return powerModel;
	}


	public double getTout() {
		return tout;
	}

	public void setTout(double tout) {
		this.tout = tout;
	}

	public double getTin() {
		return tin;
	}

	public void setTin(double tin) {
		this.tin = tin;
	}


	@Override
	public double updateVmsProcessing(double currentTime) {
		double smallerTime = super.updateVmsProcessing(currentTime);
		setPreviousUtilizationMips(getUtilizationMips());
		setUtilizationMips(0);
		double hostTotalRequestedMips = 0;

		for (Vm vm : getVmList()) {
			getVmScheduler().deallocatePesForVm(vm);
		}

		for (Vm vm : getVmList()) {
			getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
		}

		for (Vm vm : getVmList()) {
			double totalRequestedMips = vm.getCurrentRequestedTotalMips();
			double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);

			if (!Log.isDisabled()) {
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
								+ " (Host #" + vm.getHost().getId()
								+ ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
						CloudSim.clock(),
						totalAllocatedMips,
						totalRequestedMips,
						vm.getMips(),
						totalRequestedMips / vm.getMips() * 100);

				List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
				StringBuilder pesString = new StringBuilder();
				for (Pe pe : pes) {
					pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
							.getTotalAllocatedMipsForVm(vm)));
				}
				Log.formatLine(
						"%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
								+ getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
								+ pesString,
						CloudSim.clock());
			}

			if (getVmsMigratingIn().contains(vm)) {
				Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
						+ " is being migrated to Host #" + getId(), CloudSim.clock());
			} else {
				if (totalAllocatedMips + 0.1 < totalRequestedMips) {
					Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
							+ ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
				}

				vm.addStateHistoryEntry(
						currentTime,
						totalAllocatedMips,
						totalRequestedMips,
						(vm.isInMigration() && !getVmsMigratingIn().contains(vm)));

				if (vm.isInMigration()) {
					Log.formatLine(
							"%.2f: [Host #" + getId() + "] VM #" + vm.getId() + " is in migration",
							CloudSim.clock());
					totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
				}
			}

			setUtilizationMips(getUtilizationMips() + totalAllocatedMips);
			hostTotalRequestedMips += totalRequestedMips;
		}

		addStateHistoryEntry(
				currentTime,
				getUtilizationMips(),
				hostTotalRequestedMips,
				(getUtilizationMips() > 0));

		addThermalStateHistoryEntry(currentTime, getTout(), (getUtilizationMips() > 0));

		return smallerTime;
	}

	/**
	 * Adds a host state history entry.
	 *
	 * @param time the time
	 * @param outletTemperature the exhaust temperature from the server
	 * @param isActive the is active
	 */
	public
	void addThermalStateHistoryEntry(double time, double outletTemperature, boolean isActive) {

		ThermalHostStateHistoryEntry newState = new ThermalHostStateHistoryEntry(
				time,
                outletTemperature,
				isActive);
		if (!getThermalStateHistory().isEmpty()) {
			ThermalHostStateHistoryEntry previousState = getThermalStateHistory().get(getThermalStateHistory().size() - 1);
			if (previousState.getTime() == time) {
				getThermalStateHistory().set(getThermalStateHistory().size() - 1, newState);
				return;
			}
		}
		getThermalStateHistory().add(newState);
	}

    public List<ThermalHostStateHistoryEntry> getThermalStateHistory() {
        return thermalStateHistory;
    }
}
