package rapi4j.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import rapi4j.ActiveSyncDevice;
import rapi4j.internal.io.CeInputStream;
import rapi4j.internal.io.CeOutputStream;

import com.microsoft.rapi.Rapi;
import com.microsoft.rapi.Rapi.CE_FIND_DATA;
import com.microsoft.rapi.Rapi.MEMORYSTATUS;
import com.sun.jna.Native;
import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class ActiveSyncDeviceImpl implements ActiveSyncDevice {

	private Map<String, Object> config;

	protected void activate(final Map<String, Object> config) {
		this.config = config;
	}

	public void dispose() {
		Rapi.instance.CeRapiUninit();
	}

	public String getName() {
		return (String) this.config.get(DEVICE_NAME);
	}

	public String getId() {
		return (String) this.config.get(DEVICE_ID);
	}

	@Override
	public String toString() {
		return "Device " + getId(); //$NON-NLS-1$
	}

	public Rapi.CeOsVersionInfo getVersionInfo() throws IOException {
		return null;
	}

	public Rapi.SystemInfo getSystemInfo() throws IOException {
		return RapiHelper.getSystemInfo();
	}

	public Rapi.SystemPowerStateEx2 getPowerStatus() throws IOException {
		return RapiHelper.getDetailedSystemPowerStatus(false);
	}

	public void copyStream(final InputStream stream, final String targetFilePath) throws IOException {
		RapiHelper.copyStream(stream, targetFilePath);
	}

	public InputStream createInputStream(final String path) throws IOException {
		return new CeInputStream(path);
	}

	public OutputStream createOutputStream(final String path) throws IOException {
		return new CeOutputStream(path);
	}

	public <Result> Result findFiles(final String spec, final FileVisitor<Result> visitor) {
		final File file = new File(spec);
		final String pathspec = file.getParent() != null ? file.getParent() : file.getPath();
		final String path = pathspec.replace('\\', '/');
		Result result = null;

		final IntByReference count = new IntByReference();
		final PointerByReference datas = new PointerByReference();
		try {
			if (Rapi.instance.CeFindAllFiles(spec, Rapi.FAF_ATTRIBUTES | Rapi.FAF_CREATION_TIME
					| Rapi.FAF_LASTACCESS_TIME | Rapi.FAF_LASTWRITE_TIME | Rapi.FAF_NAME
					| Rapi.FAF_NO_HIDDEN_SYS_ROMMODULES | Rapi.FAF_SIZE_HIGH | Rapi.FAF_SIZE_LOW, count, datas)) {
				for (final CE_FIND_DATA findData : CE_FIND_DATA.createArray(datas.getValue(), count.getValue())) {
					result = visitor.found(new FileInfo() {
						private final Date createdAt = findData.ftCreationTime.toDate();
						private final Date accessedAt = findData.ftLastAccessTime.toDate();
						private final Date writtenAt = findData.ftLastWriteTime.toDate();

						@SuppressWarnings("nls")
						private final String name = (path == null || path.equals("/") ? "" : path) + "/"
								+ Native.toString(findData.cFileName);

						public boolean isDirectory() {
							return (findData.dwFileAttributes & Kernel32.FILE_ATTRIBUTE_DIRECTORY) == Kernel32.FILE_ATTRIBUTE_DIRECTORY;
						}

						public String getName() {
							return this.name;
						}

						public Date getWrittenAt() {
							return this.writtenAt;
						}

						public Date getAccessedAt() {
							return this.accessedAt;
						}

						public Date getCreatedAt() {
							return this.createdAt;
						}

						public long getSize() {
							return findData.getFileSize();
						}
					});
					if (result != null) {
						return result;
					}
				}
			}
		} finally {
			Rapi.instance.CeRapiFreeBuffer(datas.getValue());
			// RapiHelper.checkLastError();
		}
		return result;
	}

	public void reboot() throws IOException {
		RapiHelper.invoke("ResetDevice"); //$NON-NLS-1$
	}

	public void deleteFile(final String filePath) {
		Rapi.instance.CeDeleteFile(filePath);
	}

	public void removeDirectory(final String path) {
		Rapi.instance.CeRemoveDirectory(path);
	}

	public void createDirectory(final String path) {
		Rapi.instance.CeCreateDirectory(path, null);
	}

	public MEMORYSTATUS getMemoryStatus() throws IOException {
		final MEMORYSTATUS status = new MEMORYSTATUS();
		Rapi.instance.CeGlobalMemoryStatus(status);
		RapiHelper.checkRapiError("Could not query memory status"); //$NON-NLS-1$
		return status;
	}
}