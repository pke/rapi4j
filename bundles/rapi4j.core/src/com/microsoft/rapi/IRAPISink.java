package com.microsoft.rapi;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("{b4fd053e-4810-46db-889b-20e638e334f0}")
public interface IRAPISink extends Com4jObject {

	@VTID(3)
	void onDeviceConnected(final IRAPIDevice device);

	@VTID(4)
	void onDeviceDisconnected(final IRAPIDevice device);
}
