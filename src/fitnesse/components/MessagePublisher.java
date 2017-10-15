package fitnesse.components;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import javax.jms.*;

public class MessagePublisher {
  public static void publishStatusToJMSSend(TestPage sourcePage, TestSummary summary) {
    try {
      String textToPublish = getStringifiedMessageToPublish(sourcePage, summary);
      String serverAddress = sourcePage.getVariable(WikiPageIdentity.MESSAGE_BROKER_ADDRESS);
      String queueName = sourcePage.getVariable(WikiPageIdentity.QUEUE_NAME);

      ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(serverAddress);
      Connection connection = connectionFactory.createConnection();
      Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

      connection.start();
      Destination destination = session.createQueue(queueName);
      MessageProducer producer = session.createProducer(destination);
      producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
      //To ensure real-time consumption of the messages by subscribers, the message will be available only for a minute to consume.
      producer.setTimeToLive(60000);
      TextMessage message = session.createTextMessage(textToPublish);
      producer.send(message);
      session.close();
      connection.close();
    } catch (Exception e) {
      System.out.println("Error during publishing result to JMS: "+e.toString());
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

