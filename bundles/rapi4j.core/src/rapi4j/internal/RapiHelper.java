package rapi4j.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.Version;

import com.microsoft.rapi.IDccMan;
import com.microsoft.rapi.Rapi;
import com.microsoft.rapi.Rapi.CeOsVersionInfo;
import com.microsoft.rapi.Rapi.HKEY;
import com.microsoft.rapi.Rapi.MEMORYSTATUS;
import com.microsoft.rapi.Rapi.SystemPowerStateEx2;
import com.microsoft.rapi.Rapi.SystemPowerStatusEx;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.examples.win32.W32Errors;
import com.sun.jna.examples.win32.Kernel32.FILETIME;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.examples.win32.W32API.HANDLEByReference;
import com.sun.jna.examples.win32.W32API.HRESULT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * 
 * <p>
 * This class provides an auto-deploy feature for invoke method calls.
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 * TODO:
 * - add (trace) logging 
 * 
 */
public class RapiHelper {

	/** Location of the native libraries that you can use with {@link #invoke(String, String, byte[], InvokeRunnable)} */
	private static final String NATIVE_SOURCE_PATH = "native/"; //$NON-NLS-1$

	//private static final Logger logger = LoggerFactory.getLogger("rapi4j"); //$NON-NLS-1$

	/** The name of the native library that is installed on the mobile device */
	private static String dllName = "rapi4jex.dll"; //$NON-NLS-1$

	/** The complete path of the native library on the mobile device */
	private static String targetPath = "\\Windows\\" + dllName; //$NON-NLS-1$

	@SuppressWarnings("nls")
	public static String getSystemError(final int code) {
		final Kernel32 lib = Kernel32.INSTANCE;
		final PointerByReference pref = new PointerByReference();
		lib.FormatMessage(Kernel32.FORMAT_MESSAGE_ALLOCATE_BUFFER | Kernel32.FORMAT_MESSAGE_FROM_SYSTEM
				| Kernel32.FORMAT_MESSAGE_IGNORE_INSERTS, null, code, 0, pref, 0, null);
		String s = pref.getValue().getString(0, !Boolean.getBoolean("w32.ascii"));
		s = s.replace(".\r", ".").replace(".\n", ".");
		lib.LocalFree(pref.getValue());
		return s;
	}

	public static void checkLastError() throws IOException {
		checkLastError(null);
	}

	public static void checkRapiError() throws IOException {
		checkRapiError(null);
	}

	@SuppressWarnings("nls")
	public static void checkLastError(final String message) throws IOException {
		checkRapiError(message);
		String finalMessage = message != null ? message : "";
		final int error = Rapi.instance.CeGetLastError();
		if (error != W32Errors.NO_ERROR) {
			finalMessage += getSystemError(error);
			throw new IOException(finalMessage);
		}
	}

	public static void checkRapiError(final String message) throws IOException {
		final Rapi.HRESULT rapiError = Rapi.instance.CeRapiGetError();
		String finalMessage = message != null ? message : ""; //$NON-NLS-1$
		if (rapiError.failed() && rapiError.intValue() != Rapi.CERAPI_E_ALREADYINITIALIZED) {
			finalMessage += getSystemError(rapiError.intValue());
			throw new IOException(finalMessage);
		}
	}

	public static CeOsVersionInfo getOsVersionInfo() throws IOException {
		final CeOsVersionInfo info = new CeOsVersionInfo();
		if (!Rapi.instance.CeGetVersionEx(info)) {
			checkLastError();
		}
		return info;
	}

	public static Rapi.SystemInfo getSystemInfo() throws IOException {
		final Rapi.SystemInfo info = new Rapi.SystemInfo();
		Rapi.instance.CeGetSystemInfo(info);
		checkRapiError();
		return info;
	}

	public static SystemPowerStatusEx getPowerStatus(final boolean update) throws IOException {
		final SystemPowerStatusEx status = new SystemPowerStatusEx();
		if (!Rapi.instance.CeGetSystemPowerStatusEx(status, update)) {
			checkLastError();
		}
		return status;
	}

