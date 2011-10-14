package com.microsoft.rapi;

import java.io.IOException;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.examples.win32.Kernel32;
import com.sun.jna.examples.win32.Kernel32.FILETIME;
import com.sun.jna.examples.win32.Kernel32.OVERLAPPED;
import com.sun.jna.examples.win32.W32API;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * Remote API for Windows CE devices.
 * 
 * <p>
 * For a complete reference please check:
 * <a href="http://msdn.microsoft.com/en-us/library/aa920177.aspx">Remote API in MSDN</a>
 * 
 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
 *
 */
public interface Rapi extends W32API {

	static Rapi instance = (Rapi) Native.loadLibrary("rapi", Rapi.class, DEFAULT_OPTIONS); //$NON-NLS-1$

	static public int CERAPI_E_ALREADYINITIALIZED = 0x80041001;

	/**
	 * Generic structure with its first member being the size of the complete structure.
	 * @author <a href="mailto:kursawe@topsystem.de">Philipp Kursawe</a>
	 *
	 */
	public static class SizeStructure extends Structure {
		public int dwSize = size();
	}

	/**
	 * Synchronously initializes the communication layers between the desktop and the target Windows Embedded CE-based 
	 * device. 
	 * 
	 * <p>
	 * CeRapiInit or {@link #CeRapiInitEx(RapiInit)} must be called before calling any of the other RAPI functions.
	 * @return
	 */
	HRESULT CeRapiInit();

	/**
	 * Contains information used to initialize a RAPI connection.
	 */
	class RapiInit extends Structure {
		/** Specifies the size of the RAPIINIT structure being passed in. */
		public int dwSize = size();

		/** Specifies a handle to an event. The event is set when the RAPI connection is made or an error occurs. */
		public HANDLE heRapiInit;

		/** Specifies the results, success or failure, of the RAPI connection. */
		public HRESULT hrRapiInit;
	}

	/**
	 * Asynchronously initializes the communication layers between the desktop and the target Windows Embedded CE-based 
	 * remote device. 
	 * <p>
	 * {@link #CeRapiInit} or <code>CeRapiInitEx</code> must be called before calling any of the other RAPI methods.
	 * @param init Initially, a handle to an event is passed back in the heRapiInit member of this structure. When the 
	 * connection is made or an error occurs, the event is set. The hrRapiInit member of this structure contains the 
	 * value <code>S_OK</code> if the connection is successful, or <code>E_FAIL</code> if an error occurred.
	 * @return <code>S_OK</code> indicates success. <code>E_FAIL</code> indicates failure. <code>E_INVALIDARG</code> 
	 * indicates that an invalid value was encountered. 
	 */
	HRESULT CeRapiInitEx(RapiInit init);

	/** Uninitializes a RAPI session.
	 *  
	 * <p>
	 * The CeRapiUnInit method should be called when the application has completed its use of the remote API services. 
	 * It gracefully closes down the connection to the Windows Embedded CE-based device. If called when not in an 
	 * initialized state, it will return <code>E_FAIL</code>.
	 * <p>
	 * <code>CeRapiUnInit</code> can be called during a call to {@link #CeRapiInit()} or {@link #CeRapiInitEx(RapiInit)}
	 * to cancel initialization.
	 * @return <code>E_FAIL</code> indicates that RAPI has not previously been initialized.
	 */
	int CeRapiUninit();

	/**
	 * Contains information about the current computer system. This includes the processor type, page size and memory 
	 * addresses.
	 */
	class SystemInfo extends Structure {
		/** System processor architecture */
		public short wProcessorArchitecture;

		/** Reserved for future use. */
		public short wReserved;

		/** Page size and the granularity of page protection and commitment. This is the page size used by the 
		 * VirtualAlloc function. */
		public int dwPageSize;

		/** Pointer to the lowest memory address accessible to applications and DLLs. */
		public Pointer lpMinimumApplicationAddress;

		/** Pointer to the highest memory address accessible to applications and DLLs. */
		public Pointer lpMaximumApplicationAddress;

		/** Mask representing the set of processors configured into the system. Bit 0 is processor 0; 
		 * bit 31 is processor 31. */
		public int dwActiveProcessorMask;

		/** Number of processors in the system. */
		public int dwNumberOfProcessors;

