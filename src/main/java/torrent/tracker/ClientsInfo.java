package torrent.tracker;

import java.net.*;
import java.util.*;


/**
 * Created by the7winds on 27.03.16.
 */
public class ClientsInfo {

    private final Map<InetSocketAddress, Collection<Integer>> clientInfoToIds = new HashMap<>();
    private final Map<Integer, Collection<ClientInfo>> sources = new Hashtable<>();

    public final static class ClientInfo {
        public byte[] ip;
        public short port;

        public ClientInfo(byte[] ip, short port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClientInfo) {
                ClientInfo clientInfo = (ClientInfo) obj;
                return port == clientInfo.port && Arrays.equals(ip, clientInfo.ip);
            }
            return false;
        }
    }

    synchronized void addClient(byte[] ip, short port, Collection<Integer> ids) throws UnknownHostException {
        InetSocketAddress address = new InetSocketAddress(Inet4Address.getByAddress(ip), port);
        clientInfoToIds.put(address, ids);
        ClientInfo clientInfo = new ClientInfo(ip, port);
        for (int id : ids) {
            sources.putIfAbsent(id, new HashSet<>());
            sources.get(id).add(clientInfo);
        }
    }

    synchronized void removeClient(byte[] ip, short port) throws UnknownHostException {
        InetSocketAddress address = new InetSocketAddress(Inet4Address.getByAddress(ip), port);
        Collection<Integer> ids = clientInfoToIds.get(address);
        clientInfoToIds.remove(ip);
        ClientInfo clientInfo = new ClientInfo(ip, port);
        for (Integer id : ids) {
            sources.get(id).remove(clientInfo);
        }
    }

    synchronized Collection<ClientInfo> getSources(int id) {
        sources.putIfAbsent(id, new HashSet<>());
        return sources.get(id);
    }
}
