package fitnesse.slim;

import java.util.Map;

public class LibraryMethodExecutor extends MethodExecutor {

  private final Map<String, Object> libraries;
  
  public LibraryMethodExecutor(Map<String, Object> libraries) {
    this.libraries = libraries;
  }
  @Override
  public MethodExecutionResult execute(String instanceName, String methodName, Object[] args)
      throws Throwable {
    for (Object library : libraries.values()) {
      MethodExecutionResult result = findAndInvoke(methodName, args, library);
      if(result.hasResult()) {
        return result;
      }
    }
    return MethodExecutionResult.NO_METHOD_IN_LIBRARIES;
  }

}
