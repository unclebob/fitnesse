package fitnesse.slim.instructions;

import java.security.Permission;

public class SystemExitSecurityManager extends SecurityManager {

  private SecurityManager delegate;

  public SystemExitSecurityManager(SecurityManager delegate) {
    this.delegate = delegate;
  }

  @Override
  public void checkExit(int status) {
    throw new SystemExitException("prevented system exit with exit code "
        + status);
  }

  @Override
  public void checkPermission(Permission perm, Object context) {
    if (delegate != null) {
      delegate.checkPermission(perm, context);
    }
  }

  @Override
  public void checkPermission(Permission perm) {
    if (delegate != null) {
      delegate.checkPermission(perm);
    }
  }

  public static class SystemExitException extends SecurityException {

    public SystemExitException(String message) {
      super(message);
    }

    private static final long serialVersionUID = 2584644457111168436L;

  }

}
