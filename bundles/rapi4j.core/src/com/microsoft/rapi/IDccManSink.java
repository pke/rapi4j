package com.microsoft.rapi;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

@IID("{A7B88840-A812-11cf-8011-00A0C90A8F78}")
public interface IDccManSink extends Com4jObject {
	@VTID(3)
	void onLogIpAddr(int ip);

	@VTID(4)
	void onLogTerminated();

	@VTID(5)
	void onLogActive();

	@VTID(6)
	void onLogInactive();

	@VTID(7)
	void onLogAnswered();

	@VTID(8)
	void onLogListen();

	@VTID(9)
	void onLogDisconnection();

	@VTID(10)
	void onLogError();
}
