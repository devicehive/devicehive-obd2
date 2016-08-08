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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Nikolay Khabarov on 8/6/16.
 */

public class OBD2Data {
    private int RPM;  //RPM
    private float EngineCoolantTemperature; // C
    private float Load; // percentage
    private float AirIntakeTemperature; // C
    private int IntakeManifoldPressure; // kPa
    private double MassAirFlow; // grams/sec
    private float ThrottlePosition; // percentage
    private int Speed; // km/h
    private float TimingAdvance; // degree, relative to 1st cylinder (top dead centre)
    private float OxygenSensorVoltageBank1Sensor1; // Volts
    private float OxygenSensorVoltageBank1Sensor2; // Volts
    private float FuelLevel; // percentage
    private int FuelPressure; // kPa
    private float ConsumptionRate; // L/h
    private double AirFuelRation; // ratio
    private double ModuleVoltage; // Volts

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

    public static OBD2Data readCurrentData(InputStream obd2input, OutputStream obd2ouput) throws IOException, InterruptedException {
        OBD2Data data = new OBD2Data();
        mRPMCommand.run(obd2input,  obd2ouput);
        data.RPM = mRPMCommand.getRPM();
        mEngineCoolantTemperatureCommand.run(obd2input,  obd2ouput);
        data.EngineCoolantTemperature = mEngineCoolantTemperatureCommand.getTemperature();
        mLoadCommand.run(obd2input,  obd2ouput);
        data.Load = mLoadCommand.getPercentage();
        mAirIntakeTemperatureCommand.run(obd2input,  obd2ouput);
        data.AirIntakeTemperature = mAirIntakeTemperatureCommand.getTemperature();
        mIntakeManifoldPressureCommand.run(obd2input,  obd2ouput);
        data.IntakeManifoldPressure = mIntakeManifoldPressureCommand.getMetricUnit();
        mMassAirFlowCommand.run(obd2input,  obd2ouput);
        data.MassAirFlow = mMassAirFlowCommand.getMAF();
        mThrottlePositionCommand.run(obd2input,  obd2ouput);
        data.ThrottlePosition = mThrottlePositionCommand.getPercentage();
        mSpeedCommand.run(obd2input,  obd2ouput);
        data.Speed = mSpeedCommand.getMetricSpeed();
        mTimingAdvanceCommand.run(obd2input,  obd2ouput);
        data.TimingAdvance = mTimingAdvanceCommand.getPercentage();
        mOxygenSensorVoltageCommandBank1Sensor1.run(obd2input,  obd2ouput);
        data.OxygenSensorVoltageBank1Sensor1 = mOxygenSensorVoltageCommandBank1Sensor1.getVoltage();
        mOxygenSensorVoltageCommandBank1Sensor2.run(obd2input,  obd2ouput);
        data.OxygenSensorVoltageBank1Sensor2 = mOxygenSensorVoltageCommandBank1Sensor2.getVoltage();
        mFuelLevelCommand.run(obd2input,  obd2ouput);
        data.FuelLevel = mFuelLevelCommand.getFuelLevel();
        mFuelPressureCommand.run(obd2input,  obd2ouput);
        data.FuelPressure = mFuelPressureCommand.getMetricUnit();
        mConsumptionRateCommand.run(obd2input,  obd2ouput);
        data.ConsumptionRate = mConsumptionRateCommand.getLitersPerHour();
        mAirFuelRatioCommand.run(obd2input,  obd2ouput);
        data.AirFuelRation = mAirFuelRatioCommand.getAirFuelRatio();
        mModuleVoltageCommand.run(obd2input,  obd2ouput);
        data.ModuleVoltage = mModuleVoltageCommand.getVoltage();
        return data;
    }

    public int getRPM() {
        return RPM;
    }

    public float getEngineCoolantTemperature() {
        return EngineCoolantTemperature;
    }

    public float getLoad() {
        return Load;
    }

    public float getAirIntakeTemperature() {
        return AirIntakeTemperature;
    }

    public int getIntakeManifoldPressure() {
        return IntakeManifoldPressure;
    }

    public double getMassAirFlow() {
        return MassAirFlow;
    }

    public float getThrottlePosition() {
        return ThrottlePosition;
    }

    public int getSpeed() {
        return Speed;
    }

    public float getTimingAdvance() {
        return TimingAdvance;
    }

    public float getOxygenSensorVoltageBank1Sensor1() {
        return OxygenSensorVoltageBank1Sensor1;
    }

    public float getOxygenSensorVoltageBank1Sensor2() {
        return OxygenSensorVoltageBank1Sensor2;
    }

    public float getFuelLevel() {
        return FuelLevel;
    }

    public int getFuelPressure() {
        return FuelPressure;
    }

    public float getConsumptionRate() {
        return ConsumptionRate;
    }

    public double getAirFuelRation() {
        return AirFuelRation;
    }

    public double getModuleVoltage() {
        return ModuleVoltage;
    }
}
