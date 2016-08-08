package com.dataart.obd2.obd2_gateway;

import com.github.pires.obd.commands.SpeedCommand;
import com.github.pires.obd.commands.control.ModuleVoltageCommand;
import com.github.pires.obd.commands.control.TimingAdvanceCommand;
import com.github.pires.obd.commands.engine.LoadCommand;
import com.github.pires.obd.commands.engine.MassAirFlowCommand;
import com.github.pires.obd.commands.engine.RPMCommand;
import com.github.pires.obd.commands.engine.ThrottlePositionCommand;
import com.github.pires.obd.commands.fuel.AirFuelRatioCommand;
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand;
import com.github.pires.obd.commands.fuel.FuelLevelCommand;
import com.github.pires.obd.commands.oxygen.OxygenSensorVoltageCommand;
import com.github.pires.obd.commands.pressure.FuelPressureCommand;
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand;
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand;
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nikolay Khabarov on 8/6/16.
 */

public class OBD2Data {
    private int mRPM;  //RPM
    private float mEngineCoolantTemperature; // C
    private float mLoad; // percentage
    private float mAirIntakeTemperature; // C
    private int mIntakeManifoldPressure; // kPa
    private double mMassAirFlow; // grams/sec
    private float mThrottlePosition; // percentage
    private int mSpeed; // km/h
    private float mTimingAdvance; // degree, relative to 1st cylinder (top dead centre)
    private float mOxygenSensorVoltageBank1Sensor1; // Volts
    private float mOxygenSensorVoltageBank1Sensor2; // Volts
    private float mFuelLevel; // percentage
    private int mFuelPressure; // kPa
    private float mConsumptionRate; // L/h
    private double mAirFuelRation; // ratio
    private double mModuleVoltage; // Volts

    private static RPMCommand mRPMCommand = new RPMCommand();
    private static EngineCoolantTemperatureCommand mEngineCoolantTemperatureCommand =
            new EngineCoolantTemperatureCommand();
    private static LoadCommand mLoadCommand = new LoadCommand();
    private static AirIntakeTemperatureCommand mAirIntakeTemperatureCommand =
            new AirIntakeTemperatureCommand();
    private static IntakeManifoldPressureCommand mIntakeManifoldPressureCommand =
            new IntakeManifoldPressureCommand();
    private static MassAirFlowCommand mMassAirFlowCommand = new MassAirFlowCommand();
    private static ThrottlePositionCommand mThrottlePositionCommand = new ThrottlePositionCommand();
    private static SpeedCommand mSpeedCommand = new SpeedCommand();
    private static TimingAdvanceCommand mTimingAdvanceCommand = new TimingAdvanceCommand();
    private static OxygenSensorVoltageCommand mOxygenSensorVoltageCommandBank1Sensor1 =
            new OxygenSensorVoltageCommand(OxygenSensorVoltageCommand.OxygenBank.OXYGEN_BANK_1,
                    OxygenSensorVoltageCommand.OxygenSensor.OXYGEN_SENSOR_1);
    private static OxygenSensorVoltageCommand mOxygenSensorVoltageCommandBank1Sensor2 =
            new OxygenSensorVoltageCommand(OxygenSensorVoltageCommand.OxygenBank.OXYGEN_BANK_1,
                    OxygenSensorVoltageCommand.OxygenSensor.OXYGEN_SENSOR_2);
    private static FuelLevelCommand mFuelLevelCommand = new FuelLevelCommand();
    private static FuelPressureCommand mFuelPressureCommand = new FuelPressureCommand();
    private static ConsumptionRateCommand mConsumptionRateCommand = new ConsumptionRateCommand();
    private static AirFuelRatioCommand mAirFuelRatioCommand = new AirFuelRatioCommand();
    private static ModuleVoltageCommand mModuleVoltageCommand = new ModuleVoltageCommand();


    protected OBD2Data() {
    }