	public static MEMORYSTATUS getMemoryStatus() throws IOException {
		final MEMORYSTATUS status = new MEMORYSTATUS();
		Rapi.instance.CeGlobalMemoryStatus(status);
		checkRapiError();
		return status;
	}

	public static boolean getFileTimes(final String targetFile, final FILETIME creation, final FILETIME access,
			final FILETIME written) {
		final HANDLE fileHandle = Rapi.instance.CeCreateFile(targetFile, Kernel32.GENERIC_READ,
				Kernel32.FILE_SHARE_READ, null, Kernel32.OPEN_EXISTING, Kernel32.FILE_ATTRIBUTE_NORMAL, null);
		if (fileHandle.equals(W32API.INVALID_HANDLE_VALUE)) {
			return false;
		}
		try {
			return Rapi.instance.CeGetFileTime(fileHandle, creation, access, written);
		} finally {
			Rapi.instance.CeCloseHandle(fileHandle);
		}
	}

	public interface FileHandleVisitor {
		void visit(HANDLE fileHandle);
	}

	/**
	 * Copies a stream to a file on the Windows CE device.
	 * 
	 * @param stream to read the data from
	 * @param targetFile to write the stream data to
	 * @param afterVisitor is called after the stream has been successfully copied. You can do additional actions here 
	 * on the open file handle. However you must not close the open handle.
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	public static void copyStream(final InputStream stream, final String targetFile, final IDccMan dccMan,
			final FileHandleVisitor afterVisitor) throws IOException {
		final HANDLE fileHandle = Rapi.instance.CeCreateFile(targetFile, Kernel32.GENERIC_WRITE,
				Kernel32.FILE_SHARE_WRITE, null, Kernel32.OPEN_ALWAYS, Kernel32.FILE_ATTRIBUTE_NORMAL, null);
		if (fileHandle.equals(W32API.INVALID_HANDLE_VALUE)) {
			checkLastError("Could not open file " + targetFile + " on the CE device");
		}
		try {
			final byte buffer[] = new byte[4096];
			final IntByReference written = new IntByReference();
			int read;
			while ((read = stream.read(buffer)) > -1) {
				if (!Rapi.instance.CeWriteFile(fileHandle, buffer, read, written, null)) {
					checkLastError();
				}
			}
			if (afterVisitor != null) {
				// TODO: How can we protect ourself here against exceptions?
				afterVisitor.visit(fileHandle);
			}
		} finally {
			if (dccMan != null) {
				dccMan.SetIconDataTransferring();
			}
			Rapi.instance.CeCloseHandle(fileHandle);
		}
	}

	/**
	 * @see #copyStream(InputStream, String, FileHandleVisitor)
	 */
	public static void copyStream(final InputStream stream, final String targetFile) throws IOException {
		copyStream(stream, targetFile, null, null);
	}

	/**
	 * @param filePath
	 * @return whether the given file exists in the Windows CE device.
	 */
	public static boolean fileExists(final String filePath) {
		return 0xffffffff != Rapi.instance.CeGetFileAttributes(filePath);
	}

	/**
	 * This interfaces run method is called to create an object from the memory buffer returned by a call to CeRapiInvoke.
	 *  
	 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
	 *
	 * @param <T>
	 * @see RapiHelper.invoke
	 */
	public static interface InvokeRunnable<T> {
		T run(Pointer buffer, int size);
	}

	/**
	 * Does not take any input data and also does not return anything.
	 * 
	 * @param function
	 * @throws IOException
	 */
	public static void invoke(final String function) throws IOException {
		invoke(targetPath, function, (byte[]) null, null);
	}

	/**
	 * Uses the bundles tsrapiex.dll as target dll to load the functions from.
	 * 
	 * @param <T>
	 * @param function
	 * @param input
	 * @param runnable
	 * @return
	 * @throws IOException
	 */
	public static <T> T invoke(final String function, final byte[] input, final InvokeRunnable<T> runnable)
			throws IOException {
		return invoke(targetPath, function, input, runnable);
	}

