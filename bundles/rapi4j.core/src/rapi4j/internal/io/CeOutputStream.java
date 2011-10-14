package rapi4j.internal.io;

import java.io.IOException;
import java.io.OutputStream;

import rapi4j.internal.RapiHelper;

import com.microsoft.rapi.Rapi;
import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.examples.win32.W32API.HANDLE;
import com.sun.jna.ptr.IntByReference;

/**
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 * TODO: Implement {@link #write(byte[])}
 */
public class CeOutputStream extends OutputStream {
	private final HANDLE fileHandle;
	private final IntByReference written = new IntByReference();
	private final String path;

	@SuppressWarnings("nls")
	public CeOutputStream(final String file) throws IOException {
		this.fileHandle = Rapi.instance.CeCreateFile(file, Kernel32.GENERIC_WRITE, Kernel32.FILE_SHARE_WRITE, null,
				Kernel32.CREATE_ALWAYS, Kernel32.FILE_ATTRIBUTE_NORMAL, null);
		if (this.fileHandle == W32API.INVALID_HANDLE_VALUE) {
			RapiHelper.checkLastError("Could not create file " + file + " on the CE device");
		}
		this.path = file;
	}

	@Override
	public void close() throws IOException {
		Kernel32.INSTANCE.CloseHandle(this.fileHandle);
	}

	@SuppressWarnings("nls")
	@Override
	public void write(final int b) throws IOException {
		if (!Rapi.instance.CeWriteFile(this.fileHandle, new byte[] { (byte) b }, 1, this.written, null)) {
			RapiHelper.checkLastError("Could not write to file \"" + this.path + "\"");
		}
	}

	@SuppressWarnings("nls")
	@Override
	public void write(final byte[] b) throws IOException {
		if (!Rapi.instance.CeWriteFile(this.fileHandle, b, b.length, this.written, null)) {
			RapiHelper.checkLastError("Could not write to file \"" + this.path + "\"");
		}
	}
}