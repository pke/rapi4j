package com.microsoft.rapi;

import com4j.Com4jObject;
import com4j.Holder;
import com4j.IID;
import com4j.VTID;

@IID("{dcbeb807-14d0-4cbd-926c-b991f4fd1b91}")
public interface IRAPIDesktop extends Com4jObject {
	public static final String CLSID = "{35440327-1517-4B72-865E-3FFE8E97002F}"; //$NON-NLS-1$

	@VTID(4)
	IRAPIEnumDevices enumDevices();

	@VTID(5)
	void advise(IRAPISink sink, Holder<Integer> context);

	@VTID(6)
	void unadvice(int context);
}
