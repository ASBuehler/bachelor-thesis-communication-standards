package sqtest;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DataSeries")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlDataSeries {

    private String sensorId;
    private long startTimestamp;
    private String unit;
    private double[] values;

    public XmlDataSeries() {}

    public XmlDataSeries(DataSeries original) {
        this.sensorId = original.getSensorId();
        this.startTimestamp = original.getStartTimestamp();
        this.unit = original.getUnit();
        this.values = original.getValues();
    }

    public DataSeries toDataSeries() {
        return new DataSeries(sensorId, startTimestamp, unit, values);
    }
}