    public static OBD2Data readCurrentData(InputStream obd2input, OutputStream obd2ouput) {
        OBD2Data data = new OBD2Data();
        try {
            mRPMCommand.run(obd2input,  obd2ouput);
            data.mRPM = mRPMCommand.getRPM();
            mEngineCoolantTemperatureCommand.run(obd2input,  obd2ouput);
            data.mEngineCoolantTemperature = mEngineCoolantTemperatureCommand.getTemperature();
            mLoadCommand.run(obd2input,  obd2ouput);
            data.mLoad = mLoadCommand.getPercentage();
            mAirIntakeTemperatureCommand.run(obd2input,  obd2ouput);
            data.mAirIntakeTemperature = mAirIntakeTemperatureCommand.getTemperature();
            mIntakeManifoldPressureCommand.run(obd2input,  obd2ouput);
            data.mIntakeManifoldPressure = mIntakeManifoldPressureCommand.getMetricUnit();
            mMassAirFlowCommand.run(obd2input,  obd2ouput);
            data.mMassAirFlow = mMassAirFlowCommand.getMAF();
            mThrottlePositionCommand.run(obd2input,  obd2ouput);
            data.mThrottlePosition = mThrottlePositionCommand.getPercentage();
            mSpeedCommand.run(obd2input,  obd2ouput);
            data.mSpeed = mSpeedCommand.getMetricSpeed();
            mTimingAdvanceCommand.run(obd2input,  obd2ouput);
            data.mTimingAdvance = mTimingAdvanceCommand.getPercentage();
            mOxygenSensorVoltageCommandBank1Sensor1.run(obd2input,  obd2ouput);
            data.mOxygenSensorVoltageBank1Sensor1 = mOxygenSensorVoltageCommandBank1Sensor1.getVoltage();
            mOxygenSensorVoltageCommandBank1Sensor2.run(obd2input,  obd2ouput);
            data.mOxygenSensorVoltageBank1Sensor2 = mOxygenSensorVoltageCommandBank1Sensor2.getVoltage();
            mFuelLevelCommand.run(obd2input,  obd2ouput);
            data.mFuelLevel = mFuelLevelCommand.getFuelLevel();
            mFuelPressureCommand.run(obd2input,  obd2ouput);
            data.mFuelPressure = mFuelPressureCommand.getMetricUnit();
            mConsumptionRateCommand.run(obd2input,  obd2ouput);
            data.mConsumptionRate = mConsumptionRateCommand.getLitersPerHour();
            mAirFuelRatioCommand.run(obd2input,  obd2ouput);
            data.mAirFuelRation = mAirFuelRatioCommand.getAirFuelRatio();
            mModuleVoltageCommand.run(obd2input,  obd2ouput);
            data.mModuleVoltage = mModuleVoltageCommand.getVoltage();
        } catch (Exception e) {
            e.printStackTrace();
            data = null;
        }
        return data;
    }

    public int getRPM() {
        return mRPM;
    }

    public float getEngineCoolantTemperature() {
        return mEngineCoolantTemperature;
    }

    public float getLoad() {
        return mLoad;
    }

    public float getAirIntakeTemperature() {
        return mAirIntakeTemperature;
    }

    public int getIntakeManifoldPressure() {
        return mIntakeManifoldPressure;
    }

    public double getMassAirFlow() {
        return mMassAirFlow;
    }

    public float getThrottlePosition() {
        return mThrottlePosition;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public float getTimingAdvance() {
        return mTimingAdvance;
    }

    public float getOxygenSensorVoltageBank1Sensor1() {
        return mOxygenSensorVoltageBank1Sensor1;
    }

    public float getOxygenSensorVoltageBank1Sensor2() {
        return mOxygenSensorVoltageBank1Sensor2;
    }

    public float getFuelLevel() {
        return mFuelLevel;
    }

    public int getFuelPressure() {
        return mFuelPressure;
    }

    public float getConsumptionRate() {
        return mConsumptionRate;
    }

    public double getAirFuelRation() {
        return mAirFuelRation;
    }

    public double getModuleVoltage() {
        return mModuleVoltage;
    }
}
