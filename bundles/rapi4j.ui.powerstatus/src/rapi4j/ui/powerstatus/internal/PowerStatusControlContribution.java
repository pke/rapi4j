package rapi4j.ui.powerstatus.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

public class PowerStatusControlContribution extends WorkbenchWindowControlContribution implements EventHandler {

	private BatteryControl control;

	final static class Helper {

		public static void assign(final Object object, final String property, final String value,
				final Runnable runnable) {
			final Class<?> clazz = object.getClass();
			int dotIndex = property.indexOf('.');
			final String fieldName = dotIndex == -1 ? property : property.substring(0, dotIndex++)
					+ property.substring(dotIndex++, dotIndex).toUpperCase() + property.substring(dotIndex);
			try {
				final Field field = clazz.getDeclaredField(fieldName);
				final Class<?> fieldClass = field.getType();
				Object valueOf = null;
				if (fieldClass.isPrimitive()) {
					if (fieldClass == Byte.TYPE) {
						valueOf = Byte.valueOf(value);
					} else if (fieldClass == Boolean.TYPE) {
						valueOf = Boolean.valueOf(value);
					} else if (fieldClass == Integer.TYPE) {
						valueOf = Integer.valueOf(value);
					}
				} else {
					final Method valueOfMethod = fieldClass.getMethod("valueOf", String.class); //$NON-NLS-1$
					valueOf = valueOfMethod.invoke(null, value);
				}
				field.setAccessible(true);
				if (valueOf != null && !valueOf.equals(field.get(object))) {
					field.set(object, valueOf);
					if (runnable != null) {
						runnable.run();
					}
				}
			} catch (final NoSuchFieldException e) {
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	class BatteryControl {
		final Label label;
		private byte batteryPercent = 0;
		private boolean batteryCharging = false;
		private final DefaultToolTip toolTip;
		private byte acline = (byte) 0xff;
		private byte batteryChemistry = (byte) 0xff;
		private int batteryVoltage;
		private int batteryCurrent;
		private final Map<String, String> data = new HashMap<String, String>();

		public BatteryControl(final Composite parent) {
			this.label = new Label(parent, SWT.NULL);
			this.toolTip = new DefaultToolTip(this.label, ToolTip.NO_RECREATE, false);
			update();
		}

		void update(final String name, final String value) {
			this.data.put(name, name);
			update();
		}

		protected void update() {
			if (this.label.isDisposed()) {
				return;
			}
			this.label.setImage(getImage(this.batteryPercent, this.batteryCharging, 16));
			this.toolTip.setImage(getImage(this.batteryPercent, this.batteryCharging, 48));
			String text = String.format("%d%% ", this.batteryPercent); //$NON-NLS-1$
			if (this.batteryCharging) {
				text += "(charging)";
			}
			text += "\n" + "AC-Line" + ": "; //$NON-NLS-1$//$NON-NLS-3$
			switch (this.acline) {
			case 0x00:
				text += "not connected";
				break;
			case 0x01:
				text += "connected";
				break;
			case 0x02:
				text += "backup";
				break;
			default:
				text += "unknown";
				break;
			}
			text += "\n"; //$NON-NLS-1$

			switch (this.batteryChemistry) {
			case 0x1:
				text += "";
				break;
			case 0x2:
				text += "";
				break;
			case 0x3:
				text += "";
				break;
			case 0x4:
				text += "Li-Ion";
				break;
			case 0x5:
				text += "";
				break;
			case 0x6:
				text += "";
				break;
			}
			text += "\n";
			text += "Voltage" + ": ";
			text += this.batteryVoltage;
			text += "\n";
			text += "Current" + ": ";
			text += this.batteryCurrent;
			text += " mA";

			this.toolTip.setText(text);
		}

		@SuppressWarnings("nls")
		private Image getImage(final int percentage, final boolean charging, final int size) {
			final Bundle bundle = FrameworkUtil.getBundle(getClass());
			final int steps[] = new int[] { 0, 20, 40, 60, 80, 100 };
			final int step = (Math.max(percentage, 100) + 10) / 20;
			final String imageName = "e" + size + "/battery-" + (charging ? "" : "dis") + "charging-"
					+ String.format("%03d", steps[step]);
			final String keyName = bundle.getSymbolicName() + imageName;
			final Image image = JFaceResources.getImage(keyName);
			if (image != null) {
				return image;
			}
			final String imagePath = "$nl$/icons/" + imageName + ".png";
			final URL url = FileLocator.find(bundle, new Path(imagePath), null);
			JFaceResources.getImageRegistry().put(keyName, ImageDescriptor.createFromURL(url));
			return JFaceResources.getImage(keyName);
		}

		public Control getControl() {
			return this.label;
		}

		public void setPercent(final byte percent) {
			if (this.batteryPercent == percent) {
				return;
			}
			this.batteryPercent = percent > 100 ? 100 : percent;
			update();
		}

		public void setCharging(final boolean charging) {
			if (this.batteryCharging == charging) {
				return;
			}
			this.batteryCharging = charging;
			update();
		}

		public void setAcLine(final byte acLine) {
			if (this.acline == acLine) {
				return;
			}
			this.acline = acLine;
			update();
		}

		public void setChemestry(final byte chemistry) {
			if (this.batteryChemistry == chemistry) {
				return;
			}
			this.batteryChemistry = chemistry;
			update();
		}
	}

	@Override
	protected Control createControl(final Composite parent) {
		this.control = new BatteryControl(parent);

		final Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(EventConstants.EVENT_TOPIC, new String[] { "org/osgi/service/monitor" }); //$NON-NLS-1$
		properties.put("mon.monitorable.pid", "rapi4j.PowerStatusMonitor"); //$NON-NLS-1$
		final ServiceRegistration serviceRegistration = FrameworkUtil.getBundle(getClass()).getBundleContext()
				.registerService(EventHandler.class.getName(), this, properties);
		this.control.getControl().addDisposeListener(new DisposeListener() {
			public void widgetDisposed(final DisposeEvent e) {
				serviceRegistration.unregister();
			}
		});
		return this.control.getControl();
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@Override
	protected int computeWidth(final Control control) {
		return 16;
	}

	public void handleEvent(final Event event) {
		final String name = (String) event.getProperty("mon.statusvariable.name"); //$NON-NLS-1$
		final String value = (String) event.getProperty("mon.statusvariable.value"); //$NON-NLS-1$
		Helper.assign(this.control, name, value, new Runnable() {
			public void run() {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						PowerStatusControlContribution.this.control.update();
					}
				});
			}
		});
	}
}
