package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;

public final class MethodExtractorResult{
		public final String methodName;
		public final ArrayList<String> parameterNames;

		public MethodExtractorResult(String methodName,
				ArrayList<String> parameterNames) {
			this.methodName = methodName;
			this.parameterNames = parameterNames;
		}
		public String toString(){
			return methodName + ":" + parameterNames.toString();
		}
		public String getDisgracedMethodName(){
			return Disgracer.disgraceMethodName(methodName);
		}
		public String getMethodName(){
			return methodName;
		}
		public String getParameters(){
			return parameterNames.toString();
		}
		
		public Object[] mergeParameters(Object[] args) {
			Object[] newArgs = new Object[parameterNames.size()+args.length];
		  	  for (int i=0; i< parameterNames.size();i++) newArgs[i] = parameterNames.get(i);
		  	  for (int i=0; i< args.length;i++) newArgs[i+ parameterNames.size()] = args[i];
		  	  args = newArgs;
			return args;
		}

}