	public static <T> T invoke(final String function, final Structure input, final InvokeRunnable<T> runnable)
			throws IOException {
		return invoke(targetPath, function, input, runnable);
	}

	public static class DLLVERSIONINFO extends Structure {
		public int dwSize = size();
		public int dwMajorVersion;
		public int dwMinorVersion;
		public int dwBuildVersion;
		public int dwPlatformId;

		public DLLVERSIONINFO(final Pointer p) {
			super(p);
			read();
		}
	}

	public static <T> T invoke(final String dllPath, final String function, final Structure input,
			final InvokeRunnable<T> runnable) throws IOException {
		// Ensure we have all the data from the structure in the internal memory
		input.write();
		return invoke(dllPath, function, input.getPointer().getByteArray(0, input.size()), runnable);
	}

	/**
	 * Searches also in the fragments.
	 * 
	 * @param bundle
	 * @param path
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static URL getBundleEntry(final Bundle bundle, final String path) {
		final File file = new File(path);
		final Enumeration<URL> entries = bundle.findEntries(file.getParent(), file.getName(), false);
		if (entries != null && entries.hasMoreElements()) {
			return entries.nextElement();
		}
		return null;
	}

	@SuppressWarnings("nls")
	private static void updateInvokeLibrary(final String dllPath) throws IOException {
		final Bundle bundle = FrameworkUtil.getBundle(RapiHelper.class);
		final String dllName = new File(dllPath).getName();

		if (fileExists(dllPath)) {
			//logger.debug("{} does already exist on the device, checking if update is required", dllPath);
			final Properties properties = new Properties();
			final String versionFile = NATIVE_SOURCE_PATH + dllName + ".ver";
			final URL entry = getBundleEntry(bundle, versionFile);
			if (entry == null) {
				//logger.warn(
					//	"Cannot check for update cause version info properties file {} is missing in bundle: {}. Updating anyway, to be sure! Please add the properties file for proper version checking.",
						//versionFile, bundle.getSymbolicName());
			} else {
				properties.load(entry.openStream());
				final Version ourVersion = Version.parseVersion(properties.getProperty("version").replace("\"", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				final Version otherVersion = doInvoke(dllPath,
						"GetDllVersion", (byte[]) null, new InvokeRunnable<Version>() { //$NON-NLS-1$
							public Version run(final Pointer buffer, final int size) {
								final DLLVERSIONINFO otherInfo = new DLLVERSIONINFO(buffer);
								return new Version(otherInfo.dwMajorVersion, otherInfo.dwMinorVersion,
										otherInfo.dwBuildVersion);
							}
						});
				final boolean needUpdate = otherVersion.compareTo(ourVersion) < 0;
				//logger.debug("Update {}. Bundle version of {} is {}. Found version on device is {}", new Object[] {
					//	needUpdate ? "required" : "not required", dllName, ourVersion, otherVersion });
				if (!needUpdate) {
					return;
				}
			}
		}

		final URL entry = getBundleEntry(bundle, NATIVE_SOURCE_PATH + dllName);

		if (null == entry) {
			throw new IOException(dllName + " was not found in the bundle " + bundle.getSymbolicName());
		}
		//logger.debug("Updating {}", dllName);
		final InputStream stream = entry.openStream();
		copyStream(stream, dllPath);
		//logger.debug("Updated {}", dllName);
	}

	/**
	 * Uses CeRapiInvoke to call a function on the mobile device.
	 * 
	 * <p>
	 * This method ensure that the library is copied to the mobile device before executing the call.
	 * 
	 * @param <T> Type of the expected return value.
	 * @param function
	 * @param input
	 * @param runnable
	 * @return
	 * @throws IOException
	 */
	public static <T> T invoke(final String dllPath, final String function, final byte[] input,
			final InvokeRunnable<T> runnable) throws IOException {
		updateInvokeLibrary(dllPath);
		return doInvoke(dllPath, function, input, runnable);
	}

