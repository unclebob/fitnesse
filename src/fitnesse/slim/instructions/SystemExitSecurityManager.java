package fitnesse.slim.instructions;

import java.security.Permission;

public class SystemExitSecurityManager extends SecurityManager {

  public static final String PREVENT_SYSTEM_EXIT = "prevent.system.exit";
  private SecurityManager delegate;

  /**
   * The {@link SystemExitSecurityManager} overrides the behavior of the wrapped
   * original {@link SecurityManager} to prevent {@link System#exit(int)} calls
   * from being executed.
   *
   * @author Anis Ben Hamidene
   *
   */
  private SystemExitSecurityManager(SecurityManager delegate) {
    this.delegate = delegate;
  }

  /**
   * Replaces the current {@link SecurityManager} with a
   * {@link SystemExitSecurityManager}.
   */
  public static void activateIfWanted() {
    if (isPreventSystemExit() && !isAndroid()) {
      SecurityManager currentSecMgr = System.getSecurityManager();
      tryUpdateSecurityManager(new SystemExitSecurityManager(currentSecMgr));
    }
  }

  private static void tryUpdateSecurityManager(SecurityManager securityManager) {
    try {
      System.setSecurityManager(securityManager);
    } catch (SecurityException e) {
      System.err.println("Security manager could not be updated");
    } catch (UnsupportedOperationException e) {
      System.err.println("Security manager could not be updated. If you are using a JDK version >=18, you need to set " +
        "-Djava.security.manager=allow to allow this. Or use -Dprevent.system.exit=false to disable the FitNesse feature" +
        " blocking System.exit() calls and prevent this message.");
    }
  }

  public static void restoreOriginalSecurityManager() {
    SecurityManager currentSecMgr = System.getSecurityManager();
    if (currentSecMgr instanceof SystemExitSecurityManager) {
      tryUpdateSecurityManager(((SystemExitSecurityManager) currentSecMgr).delegate);
    }
  }

  private static boolean isPreventSystemExit() {
    String preventSystemExitString = System.getProperty(PREVENT_SYSTEM_EXIT);
    if (preventSystemExitString != null) {
      return Boolean.parseBoolean(preventSystemExitString);
    } else {
      return false;
    }
  }

  private static boolean isAndroid() {
    String vendorUrl = System.getProperty("java.vendor.url", "");
    return vendorUrl.toLowerCase().contains("android");
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

  @Override
  public void checkCreateClassLoader() {

    if (delegate != null) {
      delegate.checkCreateClassLoader();
    }
  }

  @Override
  public void checkAccess(Thread t) {

    if (delegate != null) {
      delegate.checkAccess(t);
    }
  }

  @Override
  public void checkAccess(ThreadGroup g) {

    if (delegate != null) {
      delegate.checkAccess(g);
    }
  }

  @Override
  public void checkExec(String cmd) {

    if (delegate != null) {
      delegate.checkExec(cmd);
    }
  }

  @Override
  public void checkLink(String lib) {

    if (delegate != null) {
      delegate.checkLink(lib);
    }
  }

  @Override
  public void checkRead(String file) {

    if (delegate != null) {
      delegate.checkRead(file);
    }
  }

  @Override
  public void checkRead(String file, Object context) {

    if (delegate != null) {
      delegate.checkRead(file, context);
    }
  }

  @Override
  public void checkWrite(String file) {

    if (delegate != null) {
      delegate.checkWrite(file);
    }
  }

  @Override
  public void checkDelete(String file) {

    if (delegate != null) {
      delegate.checkDelete(file);
    }
  }

  @Override
  public void checkConnect(String host, int port) {

    if (delegate != null) {
      delegate.checkConnect(host, port);
    }
  }

  @Override
  public void checkConnect(String host, int port, Object context) {

    if (delegate != null) {
      delegate.checkConnect(host, port, context);
    }
  }

  @Override
  public void checkListen(int port) {

    if (delegate != null) {
      delegate.checkListen(port);
    }
  }

  @Override
  public void checkAccept(String host, int port) {

    if (delegate != null) {
      delegate.checkAccept(host, port);
    }
  }

  @Override
  public void checkPropertiesAccess() {

    if (delegate != null) {
      delegate.checkPropertiesAccess();
    }
  }

  @Override
  public void checkPropertyAccess(String key) {

    if (delegate != null) {
      delegate.checkPropertyAccess(key);
    }
  }

  @Override
  public void checkPrintJobAccess() {

    if (delegate != null) {
      delegate.checkPrintJobAccess();
    }
  }

  @Override
  public void checkPackageAccess(String pkg) {

    if (delegate != null) {
      delegate.checkPackageAccess(pkg);
    }
  }

  @Override
  public void checkPackageDefinition(String pkg) {

    if (delegate != null) {
      delegate.checkPackageDefinition(pkg);
    }
  }

  @Override
  public void checkSetFactory() {
    if (delegate != null) {
      delegate.checkSetFactory();
    }
  }

  @Override
  public void checkSecurityAccess(String target) {

    if (delegate != null) {
      delegate.checkSecurityAccess(target);
    }
  }

  public static class SystemExitException extends SecurityException {

    public SystemExitException(String message) {
      super(message);
    }

    private static final long serialVersionUID = 2584644457111168436L;

  }

}
