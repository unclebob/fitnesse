package fitnesse.responders;

import fitnesse.Responder;
import fitnesse.authentication.SecureOperation;

public interface SecureResponder extends Responder
{
	SecureOperation getSecureOperation();
}
