package fitnesse.socketservice;

import fitnesse.util.ClassUtils;

import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;




public class SslParameters {

	private String keyStoreFilename;
	private String keyStorePassword;
	private String trustStoreFilename;

	private String keyStoreFilenameOld;
	private String keyStorePasswordOld;
	private String trustStoreFilenameOld;

	protected void setKeyStoreFilename(String filename) {
		if(filename != null) this.keyStoreFilename = filename;
	}
	protected void setKeyStorePassword(String value) {
		if(value != null) this.keyStorePassword = value;
	}
	protected void setTrustStoreFilename(String filename) {
		if(filename != null) this.trustStoreFilename = filename;
	}


	private void setProperty(String tag, String value, String defaultValue) {
		if ( value == null) value =  defaultValue;
		if (value == null){
			System.clearProperty(tag);
		}
		else{
			System.setProperty(tag, value);
		}
	}

	protected SslParameters(){
	}

	protected SslParameters(String keyStoreFilename, String keyStorePassword, String trustStoreFilename){
		setKeyStoreFilename(keyStoreFilename);
		setKeyStorePassword(keyStorePassword);
		setTrustStoreFilename(trustStoreFilename);
	}

	protected void prepareGlobalConfiguration(){
		// Save the current values so that they can be restored
		keyStoreFilenameOld = System.getProperty("javax.net.ssl.keyStore" );
		keyStorePasswordOld = System.getProperty("javax.net.ssl.keyStorePassword");
		trustStoreFilenameOld= System.getProperty("javax.net.ssl.trustStore");

		setProperty("javax.net.ssl.keyStore", keyStoreFilename, "fitnesse.jks" );
		setProperty("javax.net.ssl.keyStorePassword", keyStorePassword, "FitNesse42");
		setProperty("javax.net.ssl.trustStore", trustStoreFilename, "fitnesse.jks");

	}

	protected void restorePreviousConfiguration(){
		setProperty("javax.net.ssl.keyStore", keyStoreFilenameOld, keyStoreFilenameOld );
		setProperty("javax.net.ssl.keyStorePassword", keyStorePasswordOld, keyStorePasswordOld);
		setProperty("javax.net.ssl.trustStore", trustStoreFilenameOld, trustStoreFilenameOld);
	}


	public static SslParameters createSslParameters(String sslParameterClassName) {
	  return createSslParameters(sslParameterClassName, ClassLoader.getSystemClassLoader());
	}

  public static SslParameters createSslParameters(String sslParameterClassName, ClassLoader classLoader) {
    Class<? extends SslParameters> sslParametersInstance;
    if (sslParameterClassName == null || "true".equalsIgnoreCase(sslParameterClassName)) {
      sslParametersInstance = SslParameters.class;
    }else{
      try {
        sslParametersInstance = classLoader.loadClass(sslParameterClassName).asSubclass(SslParameters.class);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Preparing SSL Parameters with Class " + sslParameterClassName + " failed. Class Not Found.", e);
      }
    }
    try{
      return sslParametersInstance.newInstance();
    }catch (Exception e) {
      throw new RuntimeException("Preparing SSL Parameters with Class " + sslParameterClassName + " failed.", e);
    }
  }

	public SSLServerSocketFactory createSSLServerSocketFactory(){
		SSLServerSocketFactory ssf;
		prepareGlobalConfiguration();
		try{
			ssf =  (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		}finally{
			restorePreviousConfiguration();
		}
	    return ssf;
	}

	public SSLSocketFactory createSSLSocketFactory(){
		SSLSocketFactory ssf;
		prepareGlobalConfiguration();
		try{
		    ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
		}finally{
			restorePreviousConfiguration();
		}
	    return ssf;
	}

}
