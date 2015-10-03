package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;

public class InsecureOperation implements SecureOperation {
  @Override
  public boolean shouldAuthenticate(FitNesseContext context, Request request) {
    return false;
  }
}
