package fitnesse.runner;

import java.io.InputStream;

public interface ResultFormatter extends ResultHandler
{
	int getByteCount() throws Exception;
	InputStream getResultStream() throws Exception;
}