	public static class LocalPointerByReference extends PointerByReference {
		String name;

		public LocalPointerByReference() {
			this.name = "unknown";
		}

		public LocalPointerByReference(final String name) {
			this.name = name;
		}

		@Override
		public void setValue(final Pointer value) {
			super.setValue(value);
		}

		@Override
		public Pointer getValue() {
			return super.getValue();
		}

		@Override
		public void setPointer(final Pointer p) {
			super.setPointer(p);
		}

		@Override
		public Pointer getPointer() {
			return super.getPointer();
		}

		@SuppressWarnings("nls")
		@Override
		protected void finalize() throws Throwable {
			if (getPointer() != null) {
				//logger.debug("Freeing memory: {}", getValue());
				Kernel32.INSTANCE.LocalFree(getValue());
			}
		}
	}

	@SuppressWarnings("nls")
	private static <T> T doInvoke(final String dllPath, final String function, final byte[] input,
			final InvokeRunnable<T> runnable) throws IOException {
		final int cbInput = input != null ? input.length : 0;
		final Pointer pInput = cbInput > 0 ? Kernel32.INSTANCE.LocalAlloc(Kernel32.LPTR, cbInput) : null;
		if (pInput != null) {
			pInput.write(0, input, 0, cbInput);
		}
		final PointerByReference outBuffer = new PointerByReference(/*dllPath + "@" + function*/);

		try {
			//logger.debug("Invoking {} in {}", function, dllPath);
			final IntByReference outBufferSize = new IntByReference();
			final HRESULT hresult = Rapi.instance.CeRapiInvoke(dllPath, function, cbInput, pInput, outBufferSize,
					outBuffer, null, 0);
			if (hresult.failed()) {
				int lastError = Rapi.instance.CeGetLastError();
				if (0 == lastError) {
					lastError = Rapi.instance.CeRapiGetError().intValue();
				}
				final String error = getSystemError(lastError != 0 ? lastError : hresult.intValue());
				throw new IOException("Error during invocation of \"" + function + "\": " + error);
			}
			if (runnable != null) {
				final Pointer value = outBuffer.getValue();
				if (value != null) {
					final int size = outBufferSize.getValue();
					final Memory memory = new Memory(size);
					memory.write(0, value.getByteArray(0, size), 0, size);
					return runnable.run(memory.share(0), size);
				}
			}
			return null;
		} catch (final Throwable e) {
			//logger.error("Error during invocation of \"" + function + "\": ", e);
			throw new IOException("Error during invocation of \"" + function + "\" " + e.getMessage());
		} finally {
			Kernel32.INSTANCE.LocalFree(outBuffer.getValue());
		}
	}

