#pragma once

#include <assert.h>
#include <shlwapi.h>

/**
  RAPI - Namespace for all RAPI related code.

  @author_philk
*/
namespace rapi {

/**
  Use this when exporting new function from your WinCE dll that should be
  invokable by CeRapiInvoke. 
  This will first call initOutput.

  @see initOutput
*/
#define RAPI_INVOKE(x) EXTERN_C int __declspec(dllexport) x(DWORD cbInput, BYTE* pInput, DWORD* pcbOutput, BYTE** ppOutput, LPVOID pStream) { \
  rapi::initOutput(pcbOutput, ppOutput); 
#define RAPI_INVOKE_RETURN(result) return (result); }  
#define RAPI_INVOKE_RETURN_LAST_ERROR() RAPI_INVOKE_RETURN(HRESULT_FROM_WIN32(GetLastError()))

/**
  Initializes a RAPI invokable function output params.
  This should right after the function in entered and before any error
  checking on input parameters.
  It will set pcbOutput to 0 and ppOutput to <code>NULL</code>.
*/
static void initOutput(DWORD* pcbOutput, BYTE** ppOutput) {
  assert(!IsBadWritePtr(pcbOutput, sizeof *pcbOutput));
  assert(!IsBadWritePtr(ppOutput, sizeof *ppOutput));
  *pcbOutput = 0;
  *ppOutput = 0;
}

template<class T> 
static T* allocOutput(DWORD* pcbOutput, BYTE** ppOutput, int size = sizeof(T)) {
  assert(!IsBadWritePtr(pcbOutput, sizeof *pcbOutput));
  assert(!IsBadWritePtr(ppOutput, sizeof *ppOutput));
  *pcbOutput = size;
  *ppOutput = (LPBYTE)LocalAlloc(LPTR, *pcbOutput);
#ifdef DEBUG
  WCHAR text[1024];
  wsprintf(text, L"allocOutput: 0x%x (%d bytes)\n", *ppOutput, size);
  OutputDebugStringW(text);
#endif
  return reinterpret_cast<T*>(*ppOutput);
}

class DllVersionInfo : public DLLVERSIONINFO {
public:
  /**
      Will fill the members of DLLVERSIONINFO with the segments from the 
      given version string.

      @param versionString must have the format "major.minor.build". It is 
      parsed from left to right, so if the "minor" and/or the "build" part
      is missing the respective members of DLLVERSIONINFO will be 0.
  */
  DllVersionInfo(LPCSTR versionString) {
    assert(versionString);
    cbSize = sizeof(*this);
    dwPlatformID = DLLVER_PLATFORM_NT;
    dwMajorVersion = dwMinorVersion = dwBuildNumber = 0;
    
    DWORD* parsing = &dwMajorVersion;
    char digit[256];
    char* pDigit = digit;
    while (*versionString && parsing <= &dwBuildNumber && pDigit < &digit[255]) {
      if (*versionString >= '0' && *versionString <= '9') {
        *pDigit++ = *versionString;
      } else if (*versionString == '.') {
        *pDigit = 0;
        *parsing++ = atoi(digit);
        pDigit = digit;
      } else { // Then its an invalid version string!
        return;
      }
      ++versionString;
    }
    // If there is anything remaining (most likely the build number)
    if (pDigit < &digit[256] && parsing <= &dwBuildNumber) {
      *pDigit = 0;
      *parsing = atoi(digit);        
    }
  }
};

// We do not want to have that in the Win32 UnitTests, which include this
// file also.
#ifdef _WIN32_WCE

/**
  Retrieves this DLLs version.

  @par Remarks
  The version is directly included in the code structure here by including
  the file ../../jrapiex.dll.ver into the variable definition. 
  If you change this library you must also increase the version in that 
  file to reflect the changes.

  @param cbInput [in] Size of the data in @c pInput or 0 if no such data is
  supplied.
  @param pInput [in] <i>not used</i>
  @param pcbOutput [out] size of the DLLVERSIONINFO struture returned in 
  @c ppOutput
  @param ppOutput A DLLVERSIONINFO structure
  @param pStream [in] <i>not used</i>

  @return S_OK or an error, if no memory for the output could be allocated.
*/
RAPI_INVOKE(GetDllVersion)
  // This will create: const LPCTSTR version="...";
  const LPCSTR  
#include "../../rapi4jex.dll.ver"
  ;
  static DllVersionInfo versionInfo(version);
  
  DLLVERSIONINFO* outVersionInfo = allocOutput<DLLVERSIONINFO>(pcbOutput, ppOutput);
  if (outVersionInfo) {
    CopyMemory(outVersionInfo, &versionInfo, *pcbOutput);
    return S_OK;
  }  
RAPI_INVOKE_RETURN_LAST_ERROR()

#endif // _WIN32_WCE

/**
  Input parameter for GetSystemParametersInfo.
*/
struct SystemParametersInfoInput {
  /// The size of this structure
  DWORD structSize;
  /// Which action to perform on SystemParametersInfo.
  UINT uiAction;
  /// 
  DWORD uiParam;

  /// If set to TRUE uiParam contains the number of bytes to allocate.
  BOOL allocate;

#ifdef _WIN32_WCE
#pragma warning(disable: 4355)
#endif
  SystemParametersInfoInput(const UINT uiAction, const UINT uiParam, boolean allocate) :
    structSize(sizeof(*this)), uiAction(uiAction), uiParam(uiParam), allocate(allocate) {
  }
};

} // namespace rapi