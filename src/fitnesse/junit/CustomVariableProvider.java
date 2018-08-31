package fitnesse.junit;

import java.util.Map;

/**
 * Provide custom variables to override page variables in a similar way as done with URL-Parameters
 * when using the fitnesse-stabdalone runner
 * @author mwyraz
 */
public interface CustomVariableProvider {
	public Map<String,String> getCustomVariables();
}
