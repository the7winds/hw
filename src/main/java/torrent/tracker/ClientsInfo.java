package torrent.tracker;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;


/**
 * Created by the7winds on 27.03.16.
 */
public class ClientsInfo {

    private final Map<SocketAddress, Collection<Integer>> clientInfoToIds =
            Collections.synchronizedMap(new HashMap<>());
    private final Map<Integer, Collection<InetSocketAddress>> sources =
            Collections.synchronizedMap(new HashMap<>());


    synchronized void addClient(InetSocketAddress socketAddress, Collection<Integer> ids) {
        clientInfoToIds.put(socketAddress, ids);
        for (int id : ids) {
            sources.computeIfAbsent(id, (k) -> new HashSet<>()).add(socketAddress);
        }
    }

    synchronized void removeClient(InetSocketAddress socketAddress) {
        Collection<Integer> ids = clientInfoToIds.get(socketAddress);
        clientInfoToIds.remove(socketAddress);
        for (Integer id : ids) {
            sources.get(id).remove(socketAddress);
        }
    }

    synchronized Collection<InetSocketAddress> getSources(int id) {
        return new HashSet<>(sources.getOrDefault(id, new HashSet<>()));
    }
}
