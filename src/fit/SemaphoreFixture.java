package fit;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

public class SemaphoreFixture extends Fixture {
  private static final String SEMAPHORE_SUFFIX = ".semaphore";
  private static final Vector<String> semaphores = new Vector<>();

  @Override
  public void doTable(Parse table) {
    String[] args = this.getArgs();
    doActionOnSemaphore(args[1], args[0], table.at(0, 0));
    super.doTable(table);
  }

  @Override
  public void doRow(Parse row) {
    this.ignore(row);
  }

  private void doActionOnSemaphore(String action, String name, Parse infoCells) {
    boolean isOk = false;
    if ("lock".equals(action)) {
      isOk = lockSemaphore(name);
      if (!isOk) setForcedAbort(true);
    } else if ("unlock".equals(action)) isOk = unlockSemaphore(name);
    else exception(infoCells.parts, new Throwable("Bad action: " + action));

    if (isOk) {
      infoCells.parts.last().more = new Parse("td", makeSemaphoreName(name), null, null);
      right(infoCells);
    } else exception(infoCells.parts,
      new Throwable("Unable to " + action + " semaphore '" + name + "'")
    );
  }

  private static String makeSemaphoreName(String name) {
    return System.getProperty("user.dir") + "/semaphores/" + name + SEMAPHORE_SUFFIX;
  }

  public static boolean lockSemaphore(String name) {
    boolean isLocked = semaphores.contains(name); //...already locked?
    if (!isLocked) {
      isLocked = createSemaphore(name);
      if (isLocked) semaphores.add(name);
    }

    return isLocked;
  }

  public static boolean unlockSemaphore(String name) {
    boolean isOk = false;
    if (semaphores.contains(name)) {
      isOk = deleteSemaphore(name);
      if (isOk) semaphores.remove(name);
    }

    return isOk;
  }

  private static boolean createSemaphore(String name) {
    boolean isLocked = false;

    //---create the directory if need be
    File semDiry = new File(makeSemaphoreName(""));
    semDiry.mkdirs();

    //---create the semaphore
    File semFile = new File(makeSemaphoreName(name));
    try {
      isLocked = semFile.createNewFile();
    } catch (IOException e) {
      isLocked = false;
    }

    return isLocked;
  }

  private static boolean deleteSemaphore(String name) {
    boolean isOk = (new File(makeSemaphoreName(name))).delete();
    if (!isOk)
      System.out.print("Unable to remove semaphore '" + name + "'");

    return isOk;
  }

  public static void ClearSemaphores() {
    for (String semaphore : semaphores) {
      unlockSemaphore(semaphore);
    }
  }
}
