// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;

import fitnesse.ContextConfigurator;
import fitnesse.plugins.PluginException;
import org.junit.Test;

public class ArgumentsTest {

  @Test(expected = IllegalArgumentException.class)
  public void testBadArgument() throws Exception {
    new Arguments("-x");
  }

  @Test
  public void defaultConfigLocation() {
    Arguments args = new Arguments();
    assertEquals("./plugins.properties", args.getConfigFile(ContextConfigurator.systemDefaults()));
  }

  @Test
  public void configLocationWithDifferentRootPath() {
    Arguments args = new Arguments("-d", "customDir");
    assertEquals("customDir/plugins.properties", args.getConfigFile(ContextConfigurator.systemDefaults()));
  }

  @Test
  public void customConfigLocation() {
    Arguments args = new Arguments("-f", "custom.properties");
    assertEquals("custom.properties", args.getConfigFile(ContextConfigurator.systemDefaults()));
  }

  @Test
  public void argumentsCanBeRepresentedByProperties() throws IOException, PluginException {
    Arguments args = new Arguments("-v", "-p", "81", "-d", "directory", "-r", "root", "-b", "someFile.txt",
              "-l", "myLogDirectory", "-o", "-e", "22", "-f", "fitnesse.properties", "-i", "-c", "SomeCommand", "-a", "user:pass", "-lh", "-w", "42");
    Properties properties = args.update(ContextConfigurator.systemDefaults()).makeFitNesseContext().getProperties();

    assertEquals("verbose", properties.getProperty("LogLevel"));
    assertEquals("81", properties.getProperty("Port"));
    assertEquals("directory", properties.getProperty("RootPath"));
    assertEquals("root", properties.getProperty("FitNesseRoot"));
    assertEquals("someFile.txt", properties.getProperty("RedirectOutput"));
    assertEquals("myLogDirectory", properties.getProperty("LogDirectory"));
    assertEquals("true", properties.getProperty("OmittingUpdates"));
    assertEquals("22", properties.getProperty("VersionsController.days"));
    assertEquals("fitnesse.properties", properties.getProperty("ConfigFile"));
    assertEquals("true", properties.getProperty("InstallOnly"));
    assertEquals("SomeCommand", properties.getProperty("Command"));
    assertEquals("user:pass", properties.getProperty("Credentials"));
    assertEquals("true", properties.getProperty("LocalhostOnly"));
    assertEquals("42", properties.getProperty("MaximumWorkers"));
  }

  @Test
  public void defaultArgumentsAsProperties() throws IOException, PluginException {
    Arguments args = new Arguments();
    Properties properties = args.update(ContextConfigurator.empty()).makeFitNesseContext().getProperties();

    assertEquals("normal", properties.getProperty("LogLevel"));
    assertNull(properties.getProperty("ConfigFile"));
    assertEquals(".", properties.getProperty("RootPath"));
    assertEquals("FitNesseRoot", properties.getProperty("FitNesseRoot"));
    assertNull(properties.getProperty("RedirectOutput"));
    assertNull(properties.getProperty("LogDirectory"));
    assertNull(properties.getProperty("OmittingUpdates"));
    assertNull(properties.getProperty("VersionsController.days"));
    assertNull(properties.getProperty("InstallOnly"));
    assertNull(properties.getProperty("Command"));
    assertNull(properties.getProperty("Credentials"));
    assertNull(properties.getProperty("LocalhostOnly"));
    assertEquals("100", properties.getProperty("MaximumWorkers"));
  }

}
