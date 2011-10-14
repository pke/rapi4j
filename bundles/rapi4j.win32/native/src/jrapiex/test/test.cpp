// test.cpp : Defines the entry point for the application.
//

#include "stdafx.h"

extern "C" __declspec (dllimport) void CALLBACK Test(
   HWND hwnd,        // handle to owner window
   HINSTANCE hinst,  // instance handle for the DLL
   LPTSTR lpCmdLine, // string the DLL will parse
   int nCmdShow      // show state
);

int WINAPI WinMain(	HINSTANCE hInstance,
					HINSTANCE hPrevInstance,
					LPTSTR    lpCmdLine,
					int       nCmdShow)
{
#ifdef DEBUG
 	Test(0, hInstance, lpCmdLine, nCmdShow);
#endif
	return 0;
}

