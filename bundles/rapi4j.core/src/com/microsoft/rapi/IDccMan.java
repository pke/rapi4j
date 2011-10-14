package com.microsoft.rapi;

import com4j.Com4jObject;
import com4j.Holder;
import com4j.IID;
import com4j.VTID;

@IID("{A7B88841-A812-11cf-8011-00A0C90A8F78}")
public interface IDccMan extends Com4jObject {
	public static final String CLSID = "{499C0C20-A766-11cf-8011-00A0C90A8F78}"; //$NON-NLS-1$

	@VTID(3)
	void Advise(IDccManSink sink, Holder<Integer> cookie);

	@VTID(4)
	void Unadvise(int cookie);

	@VTID(5)
	void ShowCommSettings();

	@VTID(6)
	void AutoconnectEnable();

	@VTID(7)
	void AutoconnectDisable();

	@VTID(8)
	void ConnectNow();

	@VTID(9)
	void DisconnectNow();

	@VTID(10)
	void SetIconDataTransferring();

	@VTID(11)
	void SetIconNoDataTransferring();

	@VTID(12)
	void SetIconError();
}
