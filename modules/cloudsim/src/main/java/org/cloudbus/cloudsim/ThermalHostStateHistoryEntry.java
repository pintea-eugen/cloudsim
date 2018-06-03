package org.cloudbus.cloudsim;

public class ThermalHostStateHistoryEntry {

    /** Temperature at the exhaust of the server*/
    double outletTemperature;

    /** The time. */
    private double time;

    /** Indicates if the host was active in the indicated time.
     * @see #time
     */
    private boolean isActive;
    /**
     * Instantiates a new host state history entry.
     *
     * @param time          the time
     * @param outletTemperature temperature from the exhaust of the server
     * @param isActive      the is active
     */
    public ThermalHostStateHistoryEntry(double time, double outletTemperature, boolean isActive) {
        setTime(time);
        setOutletTemperature(outletTemperature);
        setActive(isActive);
    }

    public double getOutletTemperature() {
        return outletTemperature;
    }

    public void setOutletTemperature(double outletTemperature) {
        this.outletTemperature = outletTemperature;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
