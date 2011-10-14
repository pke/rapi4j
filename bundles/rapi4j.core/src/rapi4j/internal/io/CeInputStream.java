package rapi4j.internal.io;

import java.io.IOException;
import java.io.InputStream;

import rapi4j.internal.RapiHelper;

import com.microsoft.rapi.Rapi;
import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.ptr.IntByReference;

/**
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 * TODO: Implement {@link #read(byte[])}
 */
public class CeInputStream extends InputStream {
	private final HANDLE fileHandle;
	private final IntByReference read = new IntByReference();
	private final byte buffer[] = new byte[1];
	private final String path;
	private int avail;

	@SuppressWarnings("nls")
	public CeInputStream(final String file) throws IOException {
		this.fileHandle = Rapi.instance.CeCreateFile(file, Kernel32.GENERIC_READ, Kernel32.FILE_SHARE_READ, null,
				Kernel32.OPEN_EXISTING, Kernel32.FILE_ATTRIBUTE_NORMAL, null);
		if (this.fileHandle == W32API.INVALID_HANDLE_VALUE) {
			RapiHelper.checkLastError("Could not open file " + file + " on the CE device");
		}
		this.path = file;
		this.avail = Rapi.instance.CeGetFileSize(this.fileHandle, null);
	}

	@Override
	public void close() throws IOException {
		Kernel32.INSTANCE.CloseHandle(this.fileHandle);
	}

	@SuppressWarnings("nls")
	@Override
	public int read() throws IOException {
		if (!Rapi.instance.CeReadFile(this.fileHandle, this.buffer, 1, this.read, null)) {
			RapiHelper.checkLastError("Could not read from file \"" + this.path + "\"");
		}
		this.avail -= this.read.getValue();
		return this.read.getValue() == 0 ? -1 : this.buffer[0];
	}

	@Override
	public int available() throws IOException {
		return this.avail;
	}
}