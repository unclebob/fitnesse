// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Date;

import util.FileUtil;
import fitnesse.ComponentFactory;
import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class FileSystemPage extends CachingPage {
  private static final long serialVersionUID = 1L;

  public static final String contentFilename = "/content.txt";
  public static final String propertiesFilename = "/properties.xml";

  private final String path;
  private final VersionsController versionsController;
  private CmSystem cmSystem = new CmSystem();

  public FileSystemPage(final String path, final String name, final ComponentFactory componentFactory) throws Exception {
    super(name, null);
    this.path = path;

    versionsController = createVersionsController(componentFactory);
    createDirectoryIfNewPage();
  }

  protected FileSystemPage(final String path, final String name, final WikiPage parent,
                           final VersionsController versionsController) throws Exception {
    super(name, parent);
    this.path = path;

    this.versionsController = versionsController;
    createDirectoryIfNewPage();
  }

  public FileSystemPage(final String path, final String name) throws Exception {
    this(path, name, new ComponentFactory());
  }

  private VersionsController createVersionsController(ComponentFactory factory) throws Exception {
    return (VersionsController) factory.createComponent(ComponentFactory.VERSIONS_CONTROLLER,
      ZipFileVersionsController.class);
  }

  @Override
  public void removeChildPage(final String name) throws Exception {
    super.removeChildPage(name);
    String pathToDelete = getFileSystemPath() + "/" + name;
    final File fileToBeDeleted = new File(pathToDelete);
    FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
    cmSystem.delete(pathToDelete);
  }

  @Override
  public boolean hasChildPage(final String pageName) throws Exception {
    final File f = new File(getFileSystemPath() + "/" + pageName);
    if (f.exists()) {
      addChildPage(pageName);
      return true;
    }
    return false;
  }

  protected synchronized void saveContent(String content) throws Exception {
    if (content == null) {
      return;
    }

    final String separator = System.getProperty("line.separator");

    if (content.endsWith("|")) {
      content += separator;
    }

    content = content.replaceAll("\r\n", separator);

    String contentPath = getFileSystemPath() + contentFilename;
    final File output = new File(contentPath);
    OutputStreamWriter writer = null;
    try {
      if (output.exists())
        cmSystem.edit(contentPath);
      writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
      writer.write(content);
    } finally {
      if (writer != null) {
        writer.close();
        cmSystem.update(contentPath);
      }
    }
  }

  protected synchronized void saveAttributes(final WikiPageProperties attributes)
    throws Exception {
    OutputStream output = null;
    String propertiesFilePath = "<unknown>";
    try {
      propertiesFilePath = getFileSystemPath() + propertiesFilename;
      File propertiesFile = new File(propertiesFilePath);
      if (propertiesFile.exists())
        cmSystem.edit(propertiesFilePath);
      output = new FileOutputStream(propertiesFile);
      WikiPageProperties propertiesToSave = new WikiPageProperties(attributes);
      removeAlwaysChangingProperties(propertiesToSave);
      propertiesToSave.save(output);
    } catch (final Exception e) {
      System.err.println("Failed to save properties file: \""
        + propertiesFilePath + "\" (exception: " + e + ").");
      e.printStackTrace();
      throw e;
    } finally {
      if (output != null) {
        output.close();
        cmSystem.update(propertiesFilePath);
      }
    }
  }

  private void removeAlwaysChangingProperties(WikiPageProperties properties) {
    properties.remove(PageData.PropertyLAST_MODIFIED);
  }

  @Override
  protected WikiPage createChildPage(final String name) throws Exception {
    return new FileSystemPage(getFileSystemPath(), name, this, this.versionsController);
  }

  private void loadContent(final PageData data) throws Exception {
    String content = "";
    final String name = getFileSystemPath() + contentFilename;
    final File input = new File(name);
    if (input.exists()) {
      final byte[] bytes = readContentBytes(input);
      content = new String(bytes, "UTF-8");
    }
    data.setContent(content);
  }

  @Override
  protected void loadChildren() throws Exception {
    final File thisDir = new File(getFileSystemPath());
    if (thisDir.exists()) {
      final String[] subFiles = thisDir.list();
      for (final String subFile : subFiles) {
        if (fileIsValid(subFile, thisDir) && !this.children.containsKey(subFile)) {
          this.children.put(subFile, getChildPage(subFile));
        }
      }
    }
  }

  private byte[] readContentBytes(final File input) throws IOException {
    FileInputStream inputStream = null;
    try {
      final byte[] bytes = new byte[(int) input.length()];
      inputStream = new FileInputStream(input);
      inputStream.read(bytes);
      return bytes;
    } finally {
      if (inputStream != null) {
        inputStream.close();
      }
    }
  }

  private boolean fileIsValid(final String filename, final File dir) {
    if (WikiWordWidget.isWikiWord(filename)) {
      final File f = new File(dir, filename);
      if (f.isDirectory()) {
        return true;
      }
    }
    return false;
  }

  private String getParentFileSystemPath() {
    return this.parent != null ? ((FileSystemPage) this.parent).getFileSystemPath() : this.path;
  }

  public String getFileSystemPath() {
    return getParentFileSystemPath() + "/" + getName();
  }

  public String getAbsoluteFileSystemPath() {
    return new File(getFileSystemPath()).getAbsolutePath();
  }

  private void loadAttributes(final PageData data) throws Exception {
    final File file = new File(getFileSystemPath() + propertiesFilename);
    if (file.exists()) {
      try {
        long lastModifiedTime = getLastModifiedTime();
        attemptToReadPropertiesFile(file, data, lastModifiedTime);
      } catch (final Exception e) {
        System.err.println("Could not read properties file:" + file.getPath());
        e.printStackTrace();
      }
    }
  }

  private long getLastModifiedTime() throws Exception {
    long lastModifiedTime = 0;

    final File file = new File(getFileSystemPath() + contentFilename);
    if (file.exists()) {
      lastModifiedTime = file.lastModified();
    } else {
      lastModifiedTime = new Date().getTime();
    }
    return lastModifiedTime;
  }

  private void attemptToReadPropertiesFile(File file, PageData data,
                                           long lastModifiedTime) throws Exception {
    InputStream input = null;
    try {
      final WikiPageProperties props = new WikiPageProperties();
      input = new FileInputStream(file);
      props.loadFromXmlStream(input);
      props.setLastModificationTime(new Date(lastModifiedTime));
      data.setProperties(props);
    } finally {
      if (input != null)
        input.close();
    }
  }

  @Override
  public void doCommit(final PageData data) throws Exception {
    saveContent(data.getContent());
    saveAttributes(data.getProperties());
    this.versionsController.prune(this);
  }

  @Override
  protected PageData makePageData() throws Exception {
    final PageData pagedata = new PageData(this);
    loadContent(pagedata);
    loadAttributes(pagedata);
    pagedata.addVersions(this.versionsController.history(this));
    return pagedata;
  }

  public PageData getDataVersion(final String versionName) throws Exception {
    return this.versionsController.getRevisionData(this, versionName);
  }

  private void createDirectoryIfNewPage() throws Exception {
    String pagePath = getFileSystemPath();
    File baseDir = new File(pagePath);
    if (!baseDir.exists()) {
      baseDir.mkdirs();
      cmSystem.update(pagePath);
    }
  }

  @Override
  protected VersionInfo makeVersion() throws Exception {
    final PageData data = getData();
    return makeVersion(data);
  }

  protected VersionInfo makeVersion(final PageData data) throws Exception {
    return this.versionsController.makeVersion(this, data);
  }

  protected void removeVersion(final String versionName) throws Exception {
    this.versionsController.removeVersion(this, versionName);
  }

  @Override
  public String toString() {
    try {
      return getClass().getName() + " at " + this.getFileSystemPath();
    } catch (final Exception e) {
      return super.toString();
    }
  }

  class CmSystem {
    public void update(String fileName) throws Exception {
      invokeCmMethod("cmUpdate", fileName);
    }


    public void edit(String fileName) throws Exception {
      invokeCmMethod("cmEdit", fileName);
    }

    public void delete(String fileToBeDeleted) throws Exception {
      invokeCmMethod("cmDelete", fileToBeDeleted);
    }

    private void invokeCmMethod(String method, String newPagePath) throws Exception {
      if (getCmSystemClassName() != null) {
        try {
          Class<?> cmSystem = Class.forName(getCmSystemClassName());
          Method updateMethod = cmSystem.getMethod(method, String.class, String.class);
          updateMethod.invoke(null, newPagePath, getCmSystemVariable());
        } catch (Exception e) {
          System.err.println("Could not invoke static " + method + "(path,payload) of " + getCmSystemClassName());
          e.printStackTrace();
        }
      }
    }

    private String getCmSystemClassName() throws Exception {
      String cmSystemVariable = getCmSystemVariable();
      if (cmSystemVariable == null)
        return null;
      String cmSystemClassName = cmSystemVariable.split(" ")[0].trim();
      if (cmSystemClassName == null || cmSystemClassName.equals(""))
        return null;

      return cmSystemClassName;
    }

    private String getCmSystemVariable() throws Exception {
      return getData().getVariable("CM_SYSTEM");
    }
  }
}
