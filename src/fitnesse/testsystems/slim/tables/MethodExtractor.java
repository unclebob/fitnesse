package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.regex.Matcher;

import org.json.JSONArray;
import org.json.JSONObject;

public class MethodExtractor{
	private final String version = "1.0";
	
	private final ArrayList<MethodExtractorRule> configurations;

	public MethodExtractor() {
		super();
		this.configurations = new ArrayList<MethodExtractorRule>();
	}
	
	public MethodExtractor(String configString) {
		super();
		this.configurations = new ArrayList<MethodExtractorRule>();
		
			JSONObject jo = new JSONObject(configString);
		
		String fV= jo.getString("FormatVersion");
		if (!version.equals(fV)) throw new IllegalArgumentException("JSON Mesage has version '" + fV + "'. This class expects version '"+ version +"'." );
		JSONArray configList = jo.getJSONArray("MethodExtractorRules");
		for (int i=0;i< configList.length(); i++){
			JSONObject config = configList.getJSONObject(i);
			String scopePattern = config.getString("Scope");
			String methodName = config.getString("TargetName");
			String parameterNames = config.getString("Parameters");
			add(new MethodExtractorRule(scopePattern, methodName, parameterNames));
		}
	}

	public boolean add(String scope, String targetName, String parameters){
		return add(new MethodExtractorRule(scope, targetName, parameters));
	}
	
	public boolean add(MethodExtractorRule config){
		return configurations.add(config);
	}
	

	public MethodExtractorResult findRule(String methodName){
		for(int i=0; i< configurations.size(); i++){
			  Matcher m = configurations.get(i).matcher(methodName);
			  if(m.matches()){
				  // The order of the next two lines is important. Don't change it
				  ArrayList<String>  parameterObjects = configurations.get(i).getParameterList(m);
				  methodName = configurations.get(i).getMethodName(m);
				  return new MethodExtractorResult(methodName, parameterObjects);	
			  }
		}
		return null;
	}
	public String toString(){
		StringBuilder sb = new StringBuilder("ME:[");
		for(int i=0; i< configurations.size(); i++){
			sb.append( configurations.get(i).toString());
		}
		sb.append("]");
		return sb.toString();
	}

	public String toJson(){
		StringBuilder sb = new StringBuilder("{\n\"FormatVersion\":\""+version+"\",\n\"MethodExtractorRules\":[\n");
		for(int i=0; i< configurations.size(); i++){
			if(i>0)sb.append(", ");
			sb.append( configurations.get(i).toJson());
		}
		sb.append("]\n}\n");
		return sb.toString();
	}
}