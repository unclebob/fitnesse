package fitnesse.socketservice;

public class SslParametersWiki extends SslParameters {

	public SslParametersWiki() {
		super();
		String aPW = "wiki15";
		setKeyStoreFilename("test\\fitnesse\\resources\\ssl\\wiki.jks");
		setKeyStorePassword(aPW);
		setTrustStoreFilename("test\\fitnesse\\resources\\ssl\\wiki_trust.jks");

	}

}
