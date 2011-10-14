package rapi4j.examples.memmonitor.internal;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Sample WinCE memory monitor that logs the available memory to the logger at debug level.
 *  
 * @author <a href="mailto:phil.kursawe@gmail.com">Philipp Kursawe</a>
 *
 */
public class MemoryMonitor implements EventHandler {

	//private final Logger logger = LoggerFactory.getLogger(MemoryMonitor.class);

	public void handleEvent(final Event event) {
		final String name = (String) event.getProperty("mon.statusvariable.name"); //$NON-NLS-1$
		final String value = (String) event.getProperty("mon.statusvariable.value"); //$NON-NLS-1$
		if (name.equals("memory.free")) { //$NON-NLS-1$
			//this.logger.debug("Memory: {} bytes available", value); //$NON-NLS-1$
		}
	}
}
