package fitnesse.responders.account;

import fitnesse.FitNesseContext;
import fitnesse.authentication.Password;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.BasicResponder;
import fitnesse.responders.ErrorResponder;
import org.apache.commons.lang3.StringUtils;

public class SaveAccountResponder extends BasicResponder {
  @Override
  public Response makeResponse(FitNesseContext context, Request request) throws Exception {
    if (request.getAuthorizationUsername() == null) {
      return getResponse(context, "You have to be logged in to use this feature.");
    }
    if (request.hasInput("changePassword")) {
      String passwordText = StringUtils.trim(request.getInput("PasswordText"));
      String confirmPasswordText = StringUtils.trim(request.getInput("ConfirmPasswordText"));
      if (passwordText.length() == 0 || !passwordText.equals(confirmPasswordText)) {
        return getResponse(context, "Password should not be empty and they should match.");
      }
      new Password().savePasswordToDefaultFile(request.getAuthorizationUsername(), passwordText);
    } else if ("admin".equals(request.getAuthorizationUsername())) {
      if (request.hasInput("createUser")) {
        String newUserNameText = StringUtils.trim(request.getInput("UserNameText"));
        String newUserPasswordText = StringUtils.trim(request.getInput("UserPasswordText"));
        if ("admin".equals(newUserNameText)) {
          return getResponse(context, "You cannot create admin user again.");
        }
        if (newUserNameText.length() == 0 || newUserPasswordText.length() == 0) {
          return getResponse(context, "Username or password field is empty.");
        }
        new Password().savePasswordToDefaultFile(newUserNameText, newUserPasswordText);
      } else if (request.hasInput("deleteUser")) {
        String newUserNameText = StringUtils.trim(request.getInput("UserNameText"));
        if ("admin".equals(newUserNameText)) {
          return getResponse(context, "You cannot delete admin user.");
        }
        Password password = new Password();
        try {
          password.deletePasswordInDefaultFile(newUserNameText);
        } catch (Exception ex) {
          return getResponse(context, ex.getMessage());
        }
      } else {
        return getResponse(context, "Invalid input to modify account.");
      }
    } else {
      return getResponse(context, "Only admin can create or delete users.");
    }

    Response response = new SimpleResponse();
    response.redirect(context.contextRoot, request.getResource());
    return response;
  }

  private static Response getResponse(FitNesseContext context, String message) throws Exception {
    Response response = new ErrorResponder(message).makeResponse(context, null);
    response.setStatus(412);
    return response;
  }

}
