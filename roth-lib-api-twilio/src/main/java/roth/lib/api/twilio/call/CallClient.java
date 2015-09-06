package roth.lib.api.twilio.call;

import roth.lib.api.twilio.TwilioClient;

public class CallClient extends TwilioClient
{

	public CallClient(String accountSid, String authToken, boolean debug)
	{
		super(accountSid, authToken, debug);
	}
	
	public MakeCallResponse makeCall(MakeCallRequest request)
	{
		return post(url(CALLS), request, MakeCallResponse.class);
	}
	
}
