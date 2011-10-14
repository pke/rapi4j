#include "stdafx.h"
#include <InitGuid.h>
#include "dccole2.h"

class ATL_NO_VTABLE DccManSink : 
  public ATL::CComObjectRootEx<CComMultiThreadModel>,
  public ATL::CComCoClass<DccManSink>,
  public IDccManSink2 {
private:

protected:
  ~DccManSink() {
    OutputDebugStringW(L"DccManSink::~DccManSink()\n");
  }

  void FinalRelease() {
  }

public:
  STDMETHODIMP OnLogIpAddr(DWORD dwIpAddr) {
    const BYTE addr[] = { 
      (BYTE) (dwIpAddr >> 24 & 0xFF), 
      (BYTE) (dwIpAddr >> 16 & 0xFF),
      (BYTE) (dwIpAddr >> 8 & 0xFF), 
      (BYTE) (dwIpAddr & 0xFF) };

      return S_OK;
  }

  STDMETHODIMP OnLogTerminated() {
    return S_OK;
  }

  STDMETHODIMP OnLogActive() {
    return S_OK;
  }

  STDMETHODIMP OnLogInactive() {
    return S_OK;
  }

  STDMETHODIMP OnLogAnswered() {
    return S_OK;
  }

  STDMETHODIMP OnLogListen() {
    return S_OK;
  }

  STDMETHODIMP OnLogDisconnection() {
    return S_OK;
  }

  STDMETHODIMP OnLogError() {
    return S_OK;
  }

  STDMETHODIMP OnLogIpAddrEx(const SOCKADDR_STORAGE* pIpAddr) {
    return S_OK;
  }


  DECLARE_PROTECT_FINAL_CONSTRUCT()

  BEGIN_COM_MAP(DccManSink)
    COM_INTERFACE_ENTRY(IUnknown)
    COM_INTERFACE_ENTRY_IID(IID_IDccManSink, IDccManSink)
    COM_INTERFACE_ENTRY_IID(IID_IDccManSink2, IDccManSink2)
  END_COM_MAP()
};

class Module : public ATL::CAtlDllModuleT<Module> {

}_AtlModule;

class DccManTest : public ::testing::Test {
public:
  void SetUp() {
    CoInitializeEx(0, COINIT_MULTITHREADED);
    
    ASSERT_HRESULT_SUCCEEDED(CoCreateInstance(CLSID_DccMan, 0, CLSCTX_INPROC_SERVER, IID_IDccMan, (LPVOID*)&dccMan));
    
    CComObject<DccManSink>* obj;
    CComObject<DccManSink>::CreateInstance(&obj);
    obj->QueryInterface(IID_IDccManSink2, (LPVOID*)&sink);
  }

  void TearDown() {
    CoUninitialize();
  }

protected:
  ATL::CComPtr<IDccManSink> sink;
  ATL::CComPtr<IDccMan> dccMan;
};

TEST_F(DccManTest, adviseSink) {
  DWORD context;
  dccMan->Advise(sink, &context);
  dccMan->Unadvise(context);
}