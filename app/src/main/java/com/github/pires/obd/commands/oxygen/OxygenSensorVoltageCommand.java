package com.github.pires.obd.commands.oxygen;

import com.github.pires.obd.commands.ObdCommand;
import com.github.pires.obd.enums.AvailableCommandNames;

/**
 * <p>ModuleVoltageCommand class.</p>
 *
 * @author Nikolay Khabarov
 * @version $Id: $Id
 */
public class OxygenSensorVoltageCommand extends ObdCommand {

    public enum OxygenBank {
        OXYGEN_BANK_1,
        OXYGEN_BANK_2
    }

    public enum OxygenSensor {
        OXYGEN_SENSOR_1,
        OXYGEN_SENSOR_2,
        OXYGEN_SENSOR_3,
        OXYGEN_SENSOR_4
    }

    // Oxygen sensor voltage
    private float voltage = 0f;
    private float percentage = 0f;

    private static String buildOBDCommand(OxygenBank bank, OxygenSensor sensor) {
        int command = 0;
        switch (bank) {
            case OXYGEN_BANK_1:
                command = 0x14;
                break;
            case OXYGEN_BANK_2:
                command = 0x18;
                break;
        }
        switch (sensor) {
            case OXYGEN_SENSOR_1:
                break;
            case OXYGEN_SENSOR_2:
                command += 1;
                break;
            case OXYGEN_SENSOR_3:
                command += 2;
                break;
            case OXYGEN_SENSOR_4:
                command += 3;
                break;
        }
        return "01 " + Integer.toHexString(command).toUpperCase();
    }

    /**
     * Default ctor.
     */
    public OxygenSensorVoltageCommand(OxygenBank bank, OxygenSensor sensor) {
        super(buildOBDCommand(bank, sensor));
    }


    /** {@inheritDoc} */
    @Override
    protected void performCalculations() {
        // ignore first two bytes [hh hh] of the response
        int a = buffer.get(2);
        int b = buffer.get(3);
        voltage = a / 200.0f;
        percentage = (b * 100) / 128.0f - 100.0f;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormattedResult() {
        return String.format("%.1f%s", voltage, getResultUnit());
    }

    /** {@inheritDoc} */
    @Override
    public String getResultUnit() {
        return "V";
    }

    /** {@inheritDoc} */
    @Override
    public String getCalculatedResult() {
        return String.valueOf(voltage);
    }

    /**
     * <p>Getter for the field <code>voltage</code>.</p>
     *
     * @return a float.
     */
    public float getVoltage() {
        return voltage;
    }

    /**
     * <p>Getter for the field <code>percentage</code>.</p>
     *
     * @return a float.
     */
    public float getPercentage() {
        return percentage;
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return AvailableCommandNames.OXYGEN_SENSOR_VOLTAGE.getValue();
    }

}
