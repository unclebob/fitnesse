package fitnesse.authentication;

import java.util.Properties;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.components.Base64;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

/**
 * HTTP SPNEGO (GSSAPI Negotiate) authenticator.
 * <p>
 * <strong>How to enable for Kerberos/Active Directory</strong>
 * <p>
 * Enable this plugin by editing plugins.properties and adding the line:
 * 
 * <pre>
 * Authenticator = fitnesse.authentication.NegotiateAuthenticator
 * </pre>
 * <p>
 * If using Kerberos on Unix, create a jaas-krb5.conf file with these contents:
 * 
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
 * <p>
 * Next, define these system properties when running the FitNesse server:
 * 
 * <pre>
 * -Djavax.security.auth.useSubjectCredsOnly=false
 * -Djava.security.auth.login.config=/path/to/jaas-krb5.conf
 * -Dsun.security.krb5.debug=true
 * </pre>
 * <p>
 * You can remove the krb5.debug property later, when you know it's working.
 * 
 * @author David Leonard Released into the Public domain, 2009. No warranty:
 *         Provided as-is.
 */
public class NegotiateAuthenticator extends Authenticator {

  public static final String AUTH_TYPE = "Negotiate";
  public static final String CLIENT_HEADER = "Authorization";
  public static final String SERVER_HEADER = "WWW-Authenticate";

  private String serviceName;         /* Server's GSSAPI name, or null for default */
  private Oid serviceNameType;        /* Name type of serviceName */
  private Oid mechanism;              /* Restricted authentication mechanism, unless null */
  private boolean stripRealm = true;  /* Strip the realm off the authenticated user's name */

  private final GSSManager manager;
  private final GSSCredential serverCreds;

  public NegotiateAuthenticator(Properties properties) throws Exception {
    super();
    serviceName = properties.getProperty("NegotiateAuthenticator.serviceName",
        null);
    serviceNameType = new Oid(properties.getProperty(
        "NegotiateAuthenticator.serviceNameType", GSSName.NT_HOSTBASED_SERVICE.toString()));
    String mechanismProperty = properties.getProperty(
        "NegotiateAuthenticator.mechanism", null);
    mechanism = mechanismProperty == null ? null : new Oid(mechanismProperty);
    stripRealm = Boolean.parseBoolean(properties.getProperty(
        "NegotiateAuthenticator.stripRealm", "true"));

    manager = GSSManager.getInstance();
    if (serviceName == null)
      serverCreds = null;
    else {
      GSSName name = manager.createName(serviceName, serviceNameType, mechanism);
      serverCreds = manager.createCredential(name,
          GSSCredential.INDEFINITE_LIFETIME, mechanism,
          GSSCredential.ACCEPT_ONLY);
    }
  }

  static class NegotiateResponder implements Responder {
    private String tokenHeader;

    public NegotiateResponder(String token) {
      if (token == null)
        tokenHeader = AUTH_TYPE;
      else
        tokenHeader = AUTH_TYPE + " " + token;
    }

    public Response makeResponse(FitNesseContext context, Request request)
        throws Exception {
      SimpleResponse response = new SimpleResponse(401);
      response.addHeader(SERVER_HEADER, tokenHeader);
      HtmlPage html = context.htmlPageFactory.newPage();
      HtmlUtil.addTitles(html, "SPNEGO authentication failed");
      html.main.add("Your client failed to complete required authentication");
      response.setContent(html.html());
      return response;
    }
  }

  @Override
  public Responder authenticate(FitNesseContext context, Request request,
      Responder privilegedResponder) throws Exception {

    String authHeader = (String) request.getHeader(CLIENT_HEADER);
    if (authHeader == null
        || !authHeader.toLowerCase().startsWith(AUTH_TYPE.toLowerCase()))
      return new NegotiateResponder(null);
    byte[] inputTokenEncoded = authHeader.substring(AUTH_TYPE.length()).trim().getBytes("UTF-8");
    byte[] inputToken = Base64.decode(inputTokenEncoded);

    String replyToken;
    GSSContext gssContext;
    String authenticatedUser;

    gssContext = manager.createContext(serverCreds);

    /*
     * XXX Nowhere to attach a partial context, so we are limited to
     * single-round auth mechanisms.
     */
    byte[] replyTokenBytes = gssContext.acceptSecContext(inputToken, 0,
        inputToken.length);
    replyToken = replyTokenBytes == null ? null : new String(Base64.encode(replyTokenBytes), "UTF-8");
    if (!gssContext.isEstablished())
      return new NegotiateResponder(replyToken);

    authenticatedUser = gssContext.getSrcName().toString();

    if (stripRealm) {
      int at = authenticatedUser.indexOf('@');
      if (at != -1)
        authenticatedUser = authenticatedUser.substring(0, at);
    }

    /* TODO expose delegated credentials to the responder? */
    request.setCredentials(authenticatedUser, null);

    final Responder responder = super.authenticate(context, request,
        privilegedResponder);
    if (replyToken == null)
      return responder;

    final String replyAuthHeader = AUTH_TYPE + " " + replyToken;
    return new Responder() {
      public Response makeResponse(FitNesseContext context, Request request)
          throws Exception {
        Response response = responder.makeResponse(context, request);
        response.addHeader(SERVER_HEADER, replyAuthHeader);
        return response;
      }
    };
  }

  public boolean isAuthenticated(String username, String password)
      throws Exception {
    return true;
  }

}
