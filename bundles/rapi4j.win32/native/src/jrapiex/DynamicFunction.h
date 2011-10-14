#pragma once

#include <typeinfo>

namespace win32 {

/**
  Makes a DLL exported function available only if its available in the DLL.

  This makes it possible to not statically link against a DLL and to be able to
  run on different versions of Windows.

  @par Example:
  @begincode
  win32::DynamicFunction<HRESULT (STDAPICALLTYPE*)(
    HWND hwnd,
    LPCWSTR pszSubAppName,
    LPCWSTR pszSubIdList)>
    SetWindowTheme("uxtheme", "SetWindowTheme");

  if (SetWindowTheme) {
    (*SetWindowTheme)(toolbarHWND, L" ", L" ");
  }
  @endcode

  @author_philk
*/
template <class Function>
class DynamicFunction {
  Function function;

public:
  DynamicFunction(const char* _lib, const char* _symbol) : function((Function)::GetProcAddressA(::LoadLibraryA(_lib), _symbol)) {}
  DynamicFunction(const wchar_t* _lib, const wchar_t* _symbol) : function((Function)::GetProcAddressW(::LoadLibraryW(_lib), _symbol)) {}
  Function operator*() const { return function; }
  operator bool() const { return function? true : false; }
};

} // win32