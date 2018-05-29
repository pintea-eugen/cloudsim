package org.cloudbus.cloudsim.cooling;

import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.cooling.heatreuse.HeatExchangeSystem;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModelLinear;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.cloudbus.cloudsim.cooling.constants.ThermoDynamicConstants.AIR_DENSITY;
import static org.cloudbus.cloudsim.cooling.constants.ThermoDynamicConstants.AIR_SPECIFIC_HEAT;

public class BlueSimCoolingSystem extends PowerCoolingSystem {

  //  private static final int NUM_SERVERS_INFLUENCED_THERMALLY = 20;

    public BlueSimCoolingSystem(List<PowerHost> powerHostList) {
        airFlow = 145.93; // 145.93 m3/hour, 40.54 l/hour,5l/sec = 18 m3/hour
        k = airFlow * AIR_DENSITY * AIR_SPECIFIC_HEAT;
        NUM_SERVERS_INFLUENCED_THERMALLY = 20;
        sumAlpha = new double[NUM_SERVERS_INFLUENCED_THERMALLY];
        initializeAlpha();
        initializeTemperatureMatrices(powerHostList);
        computeSumAlpha();
    }

    public BlueSimCoolingSystem(List<PowerHost> powerHostList, HeatExchangeSystem heatExchangeSystem) {
        super(heatExchangeSystem);
        airFlow = 145.93; // 145.93 m3/hour, 40.54 l/hour,5l/sec = 18 m3/hour
        k = airFlow * AIR_DENSITY * AIR_SPECIFIC_HEAT;
        NUM_SERVERS_INFLUENCED_THERMALLY = 20;
        sumAlpha = new double[NUM_SERVERS_INFLUENCED_THERMALLY];
        initializeAlpha();
        initializeTemperatureMatrices(powerHostList);
        computeSumAlpha();
    }

