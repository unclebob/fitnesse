/*
 * @author Rick Mugridge on Jan 10, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.graphic;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import fitLibrary.FitLibraryFixture;

/**
 * Used to check that the Dot file associated with the GIF matches the expected
 * Dot file contents.
 * It assumes that Dot is installed, as it runs it to produce a GIF for an actual
 * value that doesn't match.
 * This general approach can be used with any image-making scheme.
 */
public class DotGraphic implements GraphicInterface {
    private static final Random random = new Random(System.currentTimeMillis());
    protected String dot;
    
    public DotGraphic(String dot) {
        this.dot = dot;
    }
    public static DotGraphic parseGraphic(File file) {
    	if (FitLibraryFixture.IN_FITNESSE)
    		return new DotGraphic(getDot(file.getName()));
    	else
    		return new DotGraphic(getDot("files/"+file.getName()));
    }
    public boolean equals(Object other) {
        if (!(other instanceof DotGraphic))
            return false;
        return dot.equals(((DotGraphic)other).dot);
    }
    public File toGraphic() {
        try {
            return actualImageFile(dot);
        } catch (IOException ex) {
            throw new RuntimeException("Problem with Dot: "+ex);
        }
    }
    public String toString() {
        return GraphicTypeAdapter.toImageLink(toGraphic());
    }
	private File actualImageFile(String actualDot) throws IOException {
	    final String actuals = "tempActuals";
	    String actualName = actuals+"/actual"+random.nextInt(999999);
        File actualDiry = FitLibraryFixture.getRelativeFile(actuals);
	    if (!actualDiry.exists())
	        actualDiry.mkdir();
	    File dotFile = FitLibraryFixture.getRelativeFile(actualName+".dot");
	    FileWriter writer = new FileWriter(dotFile);
	    writer.write(actualDot);
	    writer.close();
	    File imageFile = FitLibraryFixture.getRelativeFile(actualName+".gif");
	    // Run dot on the file to produce GIF
        Process process = Runtime.getRuntime().exec("dot -Tgif \""+dotFile.getAbsolutePath()+"\" -o \""+
		        imageFile.getAbsolutePath()+"\"");
        try {
            process.waitFor();
        } catch (InterruptedException e1) {
            throw new RuntimeException("Dot process timed out.");
        }
        if (process.exitValue() != 0)
            throw new RuntimeException("Problems with actual Dot:\n"+actualDot);
	    return imageFile;
	}
	private static String getDot(String imageFileName) {
        return getFileContents(imageFileName,".dot");
	}
    private static String getFileContents(String imageFileName, String imageSuffix) {
        File file = FitLibraryFixture.getRelativeFile(imageFileName.replaceFirst(".gif",imageSuffix));
	    FileReader reader;
	    try {
	        reader = new FileReader(file);
	        char[] chars = new char[(int)file.length()];
	        reader.read(chars);
	        return new String(chars);
	    } catch (IOException ex) {
	        throw new RuntimeException("Problem reading "+imageSuffix+
	                " file from "+file.getAbsolutePath()+": "+ex);
	    }
    }
}
