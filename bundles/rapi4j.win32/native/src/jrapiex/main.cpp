#include <windows.h>
#include <winioctl.h>

#include "jrapi.h"
//#include <regext.h> Win Ce 6.0
#include "DynamicFunction.h"

struct Logger {
  void debug(const char* topic, ...);  
};


extern "C" __declspec(dllimport) BOOL KernelIoControl(DWORD dwIoControlCode, LPVOID lpInBuf, DWORD nInBufSize, LPVOID lpOutBuf, DWORD nOutBufSize, LPDWORD lpBytesReturned);

namespace rapi {
  // resolve conflict between coredll@GetSystemPowerStatusEx2 and this

/**
  Gets the systems extended power status (incl. battery type).

  @param cbInput [in] 0 if no BYTE is stored in pInput, or 1 if there is.
  @param pInput [in] BYTE that describes the fUpdate parameter to coredll
  @c GetSystemPowerStatusEx2 function.
  @param pcbOutput [out] size of SYSTEM_POWER_STATUS_EX2 returned in 
  @c ppOutput
  @param ppOutput [out] SYSTEM_POWER_STATUS_EX2
  @param pStream [in] <i>not used</i>

  @return S_OK or an error, if no memory for @c ppOutput could be allocated.
*/
RAPI_INVOKE(GetSystemPowerStatusEx2)
  BOOL fUpdate = false;
  // pInput can contain the boolean flag for fUpdate, if it is a byte
  // Only the lowest bit is evaluated
#if 0
  if (pInput && cbInput == 1 && !IsBadReadPtr(pInput, cbInput)) {
#if _DEBUG
    MessageBeep(1);
#endif
    fUpdate = *pInput;
  }
#endif

  static win32::DynamicFunction<DWORD (__stdcall *)(
    PSYSTEM_POWER_STATUS_EX2 pSystemPowerStatusEx2,
    DWORD dwLen, 
    BOOL fUpdate)>func(L"coredll", L"GetSystemPowerStatusEx2");

  if (func) {
    SYSTEM_POWER_STATUS_EX2* pstatus = allocOutput<SYSTEM_POWER_STATUS_EX2>(pcbOutput, ppOutput);
    if (pstatus && (*func)(pstatus, *pcbOutput, fUpdate)) {
      return S_OK;
    }
  } else {
    return E_NOTIMPL;
  }
RAPI_INVOKE_RETURN_LAST_ERROR()

RAPI_INVOKE(GetSystemParametersInfo)
  SystemParametersInfoInput* input = (SystemParametersInfoInput*)pInput;
  
  if (pInput && cbInput >= sizeof(SystemParametersInfoInput) && !IsBadReadPtr(pInput, cbInput) && input->structSize >= sizeof(*input)) {
    LPVOID pvParam = (input->allocate) ? allocOutput<void>(pcbOutput, ppOutput, input->uiParam) : 0;
    if (SystemParametersInfoW(input->uiAction, input->uiParam, pvParam, 0)) {
      return S_OK;
    }
  } else {
    return E_INVALIDARG;
  }
RAPI_INVOKE_RETURN_LAST_ERROR()

}

/**
  Performs a soft-reset on the device (reboot).

  @par Remarks
  Please note, that this function might not return.

  @param cbInput [in] Size of the data in @c pInput or 0 if no such data is
  supplied.
  @param pInput [in] <i>not used</i>
  @param pcbOutput [out] <i>not used</i>
  @param ppOutput [out] <i>not used</i>
  @param pStream [in] <i>not used</i>

  @return E_NOTIMPL if the SetSystemPowerState function could not be found 
  in the devices <code>coredll.dll</code>. Please note, that if the 
  function could be found then this function might not return at all, as 
  the reset will already take place.
*/
RAPI_INVOKE(ResetDevice)
  static win32::DynamicFunction<DWORD (__stdcall *)(
      LPCWSTR psNtate,
      DWORD dwFlags, 
      DWORD dwOptions)>SetSystemPowerState(L"coredll", L"SetSystemPowerState");

  enum {
    POWER_STATE_RESET = 0x800000,
  };

  if (SetSystemPowerState) {
    (*SetSystemPowerState)(0, POWER_STATE_RESET, 0);
    return S_OK;
  }
RAPI_INVOKE_RETURN(E_NOTIMPL)


