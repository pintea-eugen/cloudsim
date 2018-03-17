package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.util.constants.ThermalConstants;

import java.util.ArrayList;

public class CoolingSystem {

    public ArrayList<PowerHost> powerHosts;
    public static final double K = ThermalConstants.K;
    public static int NUM_RACKS = 5;
    public static final int NUM_SERVERS_IN_RACK = 10;
    public static int NUM_SERVERS = NUM_RACKS * NUM_SERVERS_IN_RACK;
    public static final int Pmax = 1000;
    public static final int Pidle = 100;
    public static final double T_SUP_REF = ThermalConstants.T_SUP_REF;
    public static final double T_MAX_ACCEPTABLE = ThermalConstants.T_MAX_ACCEPTABLE;
    public static final double C1 = ThermalConstants.C1;
    public static final double C2 = ThermalConstants.C2;
    public static final double C3 = ThermalConstants.C3;
    public static final int SIZE_AFFECTED = 10;
    public static double WORKLOAD;
    public static double BARRIER;
    public static final double GRANULARITY_FOR_SUPPLY_TEMPERATURE = ThermalConstants.GRANULARITY_FOR_SUPPLY_TEMPERATURE;
    public static double[] sumalfa = new double[NUM_SERVERS_IN_RACK];
    public static double [][] alfa= new double[2*NUM_SERVERS_IN_RACK][NUM_SERVERS_IN_RACK];

    public static double[][] getAlfa() {
        return alfa;
    }

        public void setZeros()
    {
        int i,j;
        for(i=0;i<2*NUM_SERVERS_IN_RACK;i++)
            for(j=0;j<NUM_SERVERS_IN_RACK;j++)
            {
                alfa[i][j]=0.0;
            }
    }

    public void setAlfaMatrix()
    {
        int i, j;
        setZeros();
        //these factors depend on position,it means the first position in vector means how much
        //current server influences the server with one level upper , then the server 2 levels upper and so on...
        double [] influenceCurrentRack = {0.02,0.025,0.025,0.01, 0.007, 0.005, 0.003, 0.001, 0.0005, 0.0001};
        double [] influenceNeighbourRack = {0.0002,0.00025,0.00025,0.00001,0.00007,0.00005,0.00003,0.00001,0.000005,0.000001};
        double [] recirculationFactors = {0.14, 0.13, 0.13, 0.12, 0.11, 0.09, 0.07};
        int SIZE_RECIRCULATION_AFFECTED = 7;
        double influenceToTheLowerServerThroughRadiation = 0.05;
        double influenceToTheUpperServerThroughRadiation = 0.04;

        for(i=0;i<NUM_SERVERS_IN_RACK;i++)
        {
            if(i!=0)
            {
                alfa[i][i-1] = influenceToTheLowerServerThroughRadiation;
            }
            if(i<NUM_SERVERS_IN_RACK-1)
            {
                alfa[i][i+1] = influenceToTheUpperServerThroughRadiation;
            }
            //setam valorile pentru rack-ul curent
            for(j=0;j<SIZE_AFFECTED;j++)
            {
                if(i+j+1 < NUM_SERVERS_IN_RACK)
                {
                    alfa[i][i+j+1]+= influenceCurrentRack[j];
                }
                else
                {
                    break;
                }
            }
        }
        int whereToStart = 0;
        int serverInfluenced ;

        for(int curServ=NUM_SERVERS_IN_RACK-1;curServ>NUM_SERVERS_IN_RACK-SIZE_RECIRCULATION_AFFECTED-1;curServ--)
        {
            serverInfluenced = NUM_SERVERS_IN_RACK-1;
            for(int z = whereToStart;z<SIZE_RECIRCULATION_AFFECTED;z++)
            {
                alfa[curServ][serverInfluenced] += recirculationFactors[z];
                serverInfluenced--;
            }
            whereToStart++;
        }
        //Pentru rack-ul vecin
        for(i=NUM_SERVERS_IN_RACK;i<2 * NUM_SERVERS_IN_RACK;i++)
        {
            for(j=0;j<SIZE_AFFECTED;j++)
            {
                if(i+j+1 < 2 * NUM_SERVERS_IN_RACK)
                {
                    alfa[i][i-NUM_SERVERS_IN_RACK+j+1]+= influenceNeighbourRack[j];
                }
                else
                {
                    break;
                }
            }
        }
    }

