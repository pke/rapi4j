package rapi4j.internal.monitorables;

import rapi4j.ActiveSyncDevice;

import com.microsoft.rapi.Rapi.MEMORYSTATUS;

public class MemoryStatusProvider extends BaseStatusPublisher<MEMORYSTATUS> {

	//private final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10/MemoryStatusProvider"); //$NON-NLS-1$

	private static final String MEMORY_FREE = "memory.free"; //$NON-NLS-1$
	private static final String MEMORY_LOAD = "memory.load"; //$NON-NLS-1$
	private static final String MEMORY_TOTAL = "memory.total"; //$NON-NLS-1$
	private static final String MEMORY_USED = "memory.used"; //$NON-NLS-1$

	@Override
	protected MEMORYSTATUS getCurrentStatus(final ActiveSyncDevice device) throws Exception {
		return device.getMemoryStatus();
	}

	public void statusChanged(final MEMORYSTATUS status) {
		postEvent(createEvent(MEMORY_FREE, String.valueOf(status.dwAvailPhys)));
		postEvent(createEvent(MEMORY_LOAD, String.valueOf(status.dwMemoryLoad)));
		postEvent(createEvent(MEMORY_TOTAL, String.valueOf(status.dwTotalPhys)));
		postEvent(createEvent(MEMORY_USED, String.valueOf(status.dwTotalPhys - status.dwAvailPhys)));
	}
}