	/**
	 * <ol>
	 * <li>
	 * Checks if the ID is already in the devices Registry at \System\Ident\UUID
	 * </li>
	 * <li>
	 * Checks if the tsrapiex.dll is already on the device and matches the version we have locally.
	 * If not, it copies the dll to the device.
	 * </li>
	 * <li>
	 * Use <code>CeRapiInvoke</code> to call the dll on the device. The dll writes the value to the registry and also 
	 * returns it.  
	 * </li>
	 * </ol>
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("nls")
	public static String getDeviceId() throws IOException {
		return invoke("GetDeviceId", (byte[]) null, new InvokeRunnable<String>() {
			public String run(final Pointer buffer, final int size) {
				return buffer.getString(0, true);
			}
		});
	}

	@SuppressWarnings("nls")
	public static SystemPowerStateEx2 getDetailedSystemPowerStatus(final boolean update) throws IOException {
		final byte[] fUpdate = update ? new byte[] { 1 } : null;
		return invoke("GetSystemPowerStatusEx2", fUpdate, new InvokeRunnable<SystemPowerStateEx2>() {
			public SystemPowerStateEx2 run(final Pointer buffer, final int size) {
				return new SystemPowerStateEx2(buffer);
			}
		});
	}

	public static class SystemParametersInfo extends Structure {
		public static final int SPI_GETPLATFORMTYPE = 257;
		public static final int SPI_GETOEMINFO = 258;
		// Windows Mobile 5.0
		public static final int SPI_GETPROJECTNAME = 259;
		public static final int SPI_GETPLATFORMNAME = 260;
		public static final int SPI_GETBOOTMENAME = 261;
		public static final int SPI_GETPLATFORMMANUFACTURER = 262;
		public static final int SPI_GETUUID = 263;
		public static final int SPI_GETGUIDPATTERN = 264;

		public int structSize = size();
		public int uiAction;
		public int bufferSize;
		public boolean allocate;

		public SystemParametersInfo(final int action, final int size, final boolean allocate) {
			this.uiAction = action;
			this.bufferSize = size;
			this.allocate = allocate;
		}
	}

	public static String getSystemParametersInfoString(final int action, final String def) {
		try {
			return invoke("GetSystemParametersInfo", new SystemParametersInfo(action, 1024, true), //$NON-NLS-1$
					new InvokeRunnable<String>() {
						public String run(final Pointer buffer, final int size) {
							return buffer.getString(0, true);
						}
					});
		} catch (final IOException e) {
			//logger.error("Could not query system parameter", e); //$NON-NLS-1$
			return def;
		}
	}

	public static class RegistryKey {
		private HANDLE key;

		protected RegistryKey(final HANDLE key) {
			this.key = key;
		}

		public static RegistryKey create(final HKEY root, final String path) {
			final HANDLEByReference regHandle = new HANDLEByReference();
			if (W32Errors.NO_ERROR == Rapi.instance.CeRegOpenKeyEx(HKEY.HKEY_LOCAL_MACHINE, path, 0, 0, regHandle)) {
				return new RegistryKey(regHandle.getValue());
			}
			return null;
		}

		public void close() {
			if (this.key != null) {
				Rapi.instance.CeRegCloseKey(this.key);
				this.key = null;
			}
		}

		public String getValue(final String name, final String def) {
			if (this.key != null) {
				final IntByReference type = new IntByReference(); // SZ_STRING
				final IntByReference size = new IntByReference();
				if (Rapi.instance.CeRegQueryValueEx(this.key, name, null, type, null, size) == W32Errors.NO_ERROR) {
					//logger.trace("Registry type for {} is {}", name, type); //$NON-NLS-1$

					if (type.getValue() == 1) {
						final byte buffer[] = new byte[size.getValue()];
						if (Rapi.instance.CeRegQueryValueEx(this.key, name, null, type, buffer, size) == W32Errors.NO_ERROR) {

							try {
								return new String(buffer, 0, size.getValue() - 2, "UTF-16LE"); //$NON-NLS-1$
							} catch (final UnsupportedEncodingException e) {
							}
						}
					}
				}
				try {
					checkLastError();
				} catch (final IOException e) {

				}
			}
			return def;
		}

		@Override
		protected void finalize() throws Throwable {
			if (this.key != null) {
				Rapi.instance.CeRegCloseKey(this.key);
			}
			super.finalize();
		}
	}

	public static void runWithInit(final Runnable runnable) throws Exception {
		final Rapi.RapiInit init = new Rapi.RapiInit();
		W32API.HRESULT result = Rapi.instance.CeRapiInitEx(init);
		if (result.failed()) {
			final int waitResult = Kernel32.INSTANCE.WaitForSingleObject(init.heRapiInit, 1000);
			switch (waitResult) {
			case 0:
				result = init.hrRapiInit;
				break;
			case 0xffffffff:
				throw new IOException("Time out"); //$NON-NLS-1$
			default:
				throw new Exception(RapiHelper.getSystemError(Kernel32.INSTANCE.GetLastError()));
			}
		}
		if (result.failed() && result.intValue() != Rapi.CERAPI_E_ALREADYINITIALIZED) {
			Rapi.instance.CeRapiUninit();
			throw new Exception(RapiHelper.getSystemError(result.intValue()));
		}
		runnable.run();
	}
}