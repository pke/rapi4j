#pragma once

#include <rapi.h>
#include "../../jrapiex/jrapi.h"

class RapiTest : public ::testing::Test {
protected:
  void SetUp() {
    // If this test fails you may need to connect your Mobile device first or check
    // for a proper connection!
    ASSERT_HRESULT_SUCCEEDED(::CeRapiInit());
  }

  void TearDown() {
    ASSERT_HRESULT_SUCCEEDED(::CeRapiUninit());
  }
};


template <typename T, DWORD size = sizeof(T)>
class RapiInvokeTest : public RapiTest {
protected:
  RapiInvokeTest(LPCWSTR path, LPCWSTR functionName) : 
    output(0), path(path), functionName(functionName), expectedSize(size) {
  }

  ::testing::AssertionResult assertInvoke(
    const char* inputSize_expr, 
    const char* input_expr,
    const char* expectedResult_expr,
    DWORD inputSize, 
    LPBYTE input, 
    HRESULT expectedResult) {
    DWORD outputSize;
    HRESULT result = ::CeRapiInvoke(path, functionName, inputSize, input, &outputSize, (LPBYTE*)&output, NULL, 0);
    if SUCCEEDED(result) {
      if (expectedSize == outputSize) {
        return ::testing::AssertionSuccess();
      } else {
        return ::testing::AssertionFailure() << "After calling " << functionName << " in " << path << " the size of the output is " << outputSize << " instead of the expected " << expectedSize;
      }
    }
    if (expectedResult != result) {
      return ::testing::internal::IsHRESULTSuccess(expectedResult_expr, result);
    }
    return ::testing::AssertionSuccess();
  }

  void SetUp() {
    ASSERT_FALSE(IsBadStringPtrW(path, MAX_PATH));
    ASSERT_FALSE(IsBadStringPtrW(functionName, 256));
    __super::SetUp();
  }

  void TearDown() {
    if (output) {
      ASSERT_EQ(NULL, ::LocalFree(output));
    }
    __super::TearDown();
  }

protected:
  typedef RapiInvokeTest<T, size> super;
  typedef T TypeParam;
  const DWORD expectedSize;
  T* output;
private:
  LPCWSTR path;
  LPCWSTR functionName;
};
