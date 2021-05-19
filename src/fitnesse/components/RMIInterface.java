package fitnesse.components;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
  String publish(String name) throws RemoteException;
}
