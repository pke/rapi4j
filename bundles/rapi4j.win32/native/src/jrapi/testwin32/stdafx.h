// stdafx.h : include file for standard system include files,
// or project specific include files that are used frequently, but
// are changed infrequently
//

#pragma once

#ifndef _WIN32_WINNT		// Allow use of features specific to Windows XP or later.                   
#define _WIN32_WINNT 0x0501	// Change this to the appropriate value to target other versions of Windows.
#endif						

#include <stdio.h>
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#include <ObjBase.h>
#include <gtest/gtest.h>

#ifdef _DEBUG
#define _ATL_DEBUG_INTERFACES
#define _ATL_DEBUG_QI
#endif
#include <atlbase.h>
#include <atlcom.h>

// TODO: reference additional headers your program requires here
