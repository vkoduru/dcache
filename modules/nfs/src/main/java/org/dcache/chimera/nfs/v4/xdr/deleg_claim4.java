/*
 * Automatically generated by jrpcgen 1.0.7 on 2/21/09 1:22 AM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
package org.dcache.chimera.nfs.v4.xdr;
import org.dcache.xdr.*;
import java.io.IOException;

public class deleg_claim4 implements XdrAble {
    public int dc_claim;
    public int dc_delegate_type;

    public deleg_claim4() {
    }

    public deleg_claim4(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        xdrDecode(xdr);
    }

    @Override
    public void xdrEncode(XdrEncodingStream xdr)
           throws OncRpcException, IOException {
        xdr.xdrEncodeInt(dc_claim);
        switch ( dc_claim ) {
        case open_claim_type4.CLAIM_FH:
            break;
        case open_claim_type4.CLAIM_DELEG_PREV_FH:
            break;
        case open_claim_type4.CLAIM_PREVIOUS:
            xdr.xdrEncodeInt(dc_delegate_type);
            break;
        }
    }

    @Override
    public void xdrDecode(XdrDecodingStream xdr)
           throws OncRpcException, IOException {
        dc_claim = xdr.xdrDecodeInt();
        switch ( dc_claim ) {
        case open_claim_type4.CLAIM_FH:
            break;
        case open_claim_type4.CLAIM_DELEG_PREV_FH:
            break;
        case open_claim_type4.CLAIM_PREVIOUS:
            dc_delegate_type = xdr.xdrDecodeInt();
            break;
        }
    }

}
// End of deleg_claim4.java