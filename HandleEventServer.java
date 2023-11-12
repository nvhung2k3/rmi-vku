package rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HandleEventServer extends Remote{
	void registerClient(String nickname, HandleEventClient client) throws RemoteException;
    void unregisterClient(String nickname) throws RemoteException;
}
