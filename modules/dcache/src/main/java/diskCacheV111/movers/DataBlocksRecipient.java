/*
 * ChecksumUpdateRecepient.java
 *
 * Created on March 29, 2005, 11:33 AM
 */

package diskCacheV111.movers;

/**
 *
 * @author  timur
 */
public interface DataBlocksRecipient {
     public  void receiveEBlock( byte[] array, int offset, int length,
    long offsetOfArrayInFile );
}
