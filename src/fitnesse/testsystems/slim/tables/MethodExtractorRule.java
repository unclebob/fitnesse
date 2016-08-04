package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodExtractorRule {
	private final String scopePattern;
	private final String methodNamePattern;
	private final String parameterListString;
	private final String[] parameterList;

	private final Pattern scope;

	public MethodExtractorRule(String scopePattern,
			String methodNamePattern, String parameterList) {
		super();
		this.scopePattern = scopePattern;
		this.methodNamePattern = methodNamePattern;
		this.parameterListString = parameterList;

		this.parameterList = getParameterList().split(",");
		this.scope = Pattern.compile(this.scopePattern);

	}
	public String getScopePattern() {
		return scopePattern;
	}
	public String getMethodNamePattern() {
		return methodNamePattern;
	}
	public String getParameterList() {
		return parameterListString;
	}

	public Matcher matcher(String methodName){
		return scope.matcher(methodName);
	}

	public ArrayList<String> getParameterList(Matcher m){
		  ArrayList<String>  parameterObjects = new ArrayList<String>();
		  for (int i=0; i< parameterList.length; i++){
			  if (!parameterList[i].isEmpty()){
				  String parameter =parameterList[i];
				  if(parameter.startsWith("$")){
					  String groupName = parameter.substring(1);
					  try{
						  int groupID = Integer.parseInt(groupName);
						  parameterObjects.add( m.group(groupID));
					  }catch (NumberFormatException e){
						  // if it is not a number than it must be a named group
						  parameterObjects.add(m.group(groupName));
					  }
				  }
				  else{
					  parameterObjects.add(parameter);
				  }
			  }
		  }
		  return parameterObjects;

	}
	public String getMethodName(Matcher m) {

		return m.replaceAll(getMethodNamePattern());
	}

	public String toString(){
		return "Scope:"+ scopePattern + ";TargetName:"+methodNamePattern+";Parameters:"+parameterListString;
	}

	public StringBuilder toJson(){
		StringBuilder sb = new StringBuilder("{\n\"Scope\":\"");
		sb.append(getScopePattern().replaceAll("\\\\", "\\\\\\\\"));
		sb.append("\",\n\"TargetName\":\"");
		sb.append(getMethodNamePattern().replaceAll("\\\\", "\\\\\\\\"));
		sb.append("\",\n\"Parameters\":\"");
		sb.append(getParameterList().replaceAll("\\\\", "\\\\\\\\"));
		sb.append("\"\n}\n");
		return sb;

	}

}
