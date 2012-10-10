/*
 * Automatically generated by jrpcgen 1.0.7 on 2/21/09 1:22 AM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
package org.dcache.chimera.nfs.v3.xdr;
import org.dcache.xdr.*;
import java.io.IOException;

public class groups implements XdrAble {

    public groupnode value;

    public groups() {
    }

    public groups(groupnode value) {
        this.value = value;
    }

    public groups(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        if ( value != null ) { xdr.xdrEncodeBoolean(true); value.xdrEncode(xdr); } else { xdr.xdrEncodeBoolean(false); }
    }

    @Override
    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        value = xdr.xdrDecodeBoolean() ? new groupnode(xdr) : null;
    }

}
// End of groups.java