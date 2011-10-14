#include "stdafx.h"

#include "RapiTest.h"

class GetDllVersionTest : public RapiInvokeTest<DLLVERSIONINFO> {
protected:
  GetDllVersionTest() : super(L"rapi4jex", L"GetDllVersion") {
  }
};

TEST_F(GetDllVersionTest, GetDllVersion) {
  ASSERT_PRED_FORMAT3(assertInvoke, 0, NULL, S_OK);
  ASSERT_EQ(sizeof(TypeParam), output->cbSize);
}



// Copied from PocketPC 2003 SDK
typedef struct _SYSTEM_POWER_STATUS_EX2 {
  BYTE ACLineStatus;
  BYTE BatteryFlag;
  BYTE BatteryLifePercent;
  BYTE Reserved1;
  DWORD BatteryLifeTime;
  DWORD BatteryFullLifeTime;
  BYTE Reserved2;
  BYTE BackupBatteryFlag;
  BYTE BackupBatteryLifePercent;
  BYTE Reserved3;
  DWORD BackupBatteryLifeTime;
  DWORD BackupBatteryFullLifeTime;
  // Above here is old struct, below are new fields
  DWORD BatteryVoltage; 				// Reports Reading of battery voltage in millivolts (0..65535 mV)
  DWORD BatteryCurrent;				// Reports Instantaneous current drain (mA). 0..32767 for charge, 0 to -32768 for discharge
  DWORD BatteryAverageCurrent; 		// Reports short term average of device current drain (mA). 0..32767 for charge, 0 to -32768 for discharge
  DWORD BatteryAverageInterval;		// Reports time constant (mS) of integration used in reporting BatteryAverageCurrent	
  DWORD BatterymAHourConsumed; 		// Reports long-term cumulative average DISCHARGE (mAH). Reset by charging or changing the batteries. 0 to 32767 mAH  
  DWORD BatteryTemperature;			// Reports Battery temp in 0.1 degree C (-3276.8 to 3276.7 degrees C)
  DWORD BackupBatteryVoltage;			// Reports Reading of backup battery voltage
  BYTE  BatteryChemistry; 		    // See Chemistry defines above

  // New fields can be added below, but don't change any existing ones
} SYSTEM_POWER_STATUS_EX2, *PSYSTEM_POWER_STATUS_EX2, *LPSYSTEM_POWER_STATUS_EX2;

class GetSystemPowerStatusEx2Test : public RapiInvokeTest<SYSTEM_POWER_STATUS_EX2> {
protected:
  GetSystemPowerStatusEx2Test() : super(L"rapi4jex", L"GetSystemPowerStatusEx2") {
  }
};

TEST_F(GetSystemPowerStatusEx2Test, GetSystemPowerStatusEx2) {
  ASSERT_PRED_FORMAT3(assertInvoke, 0, NULL, S_OK);
}



class GetSystemParametersInfoTest : public RapiInvokeTest<BYTE, MAX_PATH> {
protected:
  GetSystemParametersInfoTest() : super(L"rapi4jex", L"GetSystemParametersInfo") {
  }
};

TEST_F(GetSystemParametersInfoTest, should_fail_with_invalid_input) {
  ASSERT_PRED_FORMAT3(assertInvoke, 0, NULL, E_INVALIDARG);
}

TEST_F(GetSystemParametersInfoTest, should_get_some_oem_info) {
  const int SPI_GETOEMINFO = 258;
  rapi::SystemParametersInfoInput input(SPI_GETOEMINFO, expectedSize, true);
  ASSERT_PRED_FORMAT3(assertInvoke, sizeof(input), (LPBYTE)&input, S_OK);
  ASSERT_FALSE(IsBadStringPtrW((LPCWSTR)output, expectedSize));
  // Just so we can inspect the output value while debugging.
  LPCWSTR oemText = (LPCWSTR)output;
}