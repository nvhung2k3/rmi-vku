package rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ChatServerImpl extends UnicastRemoteObject implements HandleEventServer {
    private Map<String, HandleEventClient> clients = new HashMap<>();

    public ChatServerImpl() throws RemoteException {
        // Constructor
    }

	@Override
	public void registerClient(String nickname, HandleEventClient client) throws RemoteException {
		client.addAllClient(clients);
		for (HandleEventClient clientItem: clients.values()) {
			clientItem.addClient(nickname, client);
		}
		clients.put(nickname, client);
	}

	@Override
	public void unregisterClient(String nickname) throws RemoteException {
		 clients.remove(nickname);
	}
}
