#pragma once

namespace {
  jnitl::JClassID javaProxy("com/microsoft/rapi/DccManSink");
  jnitl::JMethodID<jobject> javaProxy_onLogIpAddr(javaProxy,"onLogIpAddr","(I)V");
  jnitl::JMethodID<jobject> javaProxy_onLogTerminated(javaProxy,"onLogTerminated","()V");
  jnitl::JMethodID<jobject> javaProxy_onLogActive(javaProxy,"onLogActive","()V");
  jnitl::JMethodID<jobject> javaProxy_onLogInactive(javaProxy,"onLogInactive","()V");
  jnitl::JMethodID<jobject> javaProxy_onLogAnswered(javaProxy,"onLogAnswered","()V");
  jnitl::JMethodID<jobject> javaProxy_onLogListen(javaProxy,"onLogListen","()V");
  jnitl::JMethodID<jobject> javaProxy_onLogDisconnection(javaProxy,"onLogDisconnection","()V");
  jnitl::JMethodID<jobject> javaProxy_onLogError(javaProxy,"onLogError","()V");
}

class ATL_NO_VTABLE DccManSink :
  public ATL::CComObjectRootEx<CComMultiThreadModel>,
  public ATL::CComCoClass<DccManSink>,
  public IDccManSink2 {
private:
  // peer in the Java world
  jnitl::GlobalRef<jobject> javaProxy;

protected:
  ~DccManSink() {
    OutputDebugStringW(L"DccManSink::~DccManSink()\n");
  }

  void FinalRelease() {

  }

public:
  static IDccManSink* create(JNIEnv* env, jobject javaProxy) {
    OutputDebugStringW(L"DccManSink::create()\n");
    CoInitializeEx(0, COINIT_MULTITHREADED);
    CComObject<DccManSink>* obj;
    CComObject<DccManSink>::CreateInstance(&obj);
    obj->javaProxy.Attach(env, javaProxy);
    IDccManSink* sink;
    obj->QueryInterface(IID_IDccManSink, (LPVOID*)&sink);
    return sink;
  }

  STDMETHODIMP OnLogIpAddr(DWORD dwIpAddr) {
    const BYTE addr[] = {
      (BYTE) (dwIpAddr >> 24 & 0xFF),
      (BYTE) (dwIpAddr >> 16 & 0xFF),
      (BYTE) (dwIpAddr >> 8 & 0xFF),
      (BYTE) (dwIpAddr & 0xFF) };

    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogIpAddr(jniScope, javaProxy, (jint)dwIpAddr);
    return S_OK;
  }

  STDMETHODIMP OnLogTerminated() {
    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogTerminated(jniScope, javaProxy);
    return S_OK;
  }

  STDMETHODIMP OnLogActive() {
    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogActive(jniScope, javaProxy);
    return S_OK;
  }

  STDMETHODIMP OnLogInactive() {
    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogInactive(jniScope, javaProxy);
    return S_OK;
  }

  STDMETHODIMP OnLogAnswered() {
    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogAnswered(jniScope, javaProxy);
    return S_OK;
  }

  STDMETHODIMP OnLogListen() {
    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogListen(jniScope, javaProxy);
    return S_OK;
  }

  STDMETHODIMP OnLogDisconnection() {
    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogDisconnection(jniScope, javaProxy);
    return S_OK;
  }

  STDMETHODIMP OnLogError() {
    jnitl::AttachThread jniScope(jniModule);
    javaProxy_onLogError(jniScope, javaProxy);
    return S_OK;
  }

  STDMETHODIMP OnLogIpAddrEx(const SOCKADDR_STORAGE* pIpAddr) {
    jnitl::AttachThread jniScope(jniModule);
    // TODO: convert pIpAddr to java class
    return S_OK;
  }


  DECLARE_PROTECT_FINAL_CONSTRUCT()

  BEGIN_COM_MAP(DccManSink)
    COM_INTERFACE_ENTRY(IUnknown)
    COM_INTERFACE_ENTRY_IID(IID_IDccManSink, IDccManSink)
    COM_INTERFACE_ENTRY_IID(IID_IDccManSink2, IDccManSink2)
  END_COM_MAP()
};
