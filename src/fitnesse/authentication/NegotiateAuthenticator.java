package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.util.Base64;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.html.template.HtmlPage;

import org.ietf.jgss.*;
import util.FileUtil;

import java.util.Properties;
import java.io.UnsupportedEncodingException;

/**
 * <p>HTTP SPNEGO (GSSAPI Negotiate) authenticator.</p>
 * <p><strong>How to enable for Kerberos/Active Directory</strong></p>
 * <p>Enable this plugin by editing plugins.properties and adding the line:</p>
 * <pre>
 * Authenticator = fitnesse.authentication.NegotiateAuthenticator
 * </pre>
 *
 * <p>If using Kerberos on Unix, create a jaas-krb5.conf file with these contents:</p>
 * <pre>
 * com.sun.security.jgss.accept  {
 *       com.sun.security.auth.module.Krb5LoginModule required
 *       storeKey=true
 *       isInitiator=false
 *       principal=&quot;HTTP/your.web.server@YOUR.REALM&quot;
 *       useKeyTab=true
 *       keyTab=&quot;/path/to/your/http.keytab&quot;
 *       ;
 *    };
 * </pre>
 *
 * <p>Next, define these system properties when running the FitNesse server:</p>
 * <pre>
 * -Djavax.security.auth.useSubjectCredsOnly=false
 * -Djava.security.auth.login.config=/path/to/jaas-krb5.conf
 * -Dsun.security.krb5.debug=true
 * </pre>
 *
 * <p>You can remove the krb5.debug property later, when you know it's working.</p>
 *
 * @author David Leonard Released into the Public domain, 2009. No warranty:
 *         Provided as-is.
 */
public class NegotiateAuthenticator extends Authenticator {

  public static final String NEGOTIATE = "Negotiate";

  protected String serviceName;         /* Server's GSSAPI name, or null for default */
  protected Oid serviceNameType;        /* Name type of serviceName */
  protected Oid mechanism;              /* Restricted authentication mechanism, unless null */
  protected boolean stripRealm = true;  /* Strip the realm off the authenticated user's name */

  protected GSSManager manager;
  protected GSSCredential serverCreds;

  public NegotiateAuthenticator(GSSManager manager, Properties properties) throws Exception {
    super();
    this.manager = manager;
    configure(properties);
    initServiceCredentials();
  }

  public NegotiateAuthenticator(Properties properties) throws Exception {
    this(GSSManager.getInstance(), properties);
  }

  protected void initServiceCredentials() throws Exception {
    if (serviceName == null)
      serverCreds = null;
    else {
      GSSName name = manager.createName(serviceName, serviceNameType, mechanism);
      serverCreds = manager.createCredential(name,
        GSSCredential.INDEFINITE_LIFETIME, mechanism,
        GSSCredential.ACCEPT_ONLY);
    }
  }

  protected void configure(Properties properties) throws Exception {
    serviceName = properties.getProperty("NegotiateAuthenticator.serviceName", null);
    serviceNameType = new Oid(properties.getProperty("NegotiateAuthenticator.serviceNameType",
      GSSName.NT_HOSTBASED_SERVICE.toString()));
    String mechanismProperty = properties.getProperty("NegotiateAuthenticator.mechanism", null);
    mechanism = mechanismProperty == null ? null : new Oid(mechanismProperty);
    stripRealm = Boolean.parseBoolean(properties.getProperty("NegotiateAuthenticator.stripRealm", "true"));
  }

  public GSSCredential getServerCredentials() {
    return serverCreds;
  }

  public Oid getServiceNameType() {
    return serviceNameType;
  }

  public Oid getMechanism() {
    return mechanism;
  }

  // Responder used when negotiation has not started or completed
  protected static class UnauthenticatedNegotiateResponder implements Responder {
    private String token;

    public UnauthenticatedNegotiateResponder(final String token) {
      this.token = token;
    }

    @Override
    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
      SimpleResponse response = new SimpleResponse(401);
      response.addHeader("WWW-Authenticate", token == null ? NEGOTIATE : NEGOTIATE + " " + token);
      HtmlPage html = context.pageFactory.newPage();
      html.addTitles("Negotiated authentication required");
      if (request == null)
        html.setMainTemplate("authRequired.vm");
      else
        html.setMainTemplate("authFailed.vm");
      response.setContent(html.html());
      return response;
    }
  }

  @Override
  protected Responder unauthorizedResponder(FitNesseContext context, Request request) {
    return new UnauthenticatedNegotiateResponder(request.getAuthorizationPassword());
  }

  /*
  * If negotiation succeeds, sets the username field in the request.
  * Otherwise, stores the next token to send in the password field and sets request username to null.
  * XXX It would be better to allow associating generic authenticator data to each request.
  */
  protected void negotiateCredentials(Request request) throws UnsupportedEncodingException, GSSException {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.toLowerCase().startsWith(NEGOTIATE.toLowerCase()))
      request.setCredentials(null, null);
    else {
      setCredentials(request, getToken(authHeader));
    }
  }

  static byte[] getToken(String authHeader) throws UnsupportedEncodingException {
    byte[] inputTokenEncoded = authHeader.substring(NEGOTIATE.length()).trim().getBytes(FileUtil.CHARENCODING);
    byte[] inputToken = Base64.decode(inputTokenEncoded);
    return inputToken;
  }

  private void setCredentials(Request request, byte[] inputToken) throws GSSException, UnsupportedEncodingException {
    /*
    * XXX Nowhere to attach a partial context to a TCP connection, so we are limited to
  * single-round auth mechanisms.
  */
    GSSContext gssContext = manager.createContext(serverCreds);
    byte[] replyTokenBytes = gssContext.acceptSecContext(inputToken, 0, inputToken.length);
    String replyToken = replyTokenBytes == null ? null : new String(Base64.encode(replyTokenBytes), FileUtil.CHARENCODING);
    if (!gssContext.isEstablished())
      request.setCredentials(null, replyToken);
    else {
      String authenticatedUser = gssContext.getSrcName().toString();

      if (stripRealm) {
        int at = authenticatedUser.indexOf('@');
        if (at != -1)
          authenticatedUser = authenticatedUser.substring(0, at);
      }

      request.setCredentials(authenticatedUser, replyToken);
    }
  }

  @Override
  public Responder authenticate(FitNesseContext context, Request request, Responder privilegedResponder) throws Exception {
    negotiateCredentials(request);
    return super.authenticate(context, request, privilegedResponder);
  }

  @Override
  public boolean isAuthenticated(String username, String password) {
    return username != null;
  }

}
