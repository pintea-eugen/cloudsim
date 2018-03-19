package org.cloudbus.cloudsim.cooling;

import org.cloudbus.cloudsim.power.PowerHost;

import java.util.List;

public class CoolingSystem {

    //f param
    private static double airFlow = 353;
    //q param
    private static double airDensity = 1.2;
    //Cp param
    private static double airSpecificHeat = 1.005;
    //Specific thermal constant
    private static double K = airFlow * airDensity * airSpecificHeat;
    ;

    //TODO replace public with private
    public static final double tSupply = 24.0;

    public static final double a = 0.0068;
    public static final double b = 0.0008;
    public static final double c = 0.458;

    //TODO use the COP in CoolingPowerHost, instead of computing the value in
    //getCoolingPower method
    public double COP;

    private static int NUM_RACKS = 5;
    private static int NUM_SERVERS_IN_RACK = 10;
    private static int NUM_SERVERS = NUM_RACKS * NUM_SERVERS_IN_RACK;

    private static double[] tIn = new double[NUM_SERVERS_IN_RACK * NUM_RACKS];
    private static double[] tOut = new double[NUM_SERVERS_IN_RACK * NUM_RACKS];


    public static double[] sumalfa = new double[NUM_SERVERS_IN_RACK];
    public static double[][] alfa = new double[NUM_SERVERS_IN_RACK][NUM_SERVERS_IN_RACK];
    private static int SIZE_AFFECTED = 10;


    public static double[][] getAlfa() {
        return alfa;
    }

    CoolingSystem(String topology, int numServers, double tSupply) {
        //initialize K
        K = airFlow * airDensity * airSpecificHeat;
        //initialize alphamatrix with values based on the topology and numservers

        //initialize tin, tout with dimension
        tIn = new double[numServers];
        tOut = new double[numServers];
        //initialize COP
        //this.tSupply = tSupply;
        COP = a * tSupply * tSupply + b * tSupply + c;
    }

    public static double computeToutValuesForServer(int position, int powerConsumed) {
        tOut[position] = tSupply + powerConsumed / ((1 - sumalfa[position % NUM_SERVERS_IN_RACK]) * K);
        return tOut[position];
    }

    static int i = 0;
    static double maxTout = 0.0;

    public static double[] computeToutTemperatures(List<PowerHost> powerHostList) {
        double powerConsumed;
//        i++;
//        System.out.println("Compute Tout called "+i);
        for (int j = 0; j < NUM_SERVERS; j++) {
            powerConsumed = powerHostList.get(j).getElectricalPower(powerHostList.get(j).getUtilizationOfCpu());
//            System.out.println("Electrical Power "+powerConsumed);
//            System.out.println("Total Power "+powerHostList.get(j).getPower());
            tOut[j] = tSupply + powerConsumed / ((1 - sumalfa[j % NUM_SERVERS_IN_RACK]) * K);
            if (tOut[j] > maxTout) {
                maxTout = tOut[j];
//            System.out.println("Max Tout = "+maxTout);
            }
        }
        return tOut;
    }

    static double maxTin = 0;

    public static void computeTinForServers(List<PowerHost> powerHostList) {
        double sumAlfaTout = 0.0, tin;
        int rackNum, serverNumber, srv, numServersInRack = NUM_SERVERS_IN_RACK;
        double[][] alfa = getAlfa();

        for (PowerHost powerHost : powerHostList) {
            serverNumber = powerHost.getId();
            rackNum = (serverNumber - 1) / NUM_SERVERS_IN_RACK;
            sumAlfaTout = 0.0;
            for (srv = 0; srv < numServersInRack; srv++) {
                //review this alfa[srv][..], nu trebuia sa fie alfa[serverNumber]?
                sumAlfaTout += alfa[srv][serverNumber % numServersInRack] * tOut[rackNum * numServersInRack + srv];
            }
            //check whether the server is on or off
            if (tOut[serverNumber] == tSupply) {
                tIn[serverNumber] = tSupply;
            } else {
                tIn[serverNumber] = sumAlfaTout + tSupply * (1 - sumalfa[serverNumber % numServersInRack]);
            }
            if (tIn[serverNumber] > maxTin) {
                maxTin = tIn[serverNumber];
//                System.out.println("MAX Tin="+maxTin);
//                System.out.println("SumAlfaTout="+sumAlfaTout);
//                System.out.println("+ .. ="+(1 - sumalfa[serverNumber % numServersInRack]));
//                System.out.println("+ ... ="+tSupply * (1 - sumalfa[serverNumber % numServersInRack]));

            }
        }
    }

