// as.cpp : Defines the entry point for the DLL application.
//

#include "stdafx.h"
#include "DccManSink.h"

#ifdef _MANAGED
#pragma managed(push, off)
#endif

#pragma comment (linker, "/EXPORT:DllMain=_DllMain@12,PRIVATE")
#pragma comment(linker, "/EXPORT:Java_com_microsoft_rapi_Native_createDccManSink=_Java_com_microsoft_rapi_Native_createDccManSink@12,PRIVATE")
jnitl::JNIModule jniModule;

extern "C" jlong JNICALL Java_com_microsoft_rapi_Native_createDccManSink(JNIEnv* env, jclass _, jobject javaProxy) {
  IDccManSink* sink = DccManSink::create(env, javaProxy);
  return reinterpret_cast<jlong>(sink);
}

STDAPI_(jlong) CreateObject(jobject javaProxy, IID iid) {
  jnitl::AttachThread env(jniModule);

  if (iid == IID_IDccManSink) {
    IDccManSink* sink = DccManSink::create(env, javaProxy);
    return reinterpret_cast<jlong>(sink);
  }
  return 0;
}

class AsModule : public ATL::CAtlDllModuleT<AsModule> {
}_AtlModule;

extern "C" BOOL WINAPI DllMain(HINSTANCE, DWORD dwReason, LPVOID lpReserved) {
  return _AtlModule.DllMain(dwReason, lpReserved);
}

#ifdef _MANAGED
#pragma managed(pop)
#endif
