package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;

public class InsecureOperation implements SecureOperation {
  public boolean shouldAuthenticate(FitNesseContext context, Request request) throws Exception {
    return false;
  }
}
