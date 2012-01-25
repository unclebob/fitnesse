package fitnesse.updates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;

import fitnesse.FitNesseContext;
import fitnesse.Updater;

public class UpdaterBase implements Updater {
  public FitNesseContext context;
  public Properties rootProperties;
  public Update[] updates;  

  public UpdaterBase(FitNesseContext context) throws IOException {
    this.context = context;
    rootProperties = loadProperties();
  }

  public Properties getProperties() {
    return rootProperties;
  }

  public Properties loadProperties() throws IOException {
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

  private File getPropertiesFile() {
    String filename = context.rootPagePath + "/properties";
    return new File(filename);
  }

  public void saveProperties() throws IOException {
    OutputStream os = null;
    File propFile = null;
    try {
      propFile = getPropertiesFile();
      os = new FileOutputStream(propFile);
      writeProperties(os);
    } catch (IOException e) {
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

  public void update() throws IOException {
    Update[] updates = getUpdates();
    for (int i = 0; i < updates.length; i++) {
      Update update = updates[i];
      if (update.shouldBeApplied())
        performUpdate(update);
    }
    saveProperties();
  }

  private void performUpdate(Update update) {
    try {
      print(update.getMessage());
      update.doUpdate();
    }
    catch (Exception e) {
      print("\n\t" + e + "\n");
    }
  }

  private Update[] getUpdates() {
    return updates;
  }

  private void print(String message) {
    if (!UpdaterImplementation.testing)
      System.out.print(message);
  }
}
