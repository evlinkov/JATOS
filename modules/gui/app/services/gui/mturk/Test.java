package services.gui.mturk;

import java.time.Instant;

import javax.inject.Inject;
import javax.inject.Singleton;

import play.Logger;
import play.Logger.ALogger;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;

@Singleton
public class Test {

	private static final ALogger LOGGER = Logger.of(Signature.class);

	private final WSClient ws;

	@Inject
	Test(WSClient ws) {
		this.ws = ws;
	}

	public void call() {
		String mtUrl = "https://mechanicalturk.amazonaws.com/";
		String mtSandboxUrl = "https://mechanicalturk.sandbox.amazonaws.com/";
		String service = "AWSMechanicalTurkRequester";
		String accessKey = "AKIAJTG2XEVWRVTEFLWQ";
		String version = "2014-08-15";
		String operation = "GetRequesterStatistic";
		String timeStamp = Instant.now().toString();

		String data = service + operation + timeStamp;
		String signature = Signature.calculateRFC2104HMAC(data,
				"u1+FFCMcvQpc8/34QSHJe4re9Nw+8c7yYDRW1qvW");

		WSRequest wsRequest = ws.url(mtSandboxUrl)
				.setQueryParameter("Service", service)
				.setQueryParameter("AWSAccessKeyId", accessKey)
				.setQueryParameter("Version", version)
				.setQueryParameter("Operation", operation)
				.setQueryParameter("Signature", signature)
				.setQueryParameter("Timestamp", timeStamp)
				.setQueryParameter("Statistic", "NumberAssignmentsApproved")
				.setQueryParameter("TimePeriod", "ThirtyDays")
				.setQueryParameter("Count", "1");
		WSResponse wsResponse = wsRequest.get().get(5000);
		LOGGER.info("Response: " + wsResponse.getBody());
	}

}