		/** Type of processor in the system. 
		 * <p>
		 * @deprecated
		 * Use the wProcessorArchitecture, wProcessorLevel, and wProcessorRevision 
		 * members to determine the type of processor. */
		@Deprecated
		public int dwProcessorType;

		/** The granularity with which virtual memory is allocated.
		 * <p>
		 * For example, a VirtualAlloc request to allocate 1 byte will reserve an address space of dwAllocationGranularity bytes. */
		public int dwAllocationGranularity;

		/** System architecture-dependent processor level. */
		public short wProcessorLevel;

		/** Specifies an architecture-dependent processor revision. The following table shows how the revision value is assembled for each type of processor architecture. */
		public short wProcessorRevision;
	}

	/**
	 * Returns information about the system on a remote Windows Embedded CE–based device.
	 * @param info structure to be filled in by this function.
	 */
	void CeGetSystemInfo(SystemInfo info);

	/**
	 * Contains information about the power status of the system.
	 */
	static class SystemPowerStatusEx extends Structure {
		public static final byte AC_LINE_OFFLINE = 0x00;
		public static final byte AC_LINE_ONLINE = 0x01;
		public static final byte AC_LINE_BACKUP_POWER = 0x2;
		public static final byte AC_LINE_UNKNOWN = (byte) 0xff;

		public static final byte BATTERY_FLAG_HIGH = 0x01;
		public static final byte BATTERY_FLAG_LOW = 0x02;
		public static final byte BATTERY_FLAG_CRITICAL = 0x04;
		public static final byte BATTERY_FLAG_CHARGING = 0x08;
		public static final byte BATTERY_FLAG_NO_BATTERY = (byte) 0x80;
		public static final byte BATTERY_FLAG_UNKNOWN = (byte) 0xff;
		public static final byte BATTERY_PERCENTAGE_UNKNOWN = (byte) 0xff;
		public static final int BATTERY_LIFE_UNKNOWN = 0xffffffff;

		public SystemPowerStatusEx() {
		}

		protected SystemPowerStatusEx(final Pointer p) {
			super(p);
		}

		/** AC power status. It is one of the AC_LINE_* values. */
		public byte ACLineStatus;

		/** Battery charge status. It can be a combination of BATTERY_FLAG_* values. */
		public byte BatteryFlag;

		/** Percentage of full battery charge remaining. This member can be a value in the range 0 to 100, or {@link #BATTERY_PERCENTAGE_UNKNOWN}(255) if status is unknown. All other values are reserved. */
		public byte BatteryLifePercent;

		/** Reserved; set to zero. */
		public byte Reserved1;

		/** Number of seconds of battery life remaining, or {@link #BATTERY_LIFE_UNKNOWN}(0xffffffff) if remaining seconds are unknown. */
		public int BatteryLifeTime;

		/** Number of seconds of battery life when at full charge, or {@link #BATTERY_LIFE_UNKNOWN}(0xFFFFFFFF) if full lifetime is unknown. */
		public int BatteryFullLifeTime;

		/** Reserved; set to zero. */
		public byte Reserved2;

		/** Backup battery charge status. It is <b>one</b> of the BATTERY_FLAG_* values. */
		public byte BackupBatteryFlag;

		/** Percentage of full backup battery charge remaining. Must be in the range 0 to 100, or {@link #BATTERY_PERCENTAGE_UNKNOWN}(255). */
		public byte BackupBatteryLifePercent;

		/** Reserved; set to zero. */
		public byte Reserved3;

		/** Number of seconds of backup battery life remaining, or BATTERY_LIFE_UNKNOWN if remaining seconds are unknown. */
		public int BackupBatteryLifeTime;

		/** Number of seconds of backup battery life when at full charge, or BATTERY_LIFE_UNKNOWN if full lifetime is unknown. */
		public int BackupBatteryFullLifeTime;
	}

	/**
	 * Retrieves the power status of the system on a remote Windows Embedded CE–based device. 
	 * <p>
	 * The status indicates whether the system is running on AC or DC power, whether or not the batteries are currently 
	 * charging, and the remaining life of main and backup batteries.
	 * @param status structure receiving the power status information.
	 * @param update if set to <code>true</code> then get the latest information from the device driver, otherwise 
	 * retrieve cached information that maybe out-of-date by several seconds.
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 * 
	 * @remarks
	 * Not supported for emulation.
	 */
	boolean CeGetSystemPowerStatusEx(SystemPowerStatusEx status, boolean update);

