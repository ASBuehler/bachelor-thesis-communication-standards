package sqtest;

// A simple POJO for holding a series of numerical data points.
public class DataSeries {

    private String sensorId;
    private long startTimestamp;
    private String unit;
    private double[] values; // The core data: a large array of numerical values

    // Default constructor (required by many frameworks)
    public DataSeries() {
    }

    // Constructor for easy object creation
    public DataSeries(String sensorId, long startTimestamp, String unit, double[] values) {
        this.sensorId = sensorId;
        this.startTimestamp = startTimestamp;
        this.unit = unit;
        this.values = values;
    }

    // --- Public Getters and Setters for all private fields ---

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double[] getValues() {
        return values;
    }

    public void setValues(double[] values) {
        this.values = values;
    }
}