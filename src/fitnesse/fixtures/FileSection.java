package fitnesse.fixtures;

import fit.*;
import java.io.File;
import fitnesse.util.FileUtil;

public class FileSection extends Fixture
{
	public void doTable(Parse table)
	{
		try
		{
			String arg = getArgs()[0];
			if("setup".equals(arg.toLowerCase()))
			{
				new File(FitnesseFixtureContext.baseDir).mkdir();
				File dir = new File(FitnesseFixtureContext.baseDir + "/" + FitnesseFixtureContext.root.getName());
				dir.mkdir();
				new File(dir, "files").mkdir();
			}
			else
			{
				FileUtil.deleteFileSystemDirectory(FitnesseFixtureContext.baseDir);
			}
		}
		catch(Exception e)
		{
			exception(table, e);
		}
	}
}
