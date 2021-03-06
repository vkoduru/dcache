package dmg.cells.applets.spy ;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;

import dmg.cells.network.CellDomainNode;
import dmg.cells.nucleus.CellDomainInfo;
import dmg.cells.nucleus.CellTunnelInfo;


public class CanonTopo {

   private String   []  _domainNames ;
   private LinkPair []  _linkPairs ;

   /**
    *   The CanonTopo helper class created a canonical form
    *   of a topology. This makes it possible to compare
    *   different topoligies which are essentially identical.
    */
   public CanonTopo(){}
   public CanonTopo( CellDomainNode [] in ){
      _domainNames = new String[in.length] ;
      //
      // copy the domain names into _domainNames
      //
      for( int i = 0 ; i < in.length ; i++ ) {
          _domainNames[i] = in[i].getName();
      }
      //
      // get some kind of order ( canonical ) into names
      //
      _sort( _domainNames ) ;
      //
      // produce a 'name to index' hash
      //
      Hashtable<String, Integer> nameHash = new Hashtable<>() ;

      for( int i= 0 ; i < in.length ; i++ ) {
          nameHash.put(_domainNames[i], i);
      }
      //
      // produce the 'link hash'
      // the hashtable will essentially remove
      // the duplicated entries.
      //
      Hashtable<LinkPair, LinkPair> linkHash = new Hashtable<>() ;
//      System.out.println( "Creating linkHashtable" ) ;

       for (CellDomainNode node : in) {
           String thisDomain = node.getName();
           int thisPosition = nameHash.get(thisDomain);
//          System.out.println( "  domain "+thisDomain+" at position "+thisPosition ) ;
           CellTunnelInfo[] links = node.getLinks();
           if (links == null) {
               continue;
           }

           for (CellTunnelInfo link : links) {
               CellDomainInfo info = link.getRemoteCellDomainInfo();
               if (info == null) {
                   continue;
               }
               String thatDomain = info.getCellDomainName();
               int thatPosition = nameHash.
                       get(thatDomain);
               LinkPair pair = new LinkPair(thisPosition, thatPosition);
//             System.out.println( "     link "+thatDomain+"  : "+pair ) ;
               linkHash.put(pair, pair);
           }
       }
      _linkPairs    = new LinkPair[linkHash.size()] ;
       Iterator<LinkPair> iterator = linkHash.values().iterator();
      for( int i = 0  ; iterator.hasNext(); i++ ){
         _linkPairs[i] = iterator.next();
      }
      _sort( _linkPairs ) ;
      //

   }
   public int links(){ return _linkPairs.length ; }
   public int domains(){ return _domainNames.length ; }
   public String getDomain( int i ){ return _domainNames[i] ; }
   public LinkPair getLinkPair( int i ){ return _linkPairs[i] ; }

    @Override
    public boolean equals(Object other)
    {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CanonTopo)) {
            return false;
        }
        CanonTopo topo = (CanonTopo) other;
        return
            Arrays.equals(_domainNames, topo._domainNames) &&
            Arrays.equals(_linkPairs, topo._linkPairs);
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(_domainNames) ^ Arrays.hashCode(_linkPairs);
    }

   private void _sort( LinkPair [] x ){
      if( x.length < 2 ) {
          return;
      }
      LinkPair y;

      for( int j = 1 ; j < x.length ; j++ ){
         for( int i = 0 ; i < (x.length-1) ; i++ ){
            if( x[i].compareTo( x[i+1] ) > 0 ){
               y = x[i] ; x[i] = x[i+1] ; x[i+1] = y ;
            }
         }
      }
   }
   private void _sort( String [] x ){
      if( x.length < 2 ) {
          return;
      }
      String y;
      for( int j = 1 ; j < x.length ; j++ ){
         for( int i = 0 ; i < (x.length-1) ; i++ ){
            if( x[i].compareTo( x[i+1] ) > 0 ){
               y = x[i] ; x[i] = x[i+1] ; x[i+1] = y ;
            }
         }
      }

   }
   public static void main( String [] args ){
       LinkPair [] linkPairs = new LinkPair[4] ;
       CanonTopo x = new CanonTopo();
       int pos = 0 ;
       linkPairs[pos++] = new LinkPair( 1 , 10 ) ;
       linkPairs[pos++] = new LinkPair( 4 , 5 ) ;
       linkPairs[pos++] = new LinkPair( 7 , 10 ) ;
       linkPairs[pos++] = new LinkPair( 1 , 10 ) ;

       x._sort( linkPairs ) ;
       Hashtable<LinkPair, LinkPair> hash = new Hashtable<>() ;
       for( int i = 0 ; i < linkPairs.length ; i++ ){
         System.out.println( " "+i+" "+linkPairs[i] ) ;
         hash.put( linkPairs[i] , linkPairs[i] ) ;
       }
       System.out.println( " hash entries : "+hash.size() ) ;
   }
   public static void main2( String [] args ){
      CanonTopo x = new CanonTopo() ;
      x._sort( args ) ;
       for (String arg : args) {
           System.out.println(" --> " + arg);
       }

   }
}
