package com.microsoft.rapi;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("357a557c-b03f-4240-90d8-c6c71c659bf1")
public interface IRAPIEnumDevices extends Com4jObject, Iterable<Com4jObject> {
	@VTID(3)
	IRAPIDevice next();

	@VTID(4)
	void reset();

	@VTID(5)
	void skip(int cElt);

	@VTID(6)
	IRAPIEnumDevices clone();

	@VTID(10)
	java.util.Iterator<Com4jObject> iterator();
}