    public void setSumAlfaMatrix()
    {
        for(int srvNum=0;srvNum<NUM_SERVERS_IN_RACK;srvNum++)
        {
            sumalfa[srvNum] = calculateSumalfaForServer(srvNum);
        }
    }


    public double getCOP(double tSup)
    {
        return C1 * tSup * tSup +C2 * tSup +C3;
    }

    public double getPowerCooling(double powerIT,double tSup) {
        double COP,coolingPower;
        COP = getCOP(tSup);
        coolingPower = powerIT/COP;
        return coolingPower;
    }

    private double [] calculateToutTemperatures(double tSup)
    {
        double powerConsumed ,su;
        double tOut[] = new double[NUM_SERVERS];

        for(int j=0;j<NUM_SERVERS;j++)
        {
            su = powerHosts.get(j).getPower();
            if(su!=0.0)
            {
                powerConsumed = (Pidle + su*(Pmax - Pidle));
            }
            else
            {
                powerConsumed = 0.0;
            }
            tOut[j] = tSup + powerConsumed/((1 - sumalfa[j%NUM_SERVERS_IN_RACK])*K);
        }
        return tOut;
    }

    private double calculateTinForServer(double tSup,int serverNumber,double[] tout,int rackNum)
    {
        double sumAlfaTout=0.0,tin;
        int srv,numServersInRack = NUM_SERVERS_IN_RACK;
        double [][] alfa = getAlfa();
        //Rack-ul curent
        for(srv = 0;srv<numServersInRack;srv++)
        {
            sumAlfaTout+=alfa[srv][serverNumber%numServersInRack]*tout[rackNum*numServersInRack+srv];//rackForServer*numServersInRack + srv
        }
        //Rack-ul vecin
        //impar pentru ca incepe de la 0
        if(rackNum % 2 == 0)
        {
            for(srv = 0;srv<numServersInRack;srv++)
            {
                sumAlfaTout+=alfa[srv + numServersInRack][serverNumber%numServersInRack]*tout[(rackNum+1)*numServersInRack+srv];//rackForServer*numServersInRack + srv
            }
        }
        else
        {
            for(srv = 0;srv<numServersInRack;srv++)
            {
                sumAlfaTout+=alfa[srv + numServersInRack][serverNumber%numServersInRack]*tout[(rackNum-1)*numServersInRack+srv];//rackForServer*numServersInRack + srv
            }
        }
        //check whether the server is on or off
        if(tout[serverNumber] == tSup/*powerHosts.get(serverNumber).calculateUtilization()==0*/)
        {
            tin = tSup;//serverul e oprit deci nu ne intereseaza cat are Tin
        }
        else
        {
            tin = sumAlfaTout + tSup*(1-sumalfa[serverNumber%numServersInRack]);
        }
        if(tin<0){

        }
        return tin;
    }