	/**
	 * Contains information about the power status of the system.
	 * <p>
	 * This is used in tsrapiex.dll GetSystemPowerStatusEx2.
	 * @see <a href="http://msdn.microsoft.com/en-us/library/ms941842.aspx">MSDN</a>
	 * @since 2.12 and later
	 */
	class SystemPowerStateEx2 extends Structure {
		public SystemPowerStateEx2(final Pointer p) {
			super(p, ALIGN_NONE);
			read();
		}

		public SystemPowerStateEx2() {
		}

		// public SystemPowerStatusEx base;
		/** AC power status. It is one of the AC_LINE_* values. */
		public byte ACLineStatus;

		/** Battery charge status. It can be a combination of BATTERY_FLAG_* values. */
		public byte BatteryFlag;

		/** Percentage of full battery charge remaining. This member can be a value in the range 0 to 100, or {@link #BATTERY_PERCENTAGE_UNKNOWN}(255) if status is unknown. All other values are reserved. */
		public byte BatteryLifePercent;

		/** Reserved; set to zero. */
		public byte Reserved1;

		/** Number of seconds of battery life remaining, or {@link #BATTERY_LIFE_UNKNOWN}(0xffffffff) if remaining seconds are unknown. */
		public int BatteryLifeTime;

		/** Number of seconds of battery life when at full charge, or {@link #BATTERY_LIFE_UNKNOWN}(0xFFFFFFFF) if full lifetime is unknown. */
		public int BatteryFullLifeTime;

		/** Reserved; set to zero. */
		public byte Reserved2;

		/** Backup battery charge status. It is <b>one</b> of the BATTERY_FLAG_* values. */
		public byte BackupBatteryFlag;

		/** Percentage of full backup battery charge remaining. Must be in the range 0 to 100, or {@link #BATTERY_PERCENTAGE_UNKNOWN}(255). */
		public byte BackupBatteryLifePercent;

		/** Reserved; set to zero. */
		public byte Reserved3;

		/** Number of seconds of backup battery life remaining, or BATTERY_LIFE_UNKNOWN if remaining seconds are unknown. */
		public int BackupBatteryLifeTime;

		/** Number of seconds of backup battery life when at full charge, or BATTERY_LIFE_UNKNOWN if full lifetime is unknown. */
		public int BackupBatteryFullLifeTime;

		/** Number of millivolts (mV) of battery voltage. It can range from 0 to 65535. */
		public int BatteryVoltage;

		/** Number of milliamps (mA) of instantaneous current drain. It can range from 0 to 32767 for charge and 0 to –32768 for discharge. */
		public int BatteryCurrent;

		/** Average number of milliamps of short term device current drain. It can range from 0 to 32767 for charge and 0 to –32768 for discharge. */
		public int BatteryAverageCurrent;

		/** Number of milliseconds (mS) that is the time constant interval used in reporting BatteryAverageCurrent. */
		public int BatteryAverageInterval;

		/** Average number of milliamp hours (mAh) of long-term cumulative average discharge. It can range from 0 to –32768. This value is reset when the batteries are charged or changed. */
		public int BatterymAHourConsumed;

		/** Battery temperature reported in 0.1 degree Celsius increments. It can range from –3276.8 to 3276.7. */
		public int BatteryTemperature;

		/** Number of millivolts (mV) of backup battery voltage. It can range from 0 to 65535. */
		public int BackupBatteryVoltage;

		public final static byte BATTERY_CHEMISTRY_ALKALINE = 0x01;
		public final static byte BATTERY_CHEMISTRY_NICD = 0x02;
		public final static byte BATTERY_CHEMISTRY_NIMH = 0x03;
		public final static byte BATTERY_CHEMISTRY_LION = 0x04;
		public final static byte BATTERY_CHEMISTRY_LIPOLY = 0x05;
		public final static byte BATTERY_CHEMISTRY_ZINCAIR = 0x06;
		public final static byte BATTERY_CHEMISTRY_UNKNOWN = (byte) 0xFF;

