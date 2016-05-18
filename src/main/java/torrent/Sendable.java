package torrent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by the7winds on 26.03.16.
 */
public interface Sendable {

    void read(DataInputStream dataInputStream) throws IOException;

    /** Serialize data to a bytes array and send data via dataOutputStream */

    void write(DataOutputStream dataOutputStream) throws IOException;
}
