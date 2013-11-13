package fitnesse.updates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.FitNesseContext;
import fitnesse.Updater;

public class UpdaterBase implements Updater {
  protected static final Logger LOG = Logger.getLogger(UpdaterBase.class.getName());

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
    String filename = context.getRootPagePath() + "/properties";
    return new File(filename);
  }

  public void saveProperties() throws IOException {
    OutputStream os = null;
    File propFile = null;
    try {
      propFile = getPropertiesFile();
      os = new FileOutputStream(propFile);
      rootProperties.store(os, "#FitNesse properties");
    } catch (IOException e) {
      String fileName = (propFile != null) ? propFile.getAbsolutePath() : "<unknown>";
      LOG.log(Level.SEVERE, "Filed to save properties file: \"" + fileName + "\". (exception: " + e + ")");
      throw e;
    } finally {
      if (os != null)
        os.close();
    }
  }

  public boolean update() throws IOException {
    Update[] updates = getUpdates();
    for (int i = 0; i < updates.length; i++) {
      Update update = updates[i];
      if (update.shouldBeApplied())
        performUpdate(update);
    }
    saveProperties();
    return true;
  }

  private void performUpdate(Update update) {
    try {
      //LOG.info(update.getMessage());
      update.doUpdate();
    }
    catch (Exception e) {
      LOG.log(Level.SEVERE, "Update failed", e);
    }
  }

  private Update[] getUpdates() {
    return updates;
  }

}
