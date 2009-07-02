package fitnesse.updates;

import fitnesse.Updater;
import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

import java.util.Properties;
import java.util.Arrays;
import java.util.Enumeration;
import java.io.*;

public class UpdaterBase implements Updater {
  public FitNesseContext context;
  public Properties rootProperties;
  public Update[] updates;  

  public UpdaterBase(FitNesseContext context) throws Exception {
    this.context = context;
    rootProperties = loadProperties();
  }

  public Properties getProperties() {
    return rootProperties;
  }

  public Properties loadProperties() throws Exception {
    Properties properties = new Properties();
    File propFile = getPropertiesFile();
    if (propFile.exists()) {
      InputStream is = null;
      try {
        is = new FileInputStream(propFile);
        properties.load(is);
      } finally {
        if (is != null)
          is.close();
      }
    }
    return properties;
  }

  private File getPropertiesFile() throws Exception {
    String filename = context.rootPagePath + "/properties";
    return new File(filename);
  }

  public void saveProperties() throws Exception {
    OutputStream os = null;
    File propFile = null;
    try {
      propFile = getPropertiesFile();
      os = new FileOutputStream(propFile);
      writeProperties(os);
    } catch (Exception e) {
      String fileName = (propFile != null) ? propFile.getAbsolutePath() : "<unknown>";
      System.err.println("Filed to save properties file: \"" + fileName + "\". (exception: " + e + ")");
      throw e;
    } finally {
      if (os != null)
        os.close();
    }
  }

  private void writeProperties(final OutputStream OutputStream)
    throws IOException {
    BufferedWriter awriter;
    awriter = new BufferedWriter(new OutputStreamWriter(OutputStream, "8859_1"));
    awriter.write("#FitNesse properties");
    awriter.newLine();
    Object[] keys = rootProperties.keySet().toArray(new Object[0]);
    Arrays.sort(keys);
    for (Enumeration<Object> enumeration = rootProperties.keys(); enumeration
      .hasMoreElements();) {
      String key = (String) enumeration.nextElement();
      String val = (String) rootProperties.get(key);
      awriter.write(key + "=" + val);
      awriter.newLine();
    }
    awriter.flush();
  }

  public void update() throws Exception {
    Update[] updates = getUpdates();
    for (int i = 0; i < updates.length; i++) {
      Update update = updates[i];
      if (update.shouldBeApplied())
        performUpdate(update);
    }
    saveProperties();
  }

  private void performUpdate(Update update) throws Exception {
    try {
      print(update.getMessage());
      update.doUpdate();
    }
    catch (Exception e) {
      print("\n\t" + e + "\n");
    }
  }

  private Update[] getUpdates() throws Exception {
    return updates;
  }

  private void print(String message) {
    if (!UpdaterImplementation.testing)
      System.out.print(message);
  }
}