		/** Type of battery. It can be one of the BATTERY_CHEMISTRY_* values */
		public byte BatteryChemistry;
	}

	/** 
	 * @see http://msdn.microsoft.com/en-us/library/ms931202.aspx
	 */
	class MEMORYSTATUS extends Structure {
		public int dwLength = size();
		public int dwMemoryLoad;
		public int dwTotalPhys;
		public int dwAvailPhys;
		public int dwTotalPageFile;
		public int dwAvailPageFile;
		public int dwTotalVirtual;
		public int dwAvailVirtual;
	}

	/**
	 * Gets information on the physical and virtual memory of the system.
	 * @param status
	 * @see http://msdn.microsoft.com/en-us/library/ms908470.aspx
	 */
	void CeGlobalMemoryStatus(MEMORYSTATUS status);

	/**
	 * Frees the memory on the desktop computer allocated by a call to CeFindAllDatabases, CeFindAllFiles, or CeReadRecordProps
	 * 
	 * @remarks
	 * Any memory allocated on the desktop computer by a RAPI method call must be freed by calling the CeRapiFreeBuffer function. 
	 * 
	 * @param Buffer buffer to be freed. 
	 * @return S_OK indicates success. E_FAIL indicates failure.
	 */
	HRESULT CeRapiFreeBuffer(Pointer Buffer);

	static class CeOsVersionInfo extends Structure {
		public int dwOSVersionInfoSize = size();
		public int dwMajorVersion;
		public int dwMinorVersion;
		public int dwBuildNumber;
		public int dwPlatformId;
		public String szCSDVersion;

		public static CeOsVersionInfo get() throws IOException {
			final CeOsVersionInfo info = new CeOsVersionInfo();
			if (instance.CeGetVersionEx(info)) {
				return info;
			}
			return info;
		}
	}

	/**
	 * Obtains extended information about the version of the operating system that is currently running on a remote 
	 * Windows Embedded CE–based device.
	 * @param info structure that the function fills with operating system version information.
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 */
	boolean CeGetVersionEx(CeOsVersionInfo info);

	/**
	 * Reports errors that occur during RAPI method calls.
	 * @return HRESULT with errors that are be specific to RAPI function calls. This function should be used in 
	 * conjunction with CeGetLastError that checks for errors that are not related to RAPI.
	 */
	HRESULT CeRapiGetError();

	/**
	 * Returns the calling thread's last-error code value related to RAPI function calls.
	 * @return The calling thread's last-error code value indicates success. Functions set this value by calling the 
	 * SetLastError function. The Return Value section of each reference page notes the conditions under which the 
	 * function sets the last-error code. 
	 */
	int CeGetLastError();

	static final int FILE_ATTRIBUTE_INROM = 0x00000040;
	static final int FILE_ATTRIBUTE_ROMSTATICREF = 0x00001000;
	static final int FILE_ATTRIBUTE_ROMMODULE = 0x00002000;
	static final int FILE_ATTRIBUTE_HAS_CHILDREN = 0x00010000;
	static final int FILE_ATTRIBUTE_SHORTCUT = 0x00020000;

	/**
	 * Returns attributes for a specified file or directory on a remote Windows Embedded CE–based device.
	 * 
	 * @param lpFileName specifies the name of a file or directory.
	 * @return an int containing the attributes of the specified file or directory indicates success. 0xFFFFFFFF indicates failure. To get extended error information, call CeGetLastError and CeRapiGetError. 
	 */
	int CeGetFileAttributes(String lpFileName);

	boolean CeGetFileTime(HANDLE hFile, FILETIME lpCreationTime, FILETIME lpLastAccessTime, FILETIME lpLastWriteTime);

	HANDLE CeCreateFile(String lpFileName, int dwDesiredAccess, int dwShareMode,
			Kernel32.SECURITY_ATTRIBUTES lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes,
			HANDLE hTemplateFile);

