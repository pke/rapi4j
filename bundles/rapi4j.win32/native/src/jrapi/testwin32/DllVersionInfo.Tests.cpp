#include "stdafx.h"

#include "../../jrapiex/jrapi.h"

TEST(DllVersionInfo, construction_from_numerical_string) {
  {
    rapi::DllVersionInfo version("26.7.1977");
    ASSERT_EQ(26, version.dwMajorVersion);
    ASSERT_EQ(7, version.dwMinorVersion);
    ASSERT_EQ(1977, version.dwBuildNumber);
  }
  {
    rapi::DllVersionInfo version("26.7");
    ASSERT_EQ(26, version.dwMajorVersion);
    ASSERT_EQ(7, version.dwMinorVersion);
    ASSERT_EQ(0, version.dwBuildNumber);
  }
  {
    rapi::DllVersionInfo version("26.7.");
    ASSERT_EQ(26, version.dwMajorVersion);
    ASSERT_EQ(7, version.dwMinorVersion);
    ASSERT_EQ(0, version.dwBuildNumber);
  }
  {
    rapi::DllVersionInfo version("26.");
    ASSERT_EQ(26, version.dwMajorVersion);
    ASSERT_EQ(0, version.dwMinorVersion);
    ASSERT_EQ(0, version.dwBuildNumber);
  }
  {
    rapi::DllVersionInfo version("26");
    ASSERT_EQ(26, version.dwMajorVersion);
    ASSERT_EQ(0, version.dwMinorVersion);
    ASSERT_EQ(0, version.dwBuildNumber);
  }  
}

TEST(DllVersionInfo, construction_from_non_numeric_string) {
  rapi::DllVersionInfo version("abc");
  ASSERT_EQ(0, version.dwMajorVersion);
  ASSERT_EQ(0, version.dwMinorVersion);
  ASSERT_EQ(0, version.dwBuildNumber);
}