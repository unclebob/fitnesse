// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;

import fitnesse.revisioncontrol.RevisionControlException;
import fitnesse.revisioncontrol.RevisionControlOperation;
import fitnesse.revisioncontrol.RevisionController;
import fitnesse.revisioncontrol.State;
import fitnesse.revisioncontrol.ZipFileRevisionController;
import fitnesse.util.FileUtil;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class FileSystemPage extends CachingPage {
    public static final String contentFilename = "/content.txt";
    public static final String propertiesFilename = "/properties.xml";

    private final String path;
    private final RevisionController revisioner;

    protected FileSystemPage(String path, String name, WikiPage parent, RevisionController revisioner) throws Exception {
        super(name, parent);
        this.path = path;
        this.revisioner = revisioner;
    }

    public static WikiPage makeRoot(String path, String name) throws Exception {
        return makeRoot(path, name, new ZipFileRevisionController());
    }

    public static WikiPage makeRoot(String path, String name, RevisionController revisioner) throws Exception {
        return new FileSystemPage(path, name, null, revisioner);
    }

    @Override
    public void removeChildPage(String name) throws Exception {
        super.removeChildPage(name);
        final File fileToBeDeleted = new File(getFileSystemPath() + "/" + name);
        revisioner.delete(fileToBeDeleted.getAbsolutePath());
        FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
    }

    @Override
    public boolean hasChildPage(String pageName) throws Exception {
        final File f = new File(getFileSystemPath() + "/" + pageName);
        if (f.exists()) {
            addChildPage(pageName);
            return true;
        } else
            return false;
    }

    protected synchronized void saveContent(String content) throws Exception {
        if (content == null)
            return;

        final String separator = System.getProperty("line.separator");

        if (content.endsWith("|"))
            content += separator;

        content = content.replaceAll("\r\n", separator);

        final File output = new File(getFileSystemPath() + contentFilename);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
            writer.write(content);
        } finally {
            if (writer != null)
                writer.close();
        }
    }

    protected synchronized void saveAttributes(WikiPageProperties attributes) throws Exception {
        OutputStream output = null;
        String propertiesFileName = "<unknown>";
        try {
            propertiesFileName = getFileSystemPath() + propertiesFilename;
            output = new FileOutputStream(propertiesFileName);
            attributes.save(output);
        } catch (final Exception e) {
            System.err.println("Failed to save properties file: \"" + propertiesFileName + "\" (exception: " + e + ").");
            e.printStackTrace();
            throw e;
        } finally {
            if (output != null)
                output.close();
        }
    }

    @Override
    protected WikiPage createChildPage(String name) throws Exception {
        final FileSystemPage newPage = new FileSystemPage(getFileSystemPath(), name, this, this.revisioner);
        final File baseDir = new File(newPage.getFileSystemPath());
        baseDir.mkdirs();
        revisioner.add(baseDir.getAbsolutePath());
        return newPage;
    }

    private void loadContent(PageData data) throws Exception {
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
            for (final String subFile : subFiles)
                if (fileIsValid(subFile, thisDir) && !children.containsKey(subFile))
                    children.put(subFile, getChildPage(subFile));
        }
    }

    private byte[] readContentBytes(File input) throws FileNotFoundException, IOException {
        FileInputStream inputStream = null;
        try {
            final byte[] bytes = new byte[(int) input.length()];
            inputStream = new FileInputStream(input);
            inputStream.read(bytes);
            return bytes;
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
    }

    private boolean fileIsValid(String filename, File dir) {
        if (WikiWordWidget.isWikiWord(filename)) {
            final File f = new File(dir, filename);
            if (f.isDirectory())
                return true;
        }
        return false;
    }

    private String getParentFileSystemPath() throws Exception {
        return parent != null ? ((FileSystemPage) parent).getFileSystemPath() : path;
    }

    public String getFileSystemPath() throws Exception {
        return getParentFileSystemPath() + "/" + getName();
    }

    private void loadAttributes(PageData data) throws Exception {
        final File file = new File(getFileSystemPath() + propertiesFilename);
        if (file.exists())
            try {
                attemptToReadPropertiesFile(file, data);
            } catch (final Exception e) {
                System.err.println("Could not read properties file:" + file.getPath());
                e.printStackTrace();
            }
    }

    private void attemptToReadPropertiesFile(File file, PageData data) throws Exception {
        InputStream input = null;
        try {
            final WikiPageProperties props = new WikiPageProperties();
            input = new FileInputStream(file);
            props.loadFromXmlStream(input);
            data.setProperties(props);
        } finally {
            if (input != null)
                input.close();
        }
    }

    @Override
    public void doCommit(PageData data) throws Exception {
        data.getProperties().setLastModificationTime(new Date());
        saveContent(data.getContent());
        saveAttributes(data.getProperties());
        addToReversionControl(contentFilePath());
        addToReversionControl(propertiesFilePath());
        revisioner.prune(this);
    }

    private void addToReversionControl(String filePath) throws RevisionControlException {
        if (revisioner.checkState(filePath).isNotUnderRevisionControl())
            revisioner.add(filePath);
    }

    @Override
    protected PageData makePageData() throws Exception {
        final PageData pagedata = new PageData(this);
        loadContent(pagedata);
        loadAttributes(pagedata);
        pagedata.addVersions(revisioner.history(this));
        return pagedata;
    }

    public PageData getDataVersion(String versionName) throws Exception {
        return revisioner.getRevisionData(this, versionName);
    }

    @Override
    protected VersionInfo makeVersion() throws Exception {
        final PageData data = getData();
        return makeVersion(data);
    }

    protected VersionInfo makeVersion(PageData data) throws Exception {
        return this.revisioner.makeVersion(this, data);
    }

    protected void removeVersion(String versionName) throws Exception {
        revisioner.removeVersion(this, versionName);
    }

    @Override
    public String toString() {
        try {
            return getClass().getName() + " at " + this.getFileSystemPath();
        } catch (final Exception e) {
            return super.toString();
        }
    }

    public void execute(RevisionControlOperation operation) throws Exception {
        operation.execute(revisioner, contentFilePath(), propertiesFilePath());
    }

    private String propertiesFilePath() throws Exception {
        return absolutePath(propertiesFilename);
    }

    private String absolutePath(String fileName) throws Exception {
        return new File(getFileSystemPath() + fileName).getAbsolutePath();
    }

    private String contentFilePath() throws Exception {
        return absolutePath(contentFilename);
    }

    public boolean isExternallyRevisionControlled() {
        return revisioner.isExternalReversionControlEnabled();
    }

    public State checkState() throws Exception {
        return revisioner.checkState(contentFilePath(), propertiesFilePath());
    }
}
