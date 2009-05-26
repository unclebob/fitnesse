package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.components.Base64;
import fitnesse.http.MockRequest;
import fitnesse.http.Request;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import org.ietf.jgss.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import static org.mockito.Mockito.*;
import static util.RegexTestCase.assertSubString;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class NegotiateAuthenticatorTest {
  private GSSManager manager;
  private Properties properties;
  private final String TOKEN = "xxxxxxxx";

  @Before
  public void setUp() {
    manager = mock(GSSManager.class);
    properties = new Properties();
  }

  @Test
  public void credentialsShouldBeNullIfNoServiceName() throws Exception {
    NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties);
    assertNull(authenticator.getServerCredentials());
    verify(manager, never()).createName(anyString(), (Oid) anyObject(), (Oid) anyObject());
  }

  @Test
  public void credentialsShouldBeNonNullIfServiceNamePresent() throws Exception {
    properties.setProperty("NegotiateAuthenticator.serviceName", "service");
    properties.setProperty("NegotiateAuthenticator.serviceNameType", "1.1");
    properties.setProperty("NegotiateAuthenticator.mechanism", "1.2");
    GSSName gssName = mock(GSSName.class);
    GSSCredential gssCredential = mock(GSSCredential.class);
    when(manager.createName(anyString(), (Oid) anyObject(), (Oid) anyObject())).thenReturn(gssName);
    when(manager.createCredential((GSSName) anyObject(), anyInt(), (Oid) anyObject(), anyInt())).thenReturn(gssCredential);
    NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties);
    Oid serviceNameType = authenticator.getServiceNameType();
    Oid mechanism = authenticator.getMechanism();
    verify(manager).createName("service", serviceNameType, mechanism);
    assertEquals("1.1", serviceNameType.toString());
    assertEquals("1.2", mechanism.toString());
    verify(manager).createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, mechanism, GSSCredential.ACCEPT_ONLY);
    assertEquals(gssCredential, authenticator.getServerCredentials());
  }

  @Test
  public void negotiationErrorScreenForFailureToComplete() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    Responder responder = new NegotiateAuthenticator.UnauthenticatedNegotiateResponder("token");
    Request request = new MockRequest();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals("Negotiate token", response.getHeader("WWW-Authenticate"));
    String content = response.getContent();
    assertSubString("Negotiated authentication required", content);
    assertSubString("Your client failed to complete required authentication", content);
  }

  @Test
  public void negotiationErrorScreenForNeedingAuthentication() throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    FitNesseContext context = FitNesseUtil.makeTestContext(root);
    Responder responder = new NegotiateAuthenticator.UnauthenticatedNegotiateResponder("token");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, null);
    String content = response.getContent();
    assertSubString("This request requires authentication", content);
  }

  @Test
  public void noAuthorizationHeaderShouldProduceNullCredentials() throws Exception {
    MockRequest request = new MockRequest();
    NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties);
    authenticator.negotiateCredentials(request);
    assertNull(request.getAuthorizationUsername());
    assertNull(request.getAuthorizationPassword());
  }

  @Test
  public void invalidAuthorizationHeaderShouldProduceNullCredentials() throws Exception {
    MockRequest request = new MockRequest();
    request.addHeader("Authorization", "blah");
    NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties);
    authenticator.negotiateCredentials(request);
    assertNull(request.getAuthorizationUsername());
    assertNull(request.getAuthorizationPassword());
  }

  @Test
  public void validAuthorizationHeaderAndEstablishedContextShouldProduceUserAndPassword() throws Exception {
    String userName = "username";
    String password = "password";
    String encodedPassword = base64Encode(password);
    GSSContext gssContext = makeMockGssContext(userName, password);
    when(gssContext.isEstablished()).thenReturn(true);
    MockRequest request = new MockRequest();
    doNegotiation(request);
    assertEquals(userName, request.getAuthorizationUsername());
    assertEquals(encodedPassword, request.getAuthorizationPassword());
  }

  private void doNegotiation(MockRequest request) throws Exception {
    request.addHeader("Authorization", NegotiateAuthenticator.NEGOTIATE + " " + TOKEN);
    NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties);
    authenticator.negotiateCredentials(request);
  }

  private GSSContext makeMockGssContext(String userName, String password) throws GSSException {
    GSSContext gssContext = mock(GSSContext.class);
    when(manager.createContext((GSSCredential) anyObject())).thenReturn(gssContext);
    when(gssContext.acceptSecContext((byte[])anyObject(), anyInt(), anyInt())).thenReturn(password.getBytes());
    GSSName gssName = mock(GSSName.class);
    when(gssName.toString()).thenReturn(userName);
    when(gssContext.getSrcName()).thenReturn(gssName);
    return gssContext;
  }


  @Test
  public void validAuthorizationHeaderAndNoEstablishedContextShouldProducePasswordButNoUser() throws Exception {
    String userName = "username";
    String password = "password";
    String encodedPassword = base64Encode(password);
    GSSContext gssContext = makeMockGssContext(userName, password);
    when(gssContext.isEstablished()).thenReturn(false);
    MockRequest request = new MockRequest();
    doNegotiation(request);
    assertNull(request.getAuthorizationUsername());
    assertEquals(encodedPassword, request.getAuthorizationPassword());
  }


  @Test
  public void realmIsStrippedIfRequested() throws Exception {
    properties.setProperty("NegotiateAuthenticator.stripRealm", "true");
    String userName = "username@realm";
    String password = "password";
    String encodedPassword = base64Encode(password);
    GSSContext gssContext = makeMockGssContext(userName, password);
    when(gssContext.isEstablished()).thenReturn(true);
    MockRequest request = new MockRequest();
    doNegotiation(request);
    assertEquals("username", request.getAuthorizationUsername());
    assertEquals(encodedPassword, request.getAuthorizationPassword());
  }

  private String base64Encode(String s) throws UnsupportedEncodingException {
    return new String(Base64.encode(s.getBytes("UTF-8")));
  }

  @Test
  public void getTokenShouldReturnDecodedToken() throws Exception {
    byte[] actual = NegotiateAuthenticator.getToken(NegotiateAuthenticator.NEGOTIATE + " " + TOKEN);
    byte[] expected = Base64.decode(TOKEN.getBytes("UTF-8"));
    Assert.assertArrayEquals(expected, actual);
  }

}
