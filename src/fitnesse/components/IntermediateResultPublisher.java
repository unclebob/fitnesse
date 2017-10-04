package fitnesse.components;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import org.json.JSONObject;

import java.rmi.Naming;

public class IntermediateResultPublisher {
  public static void publishStatusToRMI(TestPage sourcePage, TestSummary summary) {
    try {
      String serverAddress = sourcePage.getVariable(WikiPageIdentity.INTERMEDIATE_RESULT_PUBLISHER_SERVER_ADDRESS);
      RMIInterface lookUpServer = (RMIInterface) Naming.lookup(serverAddress);
      lookUpServer.publish(getStringifiedMessageToPublish(sourcePage, summary));
    } catch (Exception e) {
      System.out.println("Error during publishing result : " + e.toString());

    }
  }

  private static String getStringifiedMessageToPublish(TestPage sourcePage, TestSummary summary) {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("testName", sourcePage.getName());
    jsonObject.put("testPath", sourcePage.getFullPath());
    jsonObject.put("right", summary.getRight());
    jsonObject.put("wrong", summary.getWrong());
    jsonObject.put("ignores", summary.getIgnores());
    jsonObject.put("exceptions", summary.getExceptions());
    return jsonObject.toString();
  }
}