    public double calculateSupplyTemperature()
    {
        double tIn[] = new double[NUM_SERVERS];
        double tOut[] = new double[NUM_SERVERS];
        double maxTin = 0.0;
        boolean foundTSupply = false,goingAscending = false,doNotGoAscending = false;

        double supplyTemperatureForDC = ThermalConstants.T_SUP_REF;

        tOut = calculateToutTemperatures(supplyTemperatureForDC);
        for(int i=0;i<NUM_SERVERS;i++)
        {
            tIn[i] = calculateTinForServer(supplyTemperatureForDC,i, tOut,i/NUM_SERVERS_IN_RACK);

            if(tIn[i] > maxTin)
            {
                maxTin = tIn[i];
                if(maxTin > T_MAX_ACCEPTABLE)
                {
                    goingAscending = false;
                    doNotGoAscending = true;
                    break;
                }
            }

        }

        if(doNotGoAscending == false)
        {
            supplyTemperatureForDC+=GRANULARITY_FOR_SUPPLY_TEMPERATURE;
            goingAscending = true;
        }

        if(goingAscending == true)
        {

            do{
                maxTin = 0.0;
                for(int i=0;i<NUM_SERVERS;i++)
                {

                    tIn[i] = calculateTinForServer(supplyTemperatureForDC,i%NUM_SERVERS_IN_RACK, tOut,i/NUM_SERVERS_IN_RACK);

                    if(tIn[i] > maxTin)
                    {
                        maxTin = tIn[i];
                        if(maxTin > T_MAX_ACCEPTABLE)
                        {
                            foundTSupply = true;
                            supplyTemperatureForDC-=GRANULARITY_FOR_SUPPLY_TEMPERATURE;
                            break;
                        }
                    }
                }
                if(foundTSupply == false)
                {
                    supplyTemperatureForDC+=GRANULARITY_FOR_SUPPLY_TEMPERATURE;
                    tOut = calculateToutTemperatures(supplyTemperatureForDC);
                }
                //System.out.println("M-am blocat in calc Tsup");
                //	System.out.println(supplyTemperatureForDC);
                if(supplyTemperatureForDC > 29.5)
                {
                    supplyTemperatureForDC = 29.5;
                    foundTSupply = true;
                }
            }while(foundTSupply == false);
        }
        else if(goingAscending == false)
        {
            do{
                maxTin = 0.0;
                for(int i=0;i<NUM_SERVERS;i++)
                {
                    tIn[i] = calculateTinForServer(supplyTemperatureForDC,i, tOut,i/NUM_SERVERS_IN_RACK);

                    if(tIn[i] > maxTin)
                    {
                        maxTin = tIn[i];
                    }
                }
                if(maxTin < T_MAX_ACCEPTABLE)
                {
                    foundTSupply = true;
                }
                else
                {
                    supplyTemperatureForDC-=GRANULARITY_FOR_SUPPLY_TEMPERATURE;
                    tOut = calculateToutTemperatures(supplyTemperatureForDC);
                }
            }while(foundTSupply == false);
        }
        return supplyTemperatureForDC;
    }

    public double getPowerIT()
    {
        double power,serverUtiliz;
        power=0;
        int i;
        for(i=0;i<NUM_SERVERS;i++)
        {
            serverUtiliz= powerHosts.get(i).getPower();
        }
        return power;
    }

    public int getNumberOfTurnedOnServers()
    {
        int nrTurnedOnServers = 0;
        for(int i = 0; i< powerHosts.size(); i++)
        {
            if(powerHosts.get(i).getUtilizationMips() > 0.0) // maybe a delta should be used
            {
                nrTurnedOnServers++;
            }
        }
        return nrTurnedOnServers;
    }

    private double calculateSumalfaForServer(int servNumber)
    {
        double sumalfa = 0;
        int numServer;
        for(numServer = 0;numServer<2*NUM_SERVERS_IN_RACK;numServer++)
        {
            sumalfa+=alfa[numServer][servNumber];
        }
        return sumalfa;
    }

    public double getTotalPower()
    {
        double powerIT = 0.0, powerCooling = 0.0, tSup = 0.0;
        powerIT = getPowerIT();
        tSup = calculateSupplyTemperature();
        powerCooling = getPowerCooling(powerIT, tSup);
        return powerCooling + powerIT;
    }

    public double getCoolingPower()
    {
        double powerIT = getPowerIT();
        double tSup = calculateSupplyTemperature();
        return getPowerCooling(powerIT, tSup);
    }
}