	/**
	 * Reads data from a file on a remote Windows Embedded CE–based device. 
	 * 
	 * <p>
	 * The read operation starts the position indicated by the file pointer. After the read operation has been 
	 * completed, the file pointer is adjusted by the number of bytes actually read.
	 * 
	 * @param hFile Handle to the file to be read. The file handle must have been created with GENERIC_READ access to 
	 * the file. This parameter cannot be a socket handle.
	 * @param lpBuffer buffer that receives the data read from the file.
	 * @param nNumberOfBytesToRead Number of bytes to be read from the file.
	 * @param lpNumberOfBytesRead Number of bytes read. CeReadFile sets this value to zero before doing any work or error checking.
	 * @param lpOverlapped Unsupported; set to <code>null</code>. 
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 */
	boolean CeReadFile(HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToRead, IntByReference lpNumberOfBytesRead,
			OVERLAPPED lpOverlapped);

	/**
	 * Writes data to a file on a remote Windows Embedded CE–based device. 
	 * 
	 * <p>
	 * CeWriteFile starts writing data to the file at the position indicated by the file pointer. After the write operation has been completed, the file pointer is adjusted by the number of bytes actually written.
	 * 
	 * @param hFile Handle to the file to be written to. The file handle must have been created with GENERIC_WRITE access to the file.
	 * @param lpBuffer buffer containing the data to be written to the file. 
	 * @param nNumberOfBytesToWrite Number of bytes to write to the file. A value of zero specifies a null write operation. A null write operation does not write any bytes but does cause the time stamp to change. CeWriteFile does not truncate or extend the file. To truncate or extend a file, use the CeSetEndOfFile function. 
	 * @param lpNumberOfBytesWritten number of bytes written by this function call. CeWriteFile sets this value to zero before doing any work or error checking.
	 * @param lpOverlapped Unsupported; set to NULL. 
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 * @remarks
	 * If part of the file is locked by another process and the write operation overlaps the locked portion, this function fails.
	 * Accessing the output buffer while a write operation is using the buffer may lead to corruption of the data written from that buffer. Applications must not read from, write to, reallocate, or free the output buffer that a write operation is using until the write operation completes. 
	 */
	boolean CeWriteFile(HANDLE hFile, byte[] lpBuffer, int nNumberOfBytesToWrite,
			IntByReference lpNumberOfBytesWritten, OVERLAPPED lpOverlapped);

	/**
	 * Retrieves the date and time that a file was created, last accessed, and last modified for a file on a remote Windows Embedded CE–based device.
	 * 
	 * @param hFile Open handle of the file whose size is being returned. The handle must have been created with either 
	 * GENERIC_READ or GENERIC_WRITE access to the file.
	 * @param lpFileSizeHigh variable where the high-order word of the file size is returned. 
	 * This parameter can be <code>null</code> if the application does not require the high-order word.
	 * @return If the function succeeds, the return value is the low-order DWORD of the file size, and, if lpFileSizeHigh is non-null, the function puts the high-order doubleword of the file size into the variable pointed to by that parameter.
	 * If the function fails and lpFileSizeHigh is NULL, the return value is INVALID_FILE_SIZE. To get extended error information, call CeGetLastError and CeRapiGetError.
	 * If the function fails and lpFileSizeHigh is non-NULL, the return value is INVALID_FILE_SIZE and CeGetLastError will return a value other than NO_ERROR.
	 */
	int CeGetFileSize(HANDLE hFile, IntByReference lpFileSizeHigh);

	/**
	 * Closes an open object handle that was created using RAPI functions.
	 * @param hObject Handle to an open object.
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}. 
	 */
	boolean CeCloseHandle(HANDLE hObject);

	/**
	 * Deletes an existing file from the object store on a remote Windows Embedded CE–based device.
	 * 
	 * @param lpFileName specifies the file to be deleted. 
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 */
	boolean CeDeleteFile(String lpFileName);

	/**
	 * Deletes an existing empty directory on a remote Windows Embedded CE–based device.
	 * 
	 * @param lpPathName specifies the path of the directory to be removed. The path must specify an empty directory, and the calling process must have write access to the directory. 
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 */
	boolean CeRemoveDirectory(String lpPathName);

