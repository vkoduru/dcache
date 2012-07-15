package org.dcache.xrootd.pool;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;

import org.dcache.pool.movers.MoverChannel;
import org.dcache.vehicles.XrootdProtocolInfo;
import org.dcache.xrootd.protocol.messages.ReadRequest;
import org.dcache.xrootd.protocol.messages.SyncRequest;
import org.dcache.xrootd.protocol.messages.WriteRequest;

/**
 * Encapsulates an open file in the xrootd data server.
 */
public interface FileDescriptor
{
    /**
     * Returns a reader object for a given read request. The reader
     * provides read access to the file and can generate response
     * objects for the request.
     */
    Reader read(ReadRequest msg);

    /**
     * Forces unwritten data to disk.
     *
     * @throws ClosedChannelException if the descriptor is closed.
     * @throws IOException if the operation failed.
     */
    void sync(SyncRequest msg)
        throws IOException;

    /**
     * Writes data to the file.
     *
     * @throws ClosedChannelException if the descriptor is closed.
     * @throws IOException if the operation failed.
     */
    void write(WriteRequest msg)
        throws IOException;

    /**
     * Returns the FileChannel of this descriptor.
     */
    MoverChannel<XrootdProtocolInfo> getChannel();
}
