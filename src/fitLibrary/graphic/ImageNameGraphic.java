/*
 * @author Rick Mugridge on Jan 11, 2005
 *
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary.graphic;

import java.io.File;

import fitLibrary.FitLibraryFixture;
import fitLibrary.FitLibraryFixture;

/**
 * Used to check whether the name of the image is as expected.
 */
public class ImageNameGraphic implements GraphicInterface {
    private File expectedFile;

    public ImageNameGraphic(File expectedFile) {
        this.expectedFile = expectedFile;
    }
    public ImageNameGraphic(String expectedFileName) {
        this.expectedFile = FitLibraryFixture.getRelativeFile(expectedFileName);
    }
    public File toGraphic() {
        return expectedFile;
    }
    public boolean equals(Object object) {
        if (!(object instanceof ImageNameGraphic))
                return false;
//        System.out.println("Compare "+ expectedFile.getName()+" and "+
//        		((ImageNameGraphic)object).expectedFile.getName());
        return expectedFile.getName().equals(
                ((ImageNameGraphic)object).expectedFile.getName());
    }
    public static GraphicInterface parseGraphic(File expectedFile) {
        return new ImageNameGraphic(expectedFile);
    }
}