	/**
	 * Can be used as a general-purpose mechanism to remotely execute a routine on a remote Windows Embedded CE-based device.
	 * 
	 * @param dllPath containing the name of a DLL that is on the Windows Embedded CE-based device and that contains the function identified in <code>functionName</code>.
	 * @param functionName the name of the function that RAPI should call on the Windows Embedded CE-based device. 
	 * @param cbInput Number of bytes in the input buffer <code>pInput</code>.
	 * @param pInput buffer containing the input data.
	 * @param outBuffer variable that is set to the location of the output buffer upon return.
	 * @param outBufferSize variable that is set to the number of bytes in the output buffer ppOutput when the function returns. 
	 * @param ppIRAPIStream <b>not used!</b>
	 * @param dwReserved 
	 * @return If RAPI services on the Windows Embedded CE-based device successfully locate and call the client function, 
	 * then in Block Mode the return value is that which is returned on the Windows Embedded CE-based device by the 
	 * called function. If the function was not called successfully, or an exception occurred during its execution, an 
	 * error code is returned.
	 */
	HRESULT CeRapiInvoke(String dllPath, String functionName, int cbInput, Pointer pInput, IntByReference outBuffer,
			PointerByReference outBufferSize, PointerByReference ppIRAPIStream, int dwReserved);

	static class CE_FIND_DATA extends Structure {
		public int dwFileAttributes;
		public FILETIME ftCreationTime;
		public FILETIME ftLastAccessTime;
		public FILETIME ftLastWriteTime;
		public int nFileSizeHigh;
		public int nFileSizeLow;
		public int dwOID;
		public char cFileName[] = new char[260];

		public CE_FIND_DATA() {
		}

		protected CE_FIND_DATA(final Pointer p) {
			super(p);
		}

		static public CE_FIND_DATA[] createArray(final Pointer p, final int size) {
			if (null == p) {
				return new CE_FIND_DATA[0];
			}
			final CE_FIND_DATA data = new CE_FIND_DATA();
			data.useMemory(p);
			data.read();
			return (CE_FIND_DATA[]) data.toArray(size);
		}

		public long getFileSize() {
			return nFileSizeHigh << 32 | nFileSizeLow & 0xffffffffL;
		}
	}

	static final int FAF_ATTRIBUTES = 0x01;
	static final int FAF_CREATION_TIME = 0x02;
	static final int FAF_LASTACCESS_TIME = 0x04;
	static final int FAF_LASTWRITE_TIME = 0x08;
	static final int FAF_SIZE_HIGH = 0x10;
	static final int FAF_SIZE_LOW = 0x20;
	static final int FAF_OID = 0x40;
	static final int FAF_NAME = 0x80;
	static final int FAF_FLAG_COUNT = 8;
	static final int FAF_ATTRIB_CHILDREN = 0x01000;
	static final int FAF_ATTRIB_NO_HIDDEN = 0x02000;
	static final int FAF_FOLDERS_ONLY = 0x04000;
	static final int FAF_NO_HIDDEN_SYS_ROMMODULES = 0x08000;
	static final int FAF_GETTARGET = 0x10000;

	/**
	 * Retrieves information about all files and directories in the given directory of the Windows Embedded CE object store on a remote device.
	 * 
	 * @param szPath contains the name of the path in which to search for files.
	 * @param dwFlags Combination of filter and retrieval flags. The filter flags specify what kinds of files to document, and the retrieval flags specify which members of the CE_FIND_DATA structure to retrieve.
	 * @param lpdwFoundCount receives a count of the items found.
	 * @param ppFindDataArray array of CE_FIND_DATA structures that receive information about the found items. It is the application's responsibility to free the memory used by the array. To free the memory you must call CeRapiFreeBuffer.
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 */
	boolean CeFindAllFiles(String szPath, int dwFlags, IntByReference lpdwFoundCount, PointerByReference ppFindDataArray);

	/**
	 * Searches for a file or sub-directory in a directory on a remote Windows Embedded CE–based device.
	 * 
	 * @param lpFileName string that specifies a valid directory or path and file name, which can contain wildcard characters (* and ?). 
	 * @param lpFindFileData structure that receives information about the found file or subdirectory. 
	 * @return A valid search handle indicates success. INVALID_HANDLE_VALUE indicates failure. The returned handle can be used in subsequent calls to CeFindNextFile or CeFindClose. To get extended error information, call CeGetLastError and CeRapiGetError. 
	 */
	HANDLE CeFindFirstFile(String lpFileName, CE_FIND_DATA lpFindFileData);

