package fitnesse.components;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import org.json.JSONObject;

import java.rmi.Naming;

public class MessagePublisher {
  public static void publishStatusToJMSSend(TestPage sourcePage, TestSummary summary) {
    try {

      String serverAddress = sourcePage.getVariable(WikiPageIdentity.INTERMEDIATE_RESULT_PUBLISHER_SERVER_ADDRESS);
      RMIInterface look_up = (RMIInterface) Naming.lookup(serverAddress);
      look_up.publish(getStringifiedMessageToPublish(sourcePage, summary));
    } catch (Exception e) {
      System.out.println("Error during publishing result to JMS: " + e.toString());

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
