// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.ComponentFactory;
import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.wikitext.parser.WikiWordPath;
import util.Clock;
import util.DiskFileSystem;
import util.FileSystem;
import util.FileUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Date;

public class FileSystemPage extends CachingPage {
  private static final long serialVersionUID = 1L;

  public static final String contentFilename = "/content.txt";
  public static final String propertiesFilename = "/properties.xml";

  private final String path;
  private final VersionsController versionsController;
  private CmSystem cmSystem = new CmSystem();

  public FileSystemPage(final String path, final String name, final FileSystem fileSystem, final ComponentFactory componentFactory) {
    super(name, null);
    this.path = path;

    versionsController = createVersionsController(componentFactory);
    createDirectoryIfNewPage(fileSystem);
  }

  public FileSystemPage(final String path, final String name) {
    this(path, name, new DiskFileSystem(), new ComponentFactory());
  }

    public FileSystemPage(final String name, final FileSystemPage parent, final FileSystem fileSystem) {
        super(name, parent);
        path = parent.getFileSystemPath();
        versionsController = parent.versionsController;
        createDirectoryIfNewPage(fileSystem);
    }

  private VersionsController createVersionsController(ComponentFactory factory) {
    return (VersionsController) factory.createComponent(ComponentFactory.VERSIONS_CONTROLLER,
      ZipFileVersionsController.class);
  }

  @Override
  public void removeChildPage(final String name) {
    super.removeChildPage(name);
    String pathToDelete = getFileSystemPath() + "/" + name;
    final File fileToBeDeleted = new File(pathToDelete);
    cmSystem.preDelete(pathToDelete);
    FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
    cmSystem.delete(pathToDelete);
  }

  @Override
  public boolean hasChildPage(final String pageName) {
    final File f = new File(getFileSystemPath() + "/" + pageName);
    if (f.exists()) {
      addChildPage(pageName);
      return true;
    }
    return false;
  }

  protected synchronized void saveContent(String content) {
    if (content == null) {
      return;
    }

    final String separator = System.getProperty("line.separator");

    if (content.endsWith("|")) {
      content += separator;
    }
    
    //First replace every windows style to unix
    content = content.replaceAll("\r\n", "\n");
    //Then do the replace to match the OS.  This works around
    //a strange behavior on windows.
    content = content.replaceAll("\n", separator);

    String contentPath = getFileSystemPath() + contentFilename;
    final File output = new File(contentPath);
    OutputStreamWriter writer = null;
    try {
      if (output.exists())
        cmSystem.edit(contentPath);
      writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
      writer.write(content);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException("Can not decode file " + output, e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (writer != null) {
        FileUtil.close(writer);
        cmSystem.update(contentPath);
      }
    }
  }

  protected synchronized void saveAttributes(final WikiPageProperties attributes)
    {
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
      throw new RuntimeException("Failed to save properties file: \""
        + propertiesFilePath + "\" (exception: " + e + ").", e);
    } finally {
      if (output != null) {
        FileUtil.close(output);
        cmSystem.update(propertiesFilePath);
      }
    }
  }

  private void removeAlwaysChangingProperties(WikiPageProperties properties) {
    properties.remove(PageData.PropertyLAST_MODIFIED);
  }

  @Override
  protected WikiPage createChildPage(final String name) {
    //return new FileSystemPage(getFileSystemPath(), name, this, this.versionsController);
    return new PageRepository().makeChildPage(name, this);
  }

  private void loadContent(final PageData data) {
    String content = "";
    final String name = getFileSystemPath() + contentFilename;
    final File input = new File(name);
    try {
      if (input.exists()) {
        final byte[] bytes = readContentBytes(input);
        content = new String(bytes, "UTF-8");
      }
      data.setContent(content);
    } catch (IOException e) {
      throw new RuntimeException("Error while loading content", e);
    }
  }

  @Override
  protected void loadChildren() {
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
    if (WikiWordPath.isWikiWord(filename)) {
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

  private void loadAttributes(final PageData data) {
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

  private long getLastModifiedTime() {
    long lastModifiedTime = 0;

    final File file = new File(getFileSystemPath() + contentFilename);
    if (file.exists()) {
      lastModifiedTime = file.lastModified();
    } else {
      lastModifiedTime = Clock.currentTimeInMillis();
    }
    return lastModifiedTime;
  }

  private void attemptToReadPropertiesFile(File file, PageData data,
                                           long lastModifiedTime) throws FileNotFoundException {
    InputStream input = null;
    try {
      final WikiPageProperties props = new WikiPageProperties();
      input = new FileInputStream(file);
      props.loadFromXmlStream(input);
      props.setLastModificationTime(new Date(lastModifiedTime));
      data.setProperties(props);
    } finally {
      if (input != null)
        FileUtil.close(input);
    }
  }

  @Override
  public void doCommit(final PageData data) {
    saveContent(data.getContent());
    saveAttributes(data.getProperties());
    this.versionsController.prune(this);
  }

  @Override
  protected PageData makePageData() {
    final PageData pagedata = new PageData(this);
    loadContent(pagedata);
    loadAttributes(pagedata);
    pagedata.addVersions(this.versionsController.history(this));
    return pagedata;
  }

  public PageData getDataVersion(final String versionName) {
    return this.versionsController.getRevisionData(this, versionName);
  }

  private void createDirectoryIfNewPage(FileSystem fileSystem) {
    String pagePath = getFileSystemPath();
    if (!fileSystem.exists(pagePath)) {
      try {
        fileSystem.makeDirectory(pagePath);
      } catch (IOException e) {
        throw new RuntimeException("Unable to create directory for new page", e);
      }
      cmSystem.update(pagePath);
    }
  }

  @Override
  protected VersionInfo makeVersion() {
    final PageData data = getData();
    return makeVersion(data);
  }

  protected VersionInfo makeVersion(final PageData data) {
    return this.versionsController.makeVersion(this, data);
  }

  protected void removeVersion(final String versionName) {
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
    public void update(String fileName) {
      invokeCmMethod("cmUpdate", fileName);
    }


    public void edit(String fileName) {
      invokeCmMethod("cmEdit", fileName);
    }

    public void delete(String fileToBeDeleted) {
      invokeCmMethod("cmDelete", fileToBeDeleted);
    }

    public void preDelete(String fileToBeDeleted) {
      invokeCmMethod("cmPreDelete", fileToBeDeleted);
    }

    private void invokeCmMethod(String method, String newPagePath) {
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

    private String getCmSystemClassName() {
      String cmSystemVariable = getCmSystemVariable();
      if (cmSystemVariable == null)
        return null;
      String cmSystemClassName = cmSystemVariable.split(" ")[0].trim();
      if (cmSystemClassName == null || cmSystemClassName.equals(""))
        return null;

      return cmSystemClassName;
    }

    private String getCmSystemVariable() {
      return getData().getVariable("CM_SYSTEM");
    }
  }
}
