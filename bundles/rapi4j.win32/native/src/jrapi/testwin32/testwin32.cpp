// testwin32.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"
#include <rapi.h>
#pragma comment(lib, "rapi")

#if 0
DWORD __stdcall ThreadProc(void*) {
  BYTE*	pOut;
  DWORD	cbOut;
  while (true) {
    CeRapiInvoke(L"rapi4jex", L"GetSystemPowerStatusEx2", 0, NULL, &cbOut, &pOut, NULL, 0);
    WCHAR text[1024];
    wsprintf(text, L"allocOutput: 0x%x\n", pOut);
    OutputDebugStringW(text);
    LocalFree(pOut);
    Sleep(1000);
  }
}
#endif

/*
int _tmain(int argc, _TCHAR* argv[]) {
  DWORD	cbOut;
  BYTE*	pOut;
  HRESULT	hr;

  //Initialize Windows CE RAPI
  hr = CeRapiInit();
  //Invoke CallMyFunction routine in MyRapi DLL in the \Windows directory.
  HANDLE thread = CreateThread(0, 0, ThreadProc, 0, 0, 0);    
  WaitForSingleObject(thread, INFINITE);

  //Uninitialize Windows CE RAPI
  hr = CeRapiUninit();
  return 0;
}
*/
