package rapi4j.internal.monitorables;

import java.util.HashSet;
import java.util.Set;

import rapi4j.ActiveSyncDevice;

import com.microsoft.rapi.Rapi.SystemPowerStateEx2;
import com.microsoft.rapi.Rapi.SystemPowerStatusEx;

public class SystemPowerPublisher extends BaseStatusPublisher<SystemPowerStateEx2> {

	private static final String VAR_BATTERY_CHARGING = "battery.charging"; //$NON-NLS-1$
	private static final String VAR_BATTERY_PERCENT = "battery.percent"; //$NON-NLS-1$
	private static final String VAR_BATTERY_VOLTAGE = "battery.voltage"; //$NON-NLS-1$
	private static final String VAR_BATTERY_CURRENT = "battery.current"; //$NON-NLS-1$
	private static final String VAR_BATTERY_TEMPERATURE = "battery.temperature"; //$NON-NLS-1$
	private static final String VAR_BATTERY_CHEMISTRY = "battery.chemistry"; //$NON-NLS-1$
	private final static String VAR_ACLINE = "acline"; //$NON-NLS-1$

	@SuppressWarnings("serial")
	private final Set<String> staticVars = new HashSet<String>() {
		{
			add(VAR_BATTERY_CHEMISTRY);
		}
	};

	@Override
	protected SystemPowerStateEx2 getCurrentStatus(final ActiveSyncDevice device) throws Exception {
		return device.getPowerStatus();
	}

	public void statusChanged(final SystemPowerStateEx2 status) {
		postEvent(createEvent(VAR_ACLINE, String.valueOf(status.ACLineStatus)));
		postEvent(createEvent(VAR_BATTERY_CHARGING, String
				.valueOf((status.BatteryFlag & SystemPowerStatusEx.BATTERY_FLAG_CHARGING) != 0)));
		postEvent(createEvent(VAR_BATTERY_PERCENT, String.valueOf(status.BatteryLifePercent)));
		postEvent(createEvent(VAR_BATTERY_CURRENT, String.valueOf(status.BatteryCurrent)));
		postEvent(createEvent(VAR_BATTERY_VOLTAGE, String.valueOf(status.BatteryVoltage)));
		postEvent(createEvent(VAR_BATTERY_CHEMISTRY, String.valueOf(status.BatteryChemistry)));
		postEvent(createEvent(VAR_BATTERY_TEMPERATURE, String.valueOf(status.BatteryTemperature)));
	}
}
