package com.dataart.obd2.obd2_gateway;

import com.github.pires.obd.commands.ObdCommand;
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
import com.github.pires.obd.exceptions.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;

/**
 * Created by Nikolay Khabarov on 8/6/16.
 */

public class OBD2Data {
    private Integer RPM;  //RPM
    private Float EngineCoolantTemperature; // C
    private Float Load; // percentage
    private Float AirIntakeTemperature; // C
    private Integer IntakeManifoldPressure; // kPa
    private Double MassAirFlow; // grams/sec
    private Float ThrottlePosition; // percentage
    private Integer Speed; // km/h
    private Float TimingAdvance; // degree, relative to 1st cylinder (top dead centre)
    private Float OxygenSensorVoltageBank1Sensor1; // Volts
    private Float OxygenSensorVoltageBank1Sensor2; // Volts
    private Float FuelLevel; // percentage
    private Integer FuelPressure; // kPa
    private Float ConsumptionRate; // L/h
    private Double AirFuelRation; // ratio
    private Double ModuleVoltage; // Volts

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

    private static HashSet<ObdCommand> ignoreCommands = new HashSet<ObdCommand>();

    protected OBD2Data() {
    }

    private static boolean run(ObdCommand command, InputStream obd2input, OutputStream obd2ouput) throws IOException, InterruptedException {
        if (ignoreCommands.contains(command)) {
            return false;
        }
        try {
            command.run(obd2input, obd2ouput);
        } catch (ResponseException e) {
            ignoreCommands.add(command);
            return false;
        }
        return true;
    }

    public static void cleanIgnoredCommands() {
        ignoreCommands.clear();
    }

    public static OBD2Data readCurrentData(InputStream obd2input, OutputStream obd2ouput) throws IOException, InterruptedException {
        OBD2Data data = new OBD2Data();
        data.RPM = run(mRPMCommand, obd2input,  obd2ouput) ? mRPMCommand.getRPM() : null;
        data.EngineCoolantTemperature = run(mEngineCoolantTemperatureCommand, obd2input,  obd2ouput) ?
                mEngineCoolantTemperatureCommand.getTemperature() : null;
        data.Load = run(mLoadCommand, obd2input,  obd2ouput) ? mLoadCommand.getPercentage() : null;
        data.AirIntakeTemperature = run(mAirIntakeTemperatureCommand, obd2input,  obd2ouput) ?
                mAirIntakeTemperatureCommand.getTemperature() : null;
        data.IntakeManifoldPressure = run(mIntakeManifoldPressureCommand, obd2input,  obd2ouput) ?
                mIntakeManifoldPressureCommand.getMetricUnit() : null;
        data.MassAirFlow = run(mMassAirFlowCommand, obd2input,  obd2ouput) ?
                mMassAirFlowCommand.getMAF() : null;
        data.ThrottlePosition = run(mThrottlePositionCommand, obd2input,  obd2ouput) ?
                mThrottlePositionCommand.getPercentage() : null;
        data.Speed = run(mSpeedCommand, obd2input,  obd2ouput) ? mSpeedCommand.getMetricSpeed() : null;
        data.TimingAdvance = run(mTimingAdvanceCommand, obd2input,  obd2ouput) ?
                mTimingAdvanceCommand.getPercentage() : null;
        data.OxygenSensorVoltageBank1Sensor1 = run(mOxygenSensorVoltageCommandBank1Sensor1, obd2input,  obd2ouput) ?
                mOxygenSensorVoltageCommandBank1Sensor1.getVoltage() : null;
        data.OxygenSensorVoltageBank1Sensor2 = run(mOxygenSensorVoltageCommandBank1Sensor2, obd2input,  obd2ouput) ?
                mOxygenSensorVoltageCommandBank1Sensor2.getVoltage() : null;
        data.FuelLevel = run(mFuelLevelCommand, obd2input,  obd2ouput) ?
                mFuelLevelCommand.getFuelLevel() : null;
        data.FuelPressure = run(mFuelPressureCommand, obd2input,  obd2ouput) ?
                mFuelPressureCommand.getMetricUnit() : null;;
        data.ConsumptionRate = run(mConsumptionRateCommand, obd2input,  obd2ouput) ?
                mConsumptionRateCommand.getLitersPerHour() : null;
        data.AirFuelRation = run(mAirFuelRatioCommand, obd2input,  obd2ouput) ?
                mAirFuelRatioCommand.getAirFuelRatio() : null;
        data.ModuleVoltage =run(mModuleVoltageCommand, obd2input,  obd2ouput) ?
                mModuleVoltageCommand.getVoltage() : null;
        return data;
    }

    public Integer getRPM() {
        return RPM;
    }

    public Float getEngineCoolantTemperature() {
        return EngineCoolantTemperature;
    }

    public Float getLoad() {
        return Load;
    }

    public Float getAirIntakeTemperature() {
        return AirIntakeTemperature;
    }

    public Integer getIntakeManifoldPressure() {
        return IntakeManifoldPressure;
    }

    public Double getMassAirFlow() {
        return MassAirFlow;
    }

    public Float getThrottlePosition() {
        return ThrottlePosition;
    }

    public Integer getSpeed() {
        return Speed;
    }

    public Float getTimingAdvance() {
        return TimingAdvance;
    }

    public Float getOxygenSensorVoltageBank1Sensor1() {
        return OxygenSensorVoltageBank1Sensor1;
    }

    public Float getOxygenSensorVoltageBank1Sensor2() {
        return OxygenSensorVoltageBank1Sensor2;
    }

    public Float getFuelLevel() {
        return FuelLevel;
    }

    public Integer getFuelPressure() {
        return FuelPressure;
    }

    public Float getConsumptionRate() {
        return ConsumptionRate;
    }

    public Double getAirFuelRation() {
        return AirFuelRation;
    }

    public Double getModuleVoltage() {
        return ModuleVoltage;
    }
}
