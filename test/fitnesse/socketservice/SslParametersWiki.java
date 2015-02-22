package fitnesse.socketservice;

import java.io.File;

public class SslParametersWiki extends SslParameters {
	
	public SslParametersWiki() {
		super();
		String aPW = "wiki15";
		setKeyStoreFilename("test" + File.separator + "fitnesse" + File.separator + "resources" +
				File.separator + "ssl" + File.separator + "wiki.jks");
		setKeyStorePassword(aPW);
		setTrustStoreFilename("test" + File.separator + "fitnesse" + File.separator +
				"resources" + File.separator + "ssl" + File.separator + "wiki_trust.jks");

	}
}