/**
  Retrieves the devices (unique) ID.

  @param cbInput [in] Size of the data in @c pInput or 0 if no such data is
  supplied.
  @param pInput [in] <i>not used</i>
  @param pcbOutput [out] the length of the Wide-String in @ppOutput
  @param ppOutput [out] A null terminated Wide-String with the device id in
  GUID format.
  @param pStream [in] <i>not used</i>

  @return S_OK or an error, if no memory for @c ppOutput could be allocated 
  or an error calling <code>KernelIoControl</code> occured.
*/
RAPI_INVOKE(GetDeviceId)
#if 1
  static win32::DynamicFunction<HRESULT (__stdcall *)(
    LPBYTE pbApplicationData,
    DWORD cbApplictionData,
    DWORD dwDeviceIDVersion,
    LPBYTE pbDeviceIDOutput,
    DWORD* pcbDeviceIDOutput
  )>GetDeviceUniqueID(L"coredll", L"GetDeviceUniqueID");

  // First try to call new version
  if (GetDeviceUniqueID) {
    DWORD size = 0;
    HRESULT result = (*GetDeviceUniqueID)(pInput, cbInput, 1, 0, &size);
    if (result == HRESULT_FROM_WIN32(ERROR_INSUFFICIENT_BUFFER)) {      
      result = (*GetDeviceUniqueID)(pInput, cbInput, 1, rapi::allocOutput<BYTE>(pcbOutput, ppOutput, size), &size);
    }
    return result;
  }
#endif

  struct DEVICE_ID {
      int dwSize;
      int dwPresetIDOffset;
      int dwPresetIDBytes;
      int dwPlatformIDOffset;
      int dwPlatformIDBytes;
  };
  BYTE iocBuffer[1024];
  DWORD iocBufferReturnSize = sizeof(iocBuffer);
  LPBYTE piocBuffer = iocBuffer;
  DEVICE_ID* deviceId = (DEVICE_ID*)piocBuffer;
  deviceId->dwSize = iocBufferReturnSize;
  
  #define IOCTL_HAL_GET_DEVICEID ((FILE_DEVICE_HAL) << 16) | ((FILE_ANY_ACCESS) << 14) | ((21) << 2) | (METHOD_BUFFERED)

  if (KernelIoControl(IOCTL_HAL_GET_DEVICEID, 0, 0, iocBuffer, iocBufferReturnSize, &iocBufferReturnSize)) {
    WCHAR result[1024];
    LPWSTR pResult = result;
    pResult += wsprintf(pResult, L"%08x-%04x-%04x-%04x-",
      *((LPDWORD)(piocBuffer + deviceId->dwPresetIDOffset)),
      *((LPWORD)(piocBuffer + deviceId->dwPresetIDOffset+4)),
      *((LPWORD)(piocBuffer + deviceId->dwPresetIDOffset+6)),
      *((LPWORD)(piocBuffer + deviceId->dwPresetIDOffset+8)));
    if (deviceId->dwPlatformIDBytes == 0) {
      pResult += wsprintf(pResult, L"%012c", '0');
    } else {
      for (int i=deviceId->dwPlatformIDOffset; i<deviceId->dwPlatformIDOffset + deviceId->dwPlatformIDBytes; ++i) {
        pResult += wsprintf(pResult, L"%x", piocBuffer[i]);
      }
    }
    LPWSTR idString = rapi::allocOutput<WCHAR>(pcbOutput, ppOutput, sizeof(WCHAR) * (pResult - result + 1));
    if (idString ) {
      CopyMemory(idString , result, *pcbOutput);
      return S_OK;
    }
  }
RAPI_INVOKE_RETURN_LAST_ERROR()

#ifdef DEBUG
EXTERN_C __declspec (dllexport) void CALLBACK Test(
   HWND hwnd,        // handle to owner window
   HINSTANCE hinst,  // instance handle for the DLL
   LPTSTR lpCmdLine, // string the DLL will parse
   int nCmdShow      // show state
  ) {
  DWORD size;
  BYTE* data;
  //DLLVERSIONINFO dllVersion = { sizeof(dllVersion), 1, 2, 0 };
  //CompareDllVersion(dllVersion.cbSize, (LPBYTE)&dllVersion, &size, &data, 0);
  //ResetDevice(0, 0, &size, &data, 0);
  GetDeviceId(0, 0, &size, &data, 0);

  for (int i=0; i<100; i++) {
    LPSYSTEM_POWER_STATUS_EX2 status;
    rapi::GetSystemPowerStatusEx2(0, 0, &size, (LPBYTE*)&status, 0);
    LocalFree(status);
  }

  data = (LPBYTE)LocalAlloc(LPTR, 2*sizeof(UINT));
  *((UINT*)data) = SPI_GETOEMINFO;
  *(((UINT*)data)+1) = 256;  
  rapi::GetSystemParametersInfo(2*sizeof(UINT), data, &size, &data, 0);
  LPWSTR guid = (LPWSTR)(data);
}
#endif



// Template for function documentation

/*
  .

  @param cbInput [in] Size of the data in @c pInput or 0 if no such data is
  supplied.
  @param pInput [in] <i>not used</i>
  @param pcbOutput [out] Size of the data returned in @c ppOutput.
  @param ppOutput [out] <i>not used</i>
  @param pStream [in] <i>not used</i>

  @return S_OK or an error, if no memory for @c ppOutput could be allocated.
*/