	/**
	 * Retrieves the next file in an enumeration context.
	 *  
	 * @param hFindFile Search handle returned by a previous call to the CeFindFirstFile function. 
	 * @param lpFindFileData structure that receives information about the found file or subdirectory. The structure can be used in subsequent calls to CeFindNextFile to refer to the found file or directory.
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 */
	boolean CeFindNextFile(HANDLE hFindFile, CE_FIND_DATA lpFindFileData);

	/**
	 * Closes the specified search handle on a remote Windows Embedded CE–based device. 
	 * <p>
	 * The CeFindFirstFile and CeFindNextFile methods use a search handle to locate files.
	 * @param hFindFile Search handle. This handle must have been previously opened by the CeFindFirstFile function.
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 * @remarks
	 * After the CeFindClose function is called, the handle specified by the hFindFile parameter cannot be used in subsequent calls to either the CeFindNextFile or CeFindClose function. 
	 */
	boolean CeFindClose(HANDLE hFindFile);

	public static class HKEY extends HANDLE {
		public HKEY() {
		}

		public HKEY(final Pointer p) {
			super(p);
		}

		public static final HKEY HKEY_CLASSES_ROOT = new HKEY(Pointer.createConstant(0x80000000));
		public static final HKEY HKEY_CURRENT_USER = new HKEY(Pointer.createConstant(0x80000001));
		public static final HKEY HKEY_LOCAL_MACHINE = new HKEY(Pointer.createConstant(0x80000002));
		public static final HKEY HKEY_USERS = new HKEY(Pointer.createConstant(0x80000003));
	}

	/**
	 * Opens the specified registry key on a remote Windows Embedded CE–based device.
	 * 
	 * @param hKey TODO: Handle to a currently open key or any of the following predefined reserved handle values:
	 * @param lpszSubKey name of the subkey to open. If this parameter is NULL or a pointer to an empty string, the function will open a new handle to the key identified by the hKey parameter. In this case, the function will not close the handles previously opened.
	 * @param ulOptions Reserved; set to 0. 
	 * @param samDesired Not supported; set to zero. 
	 * @param phkResult variable that receives a handle to the opened key. When you no longer need the returned handle, call the CeRegCloseKey function to close it.
	 * @return
	 */
	int CeRegOpenKeyEx(HANDLE hKey, String lpszSubKey, int ulOptions, int samDesired, HANDLEByReference phkResult);

	/**
	 * Retrieves the type and data for a specified value name associated with an open registry key on a remote Windows Embedded CE–based device.
	 * 
	 * @param hKey Handle to a currently open key or any of the following predefined reserved handle values: 
	 * @param lpValueName ame of the value to query. If this parameter is NULL or an empty string, the function retrieves the type and data for the key's unnamed value. A registry key does not automatically have an unnamed or default value. Unnamed values can be of any type.
	 * @param lpReserved Reserved; set to 0. 
	 * @param lpType TODO: receives the type of data associated with the specified value. The following table shows the possible values that lpType can returnThe value returned through this parameter will be one of the following.
	 * @param lpData buffer that receives the value's data. This parameter can be NULL if the data is not required.
	 * @param lpcbData specifies the size, in bytes, of the buffer pointed to by the lpData parameter. When the function returns, this variable contains the size of the data copied to lpData.
	 * @return
	 */
	int CeRegQueryValueEx(HANDLE hKey, String lpValueName, IntByReference lpReserved, IntByReference lpType,
			byte[] lpData, IntByReference lpcbData);

	/**
	 * Releases the handle of the specified registry key on a remote Windows Embedded CE–based device.
	 * 
	 * @param hKey Handle to the open key to close. 
	 * @return
	 */
	int CeRegCloseKey(HANDLE hKey);

	/**
	 * Creates a new directory on a remote Windows Embedded CE–based device.
	 * @param lpPathName specifies the path of the directory to be created.
	 * @param lpSecurityAttributes Ignored; set to <code>null</code>.
	 * @return success. To get extended error information, call {@link #CeGetLastError} and {@link #CeRapiGetError}.
	 */
	boolean CeCreateDirectory(String lpPathName, Kernel32.SECURITY_ATTRIBUTES lpSecurityAttributes);
}