    public static void setZeros() {
        int i, j;
        for (i = 0; i < NUM_SERVERS_IN_RACK; i++)
            for (j = 0; j < NUM_SERVERS_IN_RACK; j++) {
                alfa[i][j] = 0.0;
            }
    }

    //NOTE: this matrix can be determined once through CFD and it can be used from then in computations
    public static void setAlfaMatrix() {
        int i, j;
        setZeros();
        //these factors depend on position,it means the first position in vector means how much
        //current server influences the server with one level upper , then the server 2 levels upper and so on...
        double[] influenceCurrentRack = {0.02, 0.025, 0.025, 0.01, 0.007, 0.005, 0.003, 0.001, 0.0005, 0.0001};
        double[] influenceNeighbourRack = {0.0002, 0.00025, 0.00025, 0.00001, 0.00007, 0.00005, 0.00003, 0.00001, 0.000005, 0.000001};
        double[] recirculationFactors = {0.14, 0.13, 0.13, 0.12, 0.11, 0.09, 0.07};
        int SIZE_RECIRCULATION_AFFECTED = 7;
        double influenceToTheLowerServerThroughRadiation = 0.05;
        double influenceToTheUpperServerThroughRadiation = 0.04;

        for (i = 0; i < NUM_SERVERS_IN_RACK; i++) {
            if (i != 0) {
                alfa[i][i - 1] = influenceToTheLowerServerThroughRadiation;
            }
            if (i < NUM_SERVERS_IN_RACK - 1) {
                alfa[i][i + 1] = influenceToTheUpperServerThroughRadiation;
            }
            //setam valorile pentru rack-ul curent
            for (j = 0; j < SIZE_AFFECTED; j++) {
                if (i + j + 1 < NUM_SERVERS_IN_RACK) {
                    alfa[i][i + j + 1] += influenceCurrentRack[j];
                } else {
                    break;
                }
            }
        }
        int whereToStart = 0;
        int serverInfluenced;

        for (int curServ = NUM_SERVERS_IN_RACK - 1; curServ > NUM_SERVERS_IN_RACK - SIZE_RECIRCULATION_AFFECTED - 1; curServ--) {
            serverInfluenced = NUM_SERVERS_IN_RACK - 1;
            for (int z = whereToStart; z < SIZE_RECIRCULATION_AFFECTED; z++) {
                alfa[curServ][serverInfluenced] += recirculationFactors[z];
                serverInfluenced--;
            }
            whereToStart++;
        }

        for (i = 0; i < NUM_SERVERS_IN_RACK; i++) {
            for (j = 0; j < SIZE_AFFECTED; j++) {
                if (i + j + 1 < NUM_SERVERS_IN_RACK) {
                    alfa[i][i + j + 1] += influenceNeighbourRack[j];
                } else {
                    break;
                }
            }
        }
    }

    public static void setSumAlfaMatrix() {
        for (int srvNum = 0; srvNum < NUM_SERVERS_IN_RACK; srvNum++) {
            sumalfa[srvNum] = calculateSumalfaForServer(srvNum);
        }
    }

    private static double calculateSumalfaForServer(int servNumber) {
        double sumalfa = 0;
        int numServer;
        for (numServer = 0; numServer < NUM_SERVERS_IN_RACK; numServer++) {
            sumalfa += alfa[numServer][servNumber];
        }
        return sumalfa;
    }

    /**
     * Initializes the CoolingSystem, especially the alpha matrix
     */
    public static void init() {
        setZeros();
        setAlfaMatrix();
        setSumAlfaMatrix();
        System.out.println();
    }

    public static void main(String[] args) {
        setZeros();
        setAlfaMatrix();
        setSumAlfaMatrix();
        for (int i = 0; i < NUM_SERVERS_IN_RACK * NUM_RACKS; i++) {
            computeToutValuesForServer(i, 100);
        }

//		for(int i = 0; i < NUM_SERVERS_IN_RACK * NUM_RACKS; i++){
//			computeTinForServer(18.0, i, computeToutValuesForServer(i, 100), i/NUM_SERVERS_IN_RACK);
//		}

        System.out.println();
    }
}
