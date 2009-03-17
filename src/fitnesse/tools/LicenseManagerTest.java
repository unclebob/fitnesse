// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.tools;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import util.FileUtil;

public class LicenseManagerTest {
  private final String dir = "toolsTempTestDirectory";
  private final String licenseText = "// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.\n" +
    "// Released under the terms of the GNU General Public License version 2 or later.\n";

  @Before
  public void setup() {
    cleanup();
    FileUtil.createDir(dir);
    FileUtil.createFile(dir+"/license", licenseText);
  }
  
  @After
  public void cleanup() {
    FileUtil.deleteFileSystemDirectory(dir);
  }

  @Test
  public void removeLicenseFromOneFile() throws Exception {
    File testDir = FileUtil.createDir(dir);
    File fileWithLicense = new File(testDir, "fileWithLicense.java");
    FileUtil.createFile(fileWithLicense, licenseText + "xxx\n");
    LicenseManager.main(new String[]{"-r", dir});
    assertEquals("xxx\n", FileUtil.getFileContent(fileWithLicense));
  }

  @Test
  public void removeLicensesFromAllFiles() throws Exception {
    FileUtil.makeDir(dir+"/x");
    FileUtil.makeDir(dir+"/x/y");
    String[] files = {"f1.java", "x/f2.java", "x/y/f3.java", "x/y/f4.java"};
    for (String fileName : files)
      FileUtil.createFile(dir+"/"+fileName, licenseText+"yyy\n");
    LicenseManager.main(new String[] {"-r", dir});
    for (String fileName : files)
      assertEquals("yyy\n", FileUtil.getFileContent(dir+"/"+fileName));
  }

  @Test
  public void doesNotRemoveLicenseFromNonJavaFile() throws Exception {
    File testDir = FileUtil.createDir(dir);
    File fileWithLicense = new File(testDir, "fileWithLicense");
    FileUtil.createFile(fileWithLicense, licenseText + "xxx\n");
    LicenseManager.main(new String[]{"-r", dir});
    assertEquals(licenseText + "xxx\n", FileUtil.getFileContent(fileWithLicense));
  }

  @Test
  public void addLicenseToFile() throws Exception {
    File testDir = FileUtil.createDir(dir);
    File fileWithLicense = new File(testDir, "fileWithLicense.java");
    FileUtil.createFile(fileWithLicense, "xxx\n");
    LicenseManager.main(new String[]{dir+"/license", dir});
    assertEquals(licenseText + "xxx\n", FileUtil.getFileContent(fileWithLicense));
  }

  @Test
  public void addLicensesToAllFiles() throws Exception {
    FileUtil.makeDir(dir+"/x");
    FileUtil.makeDir(dir+"/x/y");
    String[] files = {"f1.java", "x/f2.java", "x/y/f3.java", "x/y/f4.java"};
    for (String fileName : files)
      FileUtil.createFile(dir+"/"+fileName, "yyy\n");
    LicenseManager.main(new String[] {dir+"/license", dir});
    for (String fileName : files)
      assertEquals(licenseText + "yyy\n", FileUtil.getFileContent(dir+"/"+fileName));
  }


}
