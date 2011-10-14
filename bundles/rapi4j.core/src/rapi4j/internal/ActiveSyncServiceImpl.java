package rapi4j.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentFactory;
import org.osgi.service.component.ComponentInstance;

import rapi4j.ActiveSyncDevice;
import rapi4j.ActiveSyncDeviceListener;
import rapi4j.ActiveSyncService;
import rapi4j.internal.RapiHelper.RegistryKey;
import rapi4j.internal.RapiHelper.SystemParametersInfo;

import com.microsoft.rapi.DccManSink;
import com.microsoft.rapi.IDccMan;
import com.microsoft.rapi.IDccManSink;
import com.microsoft.rapi.IRAPIDevice;
import com.microsoft.rapi.Native;
import com.microsoft.rapi.Rapi;
import com.microsoft.rapi.Rapi.CeOsVersionInfo;
import com.microsoft.rapi.Rapi.HKEY;
import com4j.CLSCTX;
import com4j.COM4J;
import com4j.Holder;

/**
 * Service that handles ActiveSync events. 
 * 
 * <p>
 * This service publishes each connected {@link IRAPIDevice} as a service.
 * It sets the following properties on the service:
 * <table border="1">
 * <tr><th>Name</th><th>Description</th></tr>
 * <tr>
 * 	<td><code>device.id</code></td>
 * 	<td>
 * 	An id that uniquely identifies the device.
 * 	</td>
 * </tr>
 * <tr>
 * 	<td><code>device.os.version</code></td>
 * 	<td>
 * 	The major and minor version of the operating system running on the device.
 * 	</td>
 * </tr>
 * <tr>
 * 	<td><code>device.os.platform</code></td>
 * 	<td>
 *  The device's platform name. For example, "PocketPC" or "Smartphone".
 * 	</td>
 * </tr>
 * <tr>
 * 	<td><code>device.name</code></td>
 * 	<td>
 *  The device's ID Name.<br/>
 *  <b>For Windows Mobile:</b> On Windows Mobile powered devices, this information can be found in Settings > About > Device ID > Device Name.
 * 	</td>
 * </tr>
 * </table>
 * 
 * This service does not provide an API to query the available devices. All
 * available devices are registered as services.
 * </p>
 * 
 * <h2>Use-case</h2>
 * <p>
 * You could have a database the contains additional informations about devices.
 * Use the device.id to determine if a new device entered the system and display
 * a dialog box to the user that allows to add the device to the internal database. 
 * </p>
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public class ActiveSyncServiceImpl implements ActiveSyncService {

	private IDccManSink sink;
	private IDccMan dccMan;
	private Holder<Integer> sinkCookie;
	//private final Logger logger = LoggerFactory.getLogger(ActiveSyncService.class);
	private ComponentContext context;
	private Thread thread;

	private interface Notifier<T> {
		void notify(T object);
	}

	@SuppressWarnings({ "nls", "unchecked" })
	private <T> void notifyListeners(final Notifier<? super T> visitor) {
		final Object[] services = this.context.locateServices("ActiveSyncDeviceListener");
		if (services != null) {
			for (final Object service : services) {
				try {
					visitor.notify((T) service);
				} catch (final Throwable e) {
					//ActiveSyncServiceImpl.this.logger.error("Error calling listener", e);
				}
			}
		}
	}

	@SuppressWarnings("nls")
	protected void activate(final ComponentContext context) {
		this.context = context;
		this.thread = new Thread("ActiveSync Service") {
			{
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				try {
					ActiveSyncServiceImpl.this.dccMan = COM4J.createInstance(IDccMan.class, IDccMan.CLSID,
							CLSCTX.INPROC_SERVER);
					/*final IRAPIDesktop desktop = COM4J.createInstance(IRAPIDesktop.class, IRAPIDesktop.CLSID);
					final IRAPIEnumDevices devices = desktop.enumDevices();
					final IRAPIDevice device;
					*/
					context.getBundleContext().registerService(IDccMan.class.getName(),
							ActiveSyncServiceImpl.this.dccMan, null);

					ActiveSyncServiceImpl.this.sink = Native.createIDccManSink(new DccManSink() {
						private ActiveSyncDevice deviceService;
						private ComponentInstance instance;

						@Override
						public void onLogIpAddr(final int ip) {
							final byte addr[] = new byte[] { (byte) (ip >> 24 & 0xFF), (byte) (ip >> 16 & 0xFF),
									(byte) (ip >> 8 & 0xFF), (byte) (ip & 0xFF) };
							try {
								final InetAddress address = InetAddress.getByAddress(addr);
								notifyListeners(new Notifier<ActiveSyncDeviceListener>() {
									public void notify(final ActiveSyncDeviceListener listener) {
										listener.onNewIpAddress(deviceService, address);
									}
								});
							} catch (final UnknownHostException e) {
							}
						}

						@Override
						public void onLogActive() {
							if (this.instance != null) {
								this.instance.dispose();
								this.instance = null;
							}

							try {
								final ComponentFactory deviceFactory = (ComponentFactory) context
										.locateService("DeviceFactory");

								Rapi.instance.CeRapiInit();
								final Properties props = new Properties();
								final String deviceId = RapiHelper.getDeviceId();
								if (!"".equals(deviceId)) { //$NON-NLS-1$
									props.put(ActiveSyncDevice.DEVICE_ID, deviceId);
								}
								// final SystemInfo systemInfo = RapiHelper.getSystemInfo();
								final CeOsVersionInfo osInfo = RapiHelper.getOsVersionInfo();
								props.setProperty(ActiveSyncDevice.OS_VERSION, String.format(
										"%d.%d", osInfo.dwMajorVersion, osInfo.dwMinorVersion)); //$NON-NLS-1$
								String info = RapiHelper.getSystemParametersInfoString(
										SystemParametersInfo.SPI_GETOEMINFO, null);
								if (info != null) {
									props.setProperty(ActiveSyncDevice.DEVICE_OEM, info);
								}
								info = RapiHelper.getSystemParametersInfoString(
										SystemParametersInfo.SPI_GETPLATFORMTYPE, null);
								if (info != null) {
									props.setProperty(ActiveSyncDevice.DEVICE_PLATFORM, info);
								}

								final RapiHelper.RegistryKey key = RegistryKey.create(HKEY.HKEY_LOCAL_MACHINE, "Ident"); //$NON-NLS-1$
								if (key != null) {
									props.setProperty(ActiveSyncDevice.DEVICE_NAME, key.getValue("Name", "")); //$NON-NLS-1$ //$NON-NLS-2$
									key.close();
								}

								this.instance = deviceFactory.newInstance(props);
								this.deviceService = (ActiveSyncDevice) this.instance.getInstance();
								notifyListeners(new Notifier<ActiveSyncDeviceListener>() {
									public void notify(final ActiveSyncDeviceListener listener) {
										listener.onConnected(deviceService);
									}
								});
							} catch (final IOException e) {
								//ActiveSyncServiceImpl.this.logger.error("Error creating device", e);
							}
						}

						@Override
						public void onLogDisconnection() {
							if (this.deviceService != null) {

								notifyListeners(new Notifier<ActiveSyncDeviceListener>() {
									public void notify(final ActiveSyncDeviceListener listener) {
										listener.onDisconnected(deviceService);
									}
								});
								if (this.instance != null) {
									this.instance.dispose();
									this.instance = null;
								}
								this.deviceService = null;
							}
						}
					});
					ActiveSyncServiceImpl.this.sinkCookie = new Holder<Integer>();
					ActiveSyncServiceImpl.this.dccMan.Advise(ActiveSyncServiceImpl.this.sink,
							ActiveSyncServiceImpl.this.sinkCookie);

					/*RapiHelper.runWithInit(new Runnable() {
						public void run() {
							try {
								final Rapi.MemoryStatus memoryStatus = RapiHelper.getMemoryStatus();
							} catch (final IOException e) {
								e.printStackTrace();
							}
						}
					});*/

					synchronized (this) {
						wait();
					}

					if (ActiveSyncServiceImpl.this.dccMan != null && ActiveSyncServiceImpl.this.sinkCookie != null) {
						ActiveSyncServiceImpl.this.dccMan.Unadvise(ActiveSyncServiceImpl.this.sinkCookie.value);
						// ActiveSyncServiceImpl.this.sink.dispose();
						// ActiveSyncServiceImpl.this.dccMan.dispose();
					}
				} catch (final Throwable e) {
					e.printStackTrace();
				}
			};
		};
	}

	@SuppressWarnings("nls")
	protected void deactivate(final ComponentContext context) {
		//this.logger.debug("Deactivating...");
		synchronized (this.thread) {
			this.thread.notify();
			try {
				this.thread.join();
			} catch (final InterruptedException e) {
			}
		}
		//this.logger.debug("Deactivated");
	}

	public void showCommSettings() {
		if (this.dccMan != null) {
			this.dccMan.ShowCommSettings();
		}
	}
}
