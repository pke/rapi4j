package com.microsoft.rapi;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("76a78b7d-8e54-4c06-ac38-459e6a1ab5e3")
public interface IRAPISession extends Com4jObject {
	@VTID(3)
	void CeRapiInit();

	@VTID(4)
	void CeRapiUninit();

	@VTID(5)
	int CeGetLastError();

	@VTID(6)
	int CeRapiGetError();
}
