package fitnesse.fixtures;

import fit.ColumnFixture;
import java.io.File;

public class FileSectionFileAdder extends ColumnFixture
{
	public String path;
	public String type;

	public boolean valid() throws Exception {
		File file = null;
		if ("dir".equals(type))
		{
			file = new File(FileSection.getFileSection().getPath() + "/" + path);
			file.mkdir();
		}
		else
		{
			file = new File(FileSection.getFileSection().getPath() + "/" + path);
			file.createNewFile();
		}
		return file.exists();
	}
}
