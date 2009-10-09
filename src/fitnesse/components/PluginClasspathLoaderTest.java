package fitnesse.components;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PluginClasspathLoaderTest {

    private PluginClasspathLoader loader;

    @Before
    public void setUp() throws Exception {
        loader = new PluginClasspathLoader();
    }


    @Test
    public void loadClassPath() {
        List<String> jars = listOfJarsInClasspath();
        assertFalse(jars.contains("testJar1.jar"));
        assertFalse(jars.contains("testJar2.jar"));
        int originalJarCount = jars.size();

        loader.addPluginsToClasspath();

        List<String> newListOfJars = listOfJarsInClasspath();
        assertEquals(originalJarCount + 2, newListOfJars.size());
        assertTrue(newListOfJars.contains("testJar1.jar"));
        assertTrue(newListOfJars.contains("testJar2.jar"));
    }

    private List<String> listOfJarsInClasspath() {
        List<String> classpathElements = Arrays.asList(loader.getClasspath().split(loader.getPathSeperator()));
        List<String> jars = new ArrayList<String>();
        for (String classpathElement : classpathElements) {
            int slashPosition = classpathElement.lastIndexOf(System.getProperty("file.separator"));
            String jarName = classpathElement.substring(slashPosition + 1);
            jars.add(jarName);
        }
        return jars;
    }
}
