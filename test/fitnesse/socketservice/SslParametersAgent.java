package fitnesse.socketservice;

import java.io.File;

public class SslParametersAgent extends SslParameters {

	public SslParametersAgent() {
		super("test" + File.separator + "fitnesse" + File.separator + "resources" +
				File.separator + "ssl" + File.separator + "agent.jks",
				"james007",
				"test" + File.separator + "fitnesse" + File.separator + "resources" +
						File.separator + "ssl" + File.separator + "agent_trust.jks");

	}
}
