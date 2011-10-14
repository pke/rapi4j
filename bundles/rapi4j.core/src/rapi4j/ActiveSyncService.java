package rapi4j;

import com.microsoft.rapi.IDccManSink;
import com.microsoft.rapi.IRAPIDevice;

/**
 * This service will consume {@link IDccManSink} and {@link IDccManSink2} services.
 *
 * This service registers new {@link IRAPIDevice} and removes them when their
 * disconnection has occurred.
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public interface ActiveSyncService {
	/**
	 * Shows the native dialog box for communication settings.
	 */
	void showCommSettings();
}
