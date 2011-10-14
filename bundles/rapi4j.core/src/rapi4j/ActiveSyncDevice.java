package rapi4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import com.microsoft.rapi.Rapi;
import com.microsoft.rapi.Rapi.MEMORYSTATUS;

/**
 * ActiveSync connected mobile device.
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
@SuppressWarnings("nls")
public interface ActiveSyncDevice {
	public static final String DEVICE_NAME = "device.name";
	public static final String DEVICE_PLATFORM = "device.platform";
	public static final String DEVICE_OEM = "device.oem";
	public static final String OS_VERSION = "os.version";
	public static final String DEVICE_ID = "device.id";

	Rapi.CeOsVersionInfo getVersionInfo() throws IOException;

	Rapi.SystemInfo getSystemInfo() throws IOException;

	Rapi.SystemPowerStateEx2 getPowerStatus() throws IOException;

	String getId();

	String getName();

	interface CopyListener {

	}

	/**
	 * Creates an InputStream for a given file from the device.
	 * 
	 * @param path
	 * @return
	 * @throws IOException if the stream could not be opened on the device.
	 */
	InputStream createInputStream(String path) throws IOException;

	interface FileInfo {
		String getName();

		boolean isDirectory();

		Date getCreatedAt();

		Date getWrittenAt();

		Date getAccessedAt();

		long getSize();
	}

	/**
	 * Used for {@link ActiveSyncDevice#findFiles(String, FileVisitor)}
	 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
	 *
	 */
	interface FileVisitor<Result> {
		Result found(FileInfo fileInfo);
	}

	/**
	 * 
	 * @param <Result> type to return from the method.
	 * @param spec
	 * @param visitor
	 * @return
	 */
	<Result> Result findFiles(String spec, FileVisitor<Result> visitor);

	/**
	 * Convenient method for copying data to the device.
	 *  
	 * @param stream
	 * @param targetFilePath
	 * @throws IOException if an error occurs during copying.
	 */
	void copyStream(InputStream stream, String targetFilePath) throws IOException;

	/**
	 * Reboots the device.
	 * <p>
	 * Naturally this will make the device disappear from the system. It will re-appear after it has booted itself again.
	 * @throws IOException
	 */
	public void reboot() throws IOException;

	/**
	 * Deletes the given file from the device. 
	 * @param filePath
	 */
	void deleteFile(String filePath);

	/**
	 * Will <strong>recursively</strong> remove the directory.
	 * @param path
	 */
	void removeDirectory(String path);

	/**
	 * Creates the directory.
	 * @param path
	 */
	void createDirectory(String path);

	/**
	 * @return the memory status information of the device.
	 * @throws IOException
	 */
	MEMORYSTATUS getMemoryStatus() throws IOException;
}
