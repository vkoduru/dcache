package diskCacheV111.clients.vsp ;

import java.io.* ;
import java.util.* ;
import dmg.util.* ;


public class VspDeviceShell {
   private class DataEater implements VspDataTransferrable {
      private long _sum = 0 ;
      public long getDataTransferred(){ return _sum ; }
      public void dataArrived( VspConnection vsp ,
                   byte [] buffer , int offset , int size ){
           _sum += size ;
      }
      public void dataRequested( VspConnection v ,  
                                 byte [] b , int o , int s ){}

   }

   public static void main( String [] arg ) throws Exception {
      new VspDeviceShell( arg ) ;
   }
   public VspDeviceShell( String [] arg )throws Exception {    
      if( arg.length < 3 ){
         System.err.println("Usage : ... <host> <port> <replyHost>" ) ;
         System.exit(4);
      }
      String host = arg[0] ;
      int    port = Integer.parseInt( arg[1] ) ;
      String replyHost = arg[2] ;
      
      VspDevice vsp = new VspDevice( host , port , replyHost ) ;
      

      BufferedReader br = new BufferedReader(
                          new InputStreamReader( System.in) ) ;      

      String line = null ;
      int session = 0 ;
      boolean debug = false ;
      VspConnection _currentConnection = null ;
      Hashtable     _hash              = new Hashtable() ;
      int           _nextSession       = 100 ;
      while( true ){
         System.out.print( "["+session+"] > " ) ;
         line = br.readLine() ;
         if( line == null )break ;
         Args args = new Args( line ) ;
         if( args.argc() > 0 ){
             String command = args.argv(0) ;
             args.shift() ;
             if( command.equals("cd") ){
                if( args.argc() < 1 ){ 
                    System.err.println("cd <session>") ; 
                    continue ;
                }
                try{
                   int s  = Integer.parseInt(args.argv(0)) ;
                   if( s == 0 ){
                      session = 0 ;
                      _currentConnection = null ;
                      continue ;
                   }
                   VspConnection c = (VspConnection)_hash.get( Integer.valueOf(s) ) ;
                   if( c == null ){
                      System.err.println( "No such session "+s ) ;
                      continue ;
                   }
                   _currentConnection = c ;
                   session = s ;
                }catch(Exception e){
                   System.err.println("E > "+e) ; 
                   continue ;                    
                }

             }else if( command.equals( "ls" ) ){
                Enumeration e = _hash.keys() ;
                while( e.hasMoreElements() ){
                   System.out.println( e.nextElement().toString() ) ;
                }
             }else if( command.equals( "exit" ) ){
                System.exit(0);
             }else if( command.equals( "close" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                try{
                   _currentConnection.close() ;
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "remove" ) ){
                _hash.remove(Integer.valueOf(session));
                session = 0 ;
                _currentConnection = null ;
             }else if( command.equals( "sync" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                try{
                   _currentConnection.sync() ;
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "show" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                try{
                   System.out.println( "Position  : "+
                                       _currentConnection.getPosition() ) ;
                   System.out.println( "Length    : "+
                                       _currentConnection.getLength() ) ;
                   System.out.println( "BytesRead : "+
                                       _currentConnection.getBytesRead() ) ;
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "query" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                try{
                   _currentConnection.query() ;
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "setsync" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                if( args.argc() < 1 ){
                   System.err.println( "setsync on|off" ) ;
                   continue ;
                }
                boolean sync = args.argv(0).equals( "on" ) ;
                _currentConnection.setSynchronous(sync) ;
             }else if( command.equals( "write" ) ){
                if( args.argc() < 1 ){ 
                    System.err.println("write <bytes>") ; 
                    continue ;
                }
                final int     l = Integer.parseInt(args.argv(0)) ;
                final byte [] d = new byte[l] ;
                Enumeration n = null ;
                for( int m = 0 ; m < 2 ; m++ ){
                   if( _currentConnection == null ){
                       n = _hash.elements() ;
                   }else{
                       Vector v = new Vector() ;
                       v.addElement(_currentConnection);
                       n = v.elements() ;
                   }
                   while( n.hasMoreElements() ){
                      VspConnection c = (VspConnection)n.nextElement() ;
                      try{
                         if( m == 0 ){c.write(d,0,l);}else{c.sync();} ;
                      }catch(Exception e ){
                         System.err.println( "E > "+e ) ;
                         if(debug)e.printStackTrace() ;
                      }
                   }
                }
             }else if( command.equals( "read" ) ){
                if( args.argc() < 1 ){ 
                    System.err.println("read <bytes>") ; 
                    continue ;
                }
                int l = Integer.parseInt(args.argv(0)) ;
                byte [] d = new byte[l] ;
                Enumeration n = null ;
                for( int m = 0 ; m < 2 ; m++ ){
                   if( _currentConnection == null ){
                       n = _hash.elements() ;
                   }else{
                       Vector v = new Vector() ;
                       v.addElement(_currentConnection);
                       n = v.elements() ;
                   }
                   while( n.hasMoreElements() ){
                      VspConnection c = (VspConnection)n.nextElement() ;
                      try{
                         if( m == 0 ){c.read(d,0,l);}else{c.sync();} ;
                      }catch(Exception e ){
                         System.err.println( "E > "+e ) ;
                         if(debug)e.printStackTrace() ;
                      }
                   }
                }
             }else if( command.equals( "loopread" ) ){
                if( args.argc() < 1 ){ 
                    System.err.println("loopread <count> [<blocksize>/kbytes]") ; 
                    continue ;
                }
                final int count = Integer.parseInt(args.argv(0)) ;
                int xx = args.argc() < 2 ? (1024*1024) : ( Integer.parseInt(args.argv(1)) * 1024 ) ;
                final int bsize = xx ;
                if( _currentConnection != null ){
                   System.out.println( "Need to be run in '0' mode" ) ;
                   continue ;
                }
                System.out.println("Switching to 'sync' mode" ) ;
                Enumeration n = _hash.elements() ;
                while( n.hasMoreElements() )
                   ((VspConnection)n.nextElement()).setSynchronous(true) ;
                
                n = _hash.elements() ;
                for( int ix = 0 ; n.hasMoreElements() ; ix++ ){
                  final VspConnection c = (VspConnection)n.nextElement() ;
                  final int countPosition = ix ; 
                  new Thread( 
                     new Runnable(){
                        public void run(){
                           System.out.println( "Starting "+countPosition ) ;
                           byte [] d = new byte[bsize] ;
                           long sum = 0 ;
                           long start = System.currentTimeMillis() ;
                           try{
                              for( int i = 0 ; i < count ; i++ ){
                                 int r = 0 ;
                                 while( true ){                       
                                   r = (int)c.read(d,0,d.length) ;
                                   sum += r ;
                                   if( r < d.length )break ;
                                 } 
                                 c.seek(0,0);
                              }                         
                           }catch(Exception e ){
                              System.err.println( "E > "+e ) ;
                              e.printStackTrace();
                           }
                           long diff = System.currentTimeMillis() - start ;
                           if( diff == 0 ){
                              System.out.println( "Done : ??? MB/sec" ) ;
                           }else{
                              double rate =  ((double)sum) / ((double)diff) / 1024. / 1024. * 1000.;
                              System.out.println( "Done : "+rate+" MB/sec" ) ;
                           }
                        }
                     }
                  ).start() ;
                }
             }else if( command.equals( "sread" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                if( args.argc() < 1 ){ 
                    System.err.println("sread <bytes>") ; 
                    continue ;
                }
                try{
                    int l = Integer.parseInt(args.argv(0)) ;
                    byte [] d = new byte[l] ;
                   _currentConnection.read(d,0,d.length) ;
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "fullread" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                if( args.argc() < 1 ){ 
                    System.err.println("fullread <bytes>") ; 
                    continue ;
                }
                try{
                    int l = Integer.parseInt(args.argv(0)) ;
                    DataEater de = new DataEater() ;
                    
                   long start = System.currentTimeMillis() ;
                   _currentConnection.read(l, de ) ;
                   _currentConnection.sync() ;
                   System.out.println( "Data transferred : "+de.getDataTransferred());
                   long diff = System.currentTimeMillis() - start ;
                   if( diff == 0 ){
                      System.out.println( "Done : ??? MB/sec" ) ;
                   }else{
                      double rate =  ((double)de.getDataTransferred()) / ((double)diff) / 1024. / 1024. * 1000.;
                      System.out.println( "Done : "+rate+" MB/sec" ) ;
                   }
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "seek" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                if( args.argc() < 1 ){ 
                    System.err.println("seek <offset> [<whence>]") ; 
                    continue ;
                }
                try{
                    long offset = Long.parseLong(args.argv(0)) ;
                    int whence  = 0 ;
                    if( args.argc() > 1 )whence = Integer.parseInt(args.argv(1)) ;
                   _currentConnection.seek( offset , whence ) ;
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "seekandread" ) ){
                if( _currentConnection == null ){
                   System.err.println("Not an active connection") ;
                   continue ;
                }
                if( args.argc() < 2 ){ 
                    System.err.println("seekandread <size> <offset> [<whence>]") ; 
                    continue ;
                }
                try{
                    int size = Integer.parseInt(args.argv(0)) ;
                    long offset = Long.parseLong(args.argv(1)) ;
                    int whence  = 0 ;
                    if( args.argc() > 2 )whence = Integer.parseInt(args.argv(2)) ;
                    byte [] d = new byte[size] ;
                   _currentConnection.seek_and_read( d , 0 , offset , whence , size ) ;
                }catch(Exception e ){
                    System.err.println( "E > "+e ) ;
                    if(debug)e.printStackTrace() ;
                }
             }else if( command.equals( "debug" ) ){
                vsp.setDebugOutput(true) ;
                debug=true;
             }else if( command.equals( "nodebug" ) ){
                vsp.setDebugOutput(false) ;
                debug=false;
             }else if( command.equals( "open" ) ){
                if( args.argc() < 2 ){ 
                    System.err.println("open <pnfsId> <mode> [-count=<count>]") ; 
                    continue ;
                }
                String count = args.getOpt("count" ) ;
                if( count == null ){
                   try{
                      VspConnection con = vsp.open( args.argv(0), args.argv(1) ) ;
                      int s = ++_nextSession ;
                      _hash.put( Integer.valueOf(s) , con ) ;
                      _currentConnection = con ;
                      session = s ;
                   }catch(Exception e ){
                       System.err.println( "E > "+e ) ;
                       if(debug)e.printStackTrace() ;
                   }
                }else{
                   int c = 0 ;
                   try{
                      c = Integer.parseInt(count) ;
                   }catch(IllegalArgumentException iae ){
                      System.err.println("Not an integer : "+count) ;
                      continue ;
                   }
                   for( int i = 0 ; i < c ; i++ ){
                      try{
                         VspConnection con = vsp.open( args.argv(0), args.argv(1) ) ;
                         int s = ++_nextSession ;
                         _hash.put( Integer.valueOf(s) , con ) ;
                         con.sync();
                         System.out.println( "Session O.K : "+s ) ;
                      }catch(Exception e ){
                          System.err.println( "E > "+e ) ;
                          if(debug)e.printStackTrace() ;
                          break ;
                      }
                      _currentConnection = null ;
                      session = 0 ;
                   }
                }
             }else if( command.equals("help") ){
                System.out.println(" I/O commands" ) ;
                System.out.println(" ------------" ) ;
                System.out.println("  open <pnfsId> r|w  [-count=<count>]# opens channel -> new <id>") ;
                System.out.println("  close              # closes channel");
                System.out.println("  read <bytes>       # requests <bytes> bytes");
                System.out.println("  write <bytes>      # writes <bytes> bytes");
                System.out.println("  query              # send stat command (see show)");
                System.out.println("  show               # return result of stat") ;
                System.out.println("  seek <position> <whence=0|1|2> # send seek request");
                System.out.println("  sync               # synchronizes last command" ) ;
                System.out.println("  loopread <count> [<blocksize>] # read all id's");
                System.out.println(" Admin commands" ) ;
                System.out.println(" --------------" ) ;
                System.out.println("  ls                 # show active/inactive <ids>");
                System.out.println("  debug|nodebug      # starts stops debug output" ) ;
                System.out.println("  remove <id>        # removes and <id>") ;
                System.out.println("  setsync on|off     # switches sync mode" ) ;
             }else{
                System.err.println("command not known : "+command ) ;
             }

         }
      }
   
   
   }

}