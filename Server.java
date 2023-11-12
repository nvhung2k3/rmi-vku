package rmi;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        try {
            HandleEventServer server = new ChatServerImpl();
            Registry registry = LocateRegistry.createRegistry(4545);
            registry.rebind("ChatServer", server);
            System.out.println("Chat server is running.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
