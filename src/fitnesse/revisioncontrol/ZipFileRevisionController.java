package fitnesse.revisioncontrol;

import static fitnesse.revisioncontrol.NullState.ADDED;
import static fitnesse.revisioncontrol.NullState.DELETED;
import static fitnesse.revisioncontrol.NullState.VERSIONED;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import fitnesse.util.StreamReader;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.NoSuchVersionException;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageVersionPruner;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class ZipFileRevisionController implements RevisionController {
    public static SimpleDateFormat dateFormat() {
        return new SimpleDateFormat("yyyyMMddHHmmss");
    }

    public ZipFileRevisionController() {
        this(new Properties());
    }

    public ZipFileRevisionController(Properties properties) {
    }

    public State add(String... filePaths) throws RevisionControlException {

        return ADDED;
    }

    public State checkin(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    public State checkout(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    public State checkState(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    public State delete(String... filePaths) throws RevisionControlException {

        return DELETED;
    }

    public State execute(RevisionControlOperation operation, String... filePaths) throws RevisionControlException {
        return VERSIONED;
    }

    public PageData getRevisionData(FileSystemPage page, String label) throws Exception {
        String filename = page.getFileSystemPath() + "/" + label + ".zip";
        File file = new File(filename);
        if (!file.exists())
            throw new NoSuchVersionException("There is no version '" + label + "'");

        PageData data = new PageData(page);
        ZipFile zipFile = new ZipFile(file);
        loadVersionContent(zipFile, data);
        loadVersionAttributes(zipFile, data);
        data.addVersions(loadVersions(page));
        zipFile.close();
        return data;
    }

    public State getState(String state) {
        return VERSIONED;
    }

    public Collection<VersionInfo> history(FileSystemPage page) throws Exception {
        File dir = new File(page.getFileSystemPath());
        File[] files = dir.listFiles();
        Set<VersionInfo> versions = new HashSet<VersionInfo>();
        if (files != null)
            for (File file : files)
                if (isVersionFile(file))
                    versions.add(new VersionInfo(makeVersionName(file)));
        return versions;
    }

    public boolean isExternalReversionControlEnabled() {
        return false;
    }

    public VersionInfo makeVersion(FileSystemPage page, PageData data) throws Exception {
        String dirPath = page.getFileSystemPath();
        Set filesToZip = getFilesToZip(dirPath);

        VersionInfo version = makeVersionInfo(data);

        if (filesToZip.size() == 0)
            return new VersionInfo("first_commit", "", new Date());

        String filename = makeVersionFileName(page, version.getName());
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(filename));

        for (Iterator iterator = filesToZip.iterator(); iterator.hasNext();)
            addToZip((File) iterator.next(), zos);

        zos.finish();
        zos.close();
        return new VersionInfo(version.getName());
    }

    public void prune(FileSystemPage page) throws Exception {
        PageVersionPruner.pruneVersions(page, history(page));
    }

    public void removeVersion(FileSystemPage page, String versionName) throws Exception {
        String versionFileName = makeVersionFileName(page, versionName);
        File versionFile = new File(versionFileName);
        versionFile.delete();
    }

    public State revert(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    public State update(String... filePaths) throws RevisionControlException {

        return VERSIONED;
    }

    private void addToZip(File file, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(file.getName());
        zos.putNextEntry(entry);
        FileInputStream is = new FileInputStream(file);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        is.read(bytes);
        is.close();
        zos.write(bytes, 0, size);
    }

    private Set getFilesToZip(String dirPath) {
        Set<File> filesToZip = new HashSet<File>();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null)
            return filesToZip;
        for (File file : files) {
            if (!(isVersionFile(file) || file.isDirectory()))
                filesToZip.add(file);
        }
        return filesToZip;
    }

    private boolean isVersionFile(File file) {
        return Pattern.matches("(\\S+)?\\d+\\.zip", file.getName());
    }

    private void loadVersionAttributes(ZipFile zipFile, PageData data) throws Exception {
        ZipEntry attributes = zipFile.getEntry("properties.xml");
        if (attributes != null) {
            InputStream attributeIS = zipFile.getInputStream(attributes);
            WikiPageProperties props = new WikiPageProperties(attributeIS);
            attributeIS.close();
            data.setProperties(props);
        }
    }

    private void loadVersionContent(ZipFile zipFile, PageData data) throws Exception {
        String content = "";
        ZipEntry contentEntry = zipFile.getEntry("content.txt");
        if (contentEntry != null) {
            InputStream contentIS = zipFile.getInputStream(contentEntry);
            StreamReader reader = new StreamReader(contentIS);
            content = reader.read((int) contentEntry.getSize());
            reader.close();
        }
        data.setContent(content);
    }

    private Collection loadVersions(FileSystemPage page) throws Exception {
        File dir = new File(page.getFileSystemPath());
        File[] files = dir.listFiles();
        Set<VersionInfo> versions = new HashSet<VersionInfo>();
        if (files != null)
            for (File file : files) {
                if (isVersionFile(file))
                    versions.add(new VersionInfo(makeVersionName(file)));
            }
        return versions;
    }

    private String makeVersionFileName(FileSystemPage page, String name) throws Exception {
        return page.getFileSystemPath() + "/" + name + ".zip";
    }

    private VersionInfo makeVersionInfo(PageData data) throws Exception {
        Date time = data.getProperties().getLastModificationTime();
        String versionName = VersionInfo.nextId() + "-" + dateFormat().format(time);
        String user = data.getAttribute(WikiPage.LAST_MODIFYING_USER);
        if (user != null && !"".equals(user))
            versionName = user + "-" + versionName;

        return new VersionInfo(versionName, user, time);
    }

    private String makeVersionName(File file) {
        String name = file.getName();
        return name.substring(0, name.length() - 4);
    }
}
