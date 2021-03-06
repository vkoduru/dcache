package dmg.protocols.ssh ;

import dmg.security.cipher.StreamCipher;


public class SshCmsgExecShell extends SshPacket {

    public SshCmsgExecShell( StreamCipher cipher ){
        super( cipher ) ;
    }
    public SshCmsgExecShell( ){
        super( null ) ;
    }
    @Override
    public byte [] toByteArray(){
       return super.makePacket( new byte[0] ) ;
    }
    @Override
    public byte [] toByteArray(StreamCipher cipher ){
       return super.makePacket( cipher , new byte[0] ) ;
    }
}