    @Override
    public void initializeAlpha() {
        alpha = new double[][]{{0.000240, 0.000266, 0.000261, 0.000228, 0.000178, 0.000114, 0.000119, 0.0001250, 0.001290, 0.001430, 0.000240, 0.000220, 0.000220, 0.000220, 0.000240, 0.000200, 0.000200, 0.000240, 0.000280, 0.00027},
                {0.000198, 0.000049, 0.000193, 0.000178, 0.000154, 0.000141, 0.000147, 0.000153, 0.000154, 0.000162, 0.000025, 0.000025, 0.000026, 0.000027, 0.000030, 0.000025, 0.000025, 0.000030, 0.000036, 0.000033},
                {0.000162, 0.000167, 0.000045, 0.000166, 0.000142, 0.000129, 0.000132, 0.000137, 0.000139, 0.000147, 0.000026, 0.000026, 0.000028, 0.000029, 0.000031, 0.000026, 0.000026, 0.000031, 0.000036, 0.000034},
                {0.000150, 0.000148, 0.000159, 0.000042, 0.000147, 0.000115, 0.000117, 0.000119, 0.000122, 0.000132, 0.000028, 0.000028, 0.000029, 0.000029, 0.000031, 0.000025, 0.000025, 0.000029, 0.000034, 0.000033},
                {0.000131, 0.000131, 0.000129, 0.000137, 0.000047, 0.000113, 0.000114, 0.000115, 0.000120, 0.000138, 0.000027, 0.000027, 0.000028, 0.000029, 0.000031, 0.000025, 0.000025, 0.000029, 0.000034, 0.000034},
                {0.000066, 0.000060, 0.000068, 0.000085, 0.000115, 0.000318, 0.000438, 0.000489, 0.000491, 0.000503, 0.000010, 0.000011, 0.000012, 0.000018, 0.000031, 0.000047, 0.000050, 0.000065, 0.000079, 0.000072},
                {0.000059, 0.000060, 0.000070, 0.000094, 0.000134, 0.000493, 0.000399, 0.000579, 0.000560, 0.000542, 0.000012, 0.000013, 0.000014, 0.000022, 0.000039, 0.000061, 0.000064, 0.000083, 0.000099, 0.000088},
                {0.000061, 0.000064, 0.000075, 0.000102, 0.000149, 0.000514, 0.000575, 0.000497, 0.000619, 0.000594, 0.000015, 0.000016, 0.000018, 0.000027, 0.000046, 0.000075, 0.000078, 0.000099, 0.000118, 0.000105},
                {0.000069, 0.000072, 0.000081, 0.000108, 0.000153, 0.000450, 0.000499, 0.000584, 0.000489, 0.000615, 0.000018, 0.000019, 0.000022, 0.000030, 0.000048, 0.000075, 0.000077, 0.000098, 0.000118, 0.000107},
                {0.000083, 0.000085, 0.000090, 0.000108, 0.000141, 0.000298, 0.000333, 0.000399, 0.000468, 0.000437, 0.000020, 0.000021, 0.000024, 0.000030, 0.000042, 0.000055, 0.000056, 0.000071, 0.000088, 0.000082},
                {0.000027, 0.000024, 0.000023, 0.000022, 0.000022, 0.000018, 0.000015, 0.000016, 0.000019, 0.000019, 0.000356, 0.000329, 0.000322, 0.000267, 0.000197, 0.000108, 0.000112, 0.000117, 0.000121, 0.000135},
                {0.000020, 0.000020, 0.000020, 0.000020, 0.000020, 0.000014, 0.000011, 0.000011, 0.000014, 0.000013, 0.000185, 0.000116, 0.000206, 0.000183, 0.000151, 0.000123, 0.000127, 0.000131, 0.000131, 0.000140},
                {0.000028, 0.000028, 0.000029, 0.000029, 0.000030, 0.000024, 0.000022, 0.000025, 0.000029, 0.000029, 0.000154, 0.000166, 0.000107, 0.000169, 0.000140, 0.000124, 0.000127, 0.000130, 0.000131, 0.000141},
                {0.000029, 0.000029, 0.000029, 0.000029, 0.000031, 0.000026, 0.000024, 0.000026, 0.000031, 0.000031, 0.000143, 0.000144, 0.000161, 0.000098, 0.000151, 0.000117, 0.000117, 0.000117, 0.000120, 0.000131},
                {0.000027, 0.000027, 0.000028, 0.000028, 0.000030, 0.000026, 0.000024, 0.000026, 0.000031, 0.000031, 0.000129, 0.000130, 0.000130, 0.000141, 0.000105, 0.000117, 0.000117, 0.000115, 0.000120, 0.000138},
                {0.000011, 0.000012, 0.000013, 0.000017, 0.000028, 0.000052, 0.000047, 0.000055, 0.000066, 0.000064, 0.000075, 0.000071, 0.000077, 0.000090, 0.000117, 0.000370, 0.000422, 0.000466, 0.000470, 0.000493},
                {0.000010, 0.000011, 0.000012, 0.000018, 0.000032, 0.000063, 0.000057, 0.000067, 0.000079, 0.000075, 0.000056, 0.000058, 0.000067, 0.000091, 0.000130, 0.000480, 0.000432, 0.000553, 0.000537, 0.000532},
                {0.000015, 0.000015, 0.000017, 0.000024, 0.000041, 0.000081, 0.000073, 0.000086, 0.000100, 0.000095, 0.000057, 0.000060, 0.000071, 0.000099, 0.000146, 0.000502, 0.000561, 0.000533, 0.000605, 0.000593},
                {0.000019, 0.000019, 0.000022, 0.000029, 0.000046, 0.000086, 0.000078, 0.000091, 0.000108, 0.000104, 0.000069, 0.000072, 0.000080, 0.000107, 0.000153, 0.000443, 0.000492, 0.000577, 0.000534, 0.000619},
                {0.000025, 0.000024, 0.000026, 0.000032, 0.000045, 0.000073, 0.000067, 0.000078, 0.000093, 0.000093, 0.000091, 0.000093, 0.000096, 0.000114, 0.000148, 0.000304, 0.000338, 0.000401, 0.000469, 0.000498}};
    }


    public static void main(String[] args) {
        int i,j;
        double MIPS = 5000;
        double MAX_POWER = 300;
        double STATIC_POWER_PERCENT = 0.3;
        double TIME = 10;
        List<Pe> peList = new ArrayList<Pe>();
        peList.add(new Pe(0, new PeProvisionerSimple(MIPS)));
        //peList.add(new Pe(1, new PeProvisionerSimple(MIPS)));
        List<PowerHost> powerHostList = new ArrayList<>(20);
        for (i = 0 ;i < 20; i++) {
            PowerHost powerHost = new PowerHost(i, null, null, 0, peList, null, new PowerModelLinear(MAX_POWER, STATIC_POWER_PERCENT));
            double randomUtilization = (double) new Random().nextInt(5000);
            /*int randOff = new Random().nextInt(10);
            if(randOff % 2 == 1)
                randomUtilization = 0;*/
            powerHost.setUtilizationMips(randomUtilization);
            powerHostList.add(powerHost);
        }

        BlueSimCoolingSystem blueSimCoolingSystem = new BlueSimCoolingSystem(powerHostList);
        blueSimCoolingSystem.computeToutTemperatures(powerHostList, INITIAL_SUPPLY_TEMPERATURE);

        System.out.println("Sum alfa for serveres:");
        for(i=0;i<powerHostList.size();i++) {
            System.out.println(blueSimCoolingSystem.sumAlpha[i]);
        }

        System.out.println();
        for(i=0;i<powerHostList.size();i++) {
            System.out.println(blueSimCoolingSystem.tOut[powerHostList.get(i).getId()]);
        }

        System.out.println("Maximum supply temperature: " + blueSimCoolingSystem.calculateMaximumSupplyTemperature(powerHostList));
    }
}
