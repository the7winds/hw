package torrent.tracker;

import java.util.*;


/**
 * Created by the7winds on 27.03.16.
 */
public class ClientsInfo {

    private final Map<byte[], Map<Short, Collection<Integer>>> clinetInfoToIds = new HashMap<>();
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
            ClientInfo clientInfo = (ClientInfo) obj;
            return port == clientInfo.port && Arrays.equals(ip, clientInfo.ip);
        }
    }

    synchronized void addClient(byte[] ip, short port, Collection<Integer> ids) {
        clinetInfoToIds.putIfAbsent(ip, new HashMap<>());
        clinetInfoToIds.get(ip).put(port, ids);
        ClientInfo clientInfo = new ClientInfo(ip, port);
        for (int id : ids) {
            sources.putIfAbsent(id, new TreeSet<>());
            sources.get(id).add(clientInfo);
        }
    }

    synchronized void removeClient(byte[] ip, short port) {
        Collection<Integer> ids = clinetInfoToIds.get(ip).get(port);
        clinetInfoToIds.remove(ip);
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
