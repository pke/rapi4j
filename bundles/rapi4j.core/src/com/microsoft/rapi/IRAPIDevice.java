package com.microsoft.rapi;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("{8a0f1632-3905-4ca4-aea4-7e094ecbb9a7}")
public interface IRAPIDevice extends Com4jObject {

	class RAPI_DEVICESTATUS {
	}

	@VTID(3)
	RAPI_DEVICESTATUS getConnectStat();

	@VTID(4)
	/*RAPI_DEVICEINFO*/void getDeviceInfo();

	@VTID(5)
	/*RAPI_CONNECTIONINFO*/void getConnectionInfo();

	@VTID(6)
	IRAPISession createSession();
}
