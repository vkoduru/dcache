package org.dcache.pool.movers;
import diskCacheV111.vehicles.ProtocolInfo;
import diskCacheV111.vehicles.StorageInfo;
import diskCacheV111.util.PnfsId;
import diskCacheV111.repository.SpaceMonitor;

import java.io.RandomAccessFile;

public interface MoverProtocol 
{
    public static final int READ = 1;
    public static final int WRITE  = 2;

    public void runIO(RandomAccessFile diskFile,
                      ProtocolInfo protocol,
                      StorageInfo  storage,
                      PnfsId       pnfsId,
                      SpaceMonitor spaceMonitor,
                      int          access)
        throws Exception;

    public void setAttribute(String name, Object attribute);

    public Object getAttribute(String name);

    /**
     * Get number of bytes transfered. The number of bytes may exceed
     * total file size if client does some seek requests in between.
     *
     * @return number of bytes
     */
    public long getBytesTransferred();

    /**
     * Get time between transfers begin and end. If Mover is sill
     * active, then current time used as end.
     *
     * @return transfer time in milliseconds.
     */
    public long getTransferTime();

    /**
     * Get time of last transfer.
     *
     * @return last access time in milliseconds.
     */
    public long getLastTransferred();

    /**
     * Get file modification status.
     *
     * @return true if file was changes.
     */
    public boolean wasChanged();
}
