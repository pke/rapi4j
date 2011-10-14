package rapi4j.internal.monitorables;

import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventHandler;

import rapi4j.ActiveSyncDevice;
import rapi4j.internal.StatusRunnable;

public abstract class BaseStatusPublisher<T> implements StatusRunnable<T> {

	private static final String COMPONENT_NAME_PROPERTY = "component.name"; //$NON-NLS-1$

	//private final Logger logger = LoggerFactory.getLogger(getClass());
	private final AtomicReference<ActiveSyncDevice> deviceRef = new AtomicReference<ActiveSyncDevice>();
	private EventAdmin eventAdmin;
	private StatusThread<T> thread;
	private ComponentContext context;

	// private final String listenerId;

	static class StatusThread<T> extends Thread {
		private static final int DEFAULT_INTERVAL = 1;
		private volatile int interval;
		private StatusRunnable<T> runnable;
		private final AtomicReference<T> lastStatusRef = new AtomicReference<T>();

		public StatusThread(final String name, final StatusRunnable<T> runnable) {
			this(name, runnable, DEFAULT_INTERVAL);
		}

		public StatusThread(final String name, final StatusRunnable<T> runnable, final int seconds) {
			super(name);
			this.interval = 1000 * seconds;
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					sleep(this.interval);
					final T status = this.runnable.getCurrentStatus();
					if (status != null && !status.equals(this.lastStatusRef.get())) {
						this.lastStatusRef.set(status);
						this.runnable.statusChanged(status);
					}
				}
			} catch (final InterruptedException e) {
				// Ignore
			}
		}

		void setInterval(final int seconds) {
			this.interval = 1000 * seconds;
			notify();
		}

		void notifyEventListener(final EventHandler eventHandler) {
			final T status = this.lastStatusRef.get();
			if (status != null) {
				this.runnable.statusChanged(status);
			}
		}
	}

	protected void activate(final ComponentContext context, final Map<String, Object> properties) {
		this.context = context;
		this.eventAdmin = (EventAdmin) context.locateService(EventAdmin.class.getSimpleName());
		modified(properties);
		this.thread = new StatusThread<T>((String) properties.get(COMPONENT_NAME_PROPERTY), this);
		this.thread.start();
	}

	protected void bind(final ActiveSyncDevice device) {
		this.deviceRef.set(device);
	}

	protected void unbind(final ActiveSyncDevice device) {
		this.deviceRef.compareAndSet(device, null);
	}

	protected void deactivate(final ComponentContext context) {
		this.thread.interrupt();
	}

	protected void modified(final Map<String, Object> properties) {
		//this.logger.trace("Updating properties"); //$NON-NLS-1$
		try {
			this.thread.setInterval(Integer.valueOf(properties.get("interval").toString())); //$NON-NLS-1$
		} catch (final Exception e) {
		}
	}

	protected void bind(final EventHandler eventHandler) {
		// this.thread.notifyEventListener(eventHandler);
	}

	protected Event createEvent(final String name, final String value) {
		final Map<String, Object> props = new HashMap<String, Object>();
		// props.put("mon.listener.id", this.listenerId);
		props.put("mon.monitorable.pid", this.context.getProperties().get(COMPONENT_NAME_PROPERTY)); //$NON-NLS-1$
		props.put("mon.statusvariable.value", value); //$NON-NLS-1$
		props.put("mon.statusvariable.name", name); //$NON-NLS-1$
		return new Event("org/osgi/service/monitor", props); //$NON-NLS-1$
	}

	public T getCurrentStatus() {
		try {
			return getCurrentStatus(this.deviceRef.get());
		} catch (final Exception e) {
			return null;
		}
	}

	protected void postEvent(final Event event) {
		this.eventAdmin.postEvent(event);
	}

	protected abstract T getCurrentStatus(final ActiveSyncDevice device) throws Exception;
}
