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
    Password password = new Password();
    if (request.hasInput("changePassword")) {
      String currentPassword = StringUtils.trim(request.getInput("CurrentPasswordText"));
      String newPasswordText = StringUtils.trim(request.getInput("NewPasswordText"));
      String confirmPasswordText = StringUtils.trim(request.getInput("ConfirmPasswordText"));
      if (newPasswordText.isEmpty() || !newPasswordText.equals(confirmPasswordText)) {
        return getResponse(context, "Password should not be empty and they should match.");
      }
      if (!currentPassword.equals(request.getAuthorizationPassword())) {
        return getResponse(context, "Current password is incorrect.");
      }
      password.savePassword(request.getAuthorizationUsername(), newPasswordText);
    } else if ("admin".equals(request.getAuthorizationUsername())) {
      if (request.hasInput("createUser")) {
        String newUserNameText = StringUtils.trim(request.getInput("UserNameText"));
        String newUserPasswordText = StringUtils.trim(request.getInput("UserPasswordText"));
        if (newUserNameText.isEmpty() || newUserPasswordText.isEmpty()) {
          return getResponse(context, "Username or password field is empty.");
        } else if (password.doesUserExist(newUserNameText)) {
          return getResponse(context, "User already exists.");
        }
        password.savePassword(newUserNameText, newUserPasswordText);
      } else if (request.hasInput("deleteUser")) {
        String newUserNameText = StringUtils.trim(request.getInput("UserNameText"));
        if ("admin".equals(newUserNameText)) {
          return getResponse(context, "You cannot delete admin user.");
        }
        try {
          password.deletePassword(newUserNameText);
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
