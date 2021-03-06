package dmg.cells.applets.spy ;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import dmg.cells.nucleus.CellInfo;
import dmg.cells.services.MessageObjectFrame;


class CommandPanel
      extends Panel
      implements ActionListener, FrameArrivable {

   private static final long serialVersionUID = -8855448245610647062L;
   private DomainConnection _connection ;
   private Font   _bigFont   = new Font( "SansSerif" , Font.ITALIC , 18 )  ;
   private Font   _smallFont = new Font( "SansSerif" , Font.ITALIC|Font.BOLD , 14 )  ;
   private Font   _textFont  = new Font( "Monospaced" , Font.BOLD   , 14 )  ;
   //
   // SansSerif, Serif
   //
   private Label     _topLabel  ;
   private Label     _classLabel ;
   private Label     _cellPath  ;
   private TextArea  _outputArea ;
   private TextField _requestField ;
   private CellInfo  _cellInfo;
   private String    _cellAddress;
   private boolean   _useColor;

   CommandPanel( DomainConnection connection ){
      _connection = connection ;
      _useColor   = System.getProperty( "bw" ) == null ;
      if( _useColor ) {
          setBackground(Color.orange);
      }
      setLayout( new BorderLayout() ) ;

      _topLabel = new Label( "<Cell>" , Label.CENTER )  ;
      _topLabel.setFont( _bigFont ) ;

      _classLabel = new Label("<class>") ;
      _classLabel.setFont( _smallFont ) ;

      _cellPath = new Label("<cellPath>") ;
      _cellPath.setFont( _smallFont ) ;

      _outputArea = new TextArea() ;
      _outputArea.setFont( _textFont ) ;

      Panel labelPanel = new Panel( new GridLayout( 0 , 1 ) ) ;
      labelPanel.add( _topLabel ) ;
      labelPanel.add( _classLabel ) ;
      labelPanel.add( _cellPath ) ;

      _requestField = new HistoryTextField() ;
      _requestField.addActionListener( this ) ;

      add( labelPanel   , "North" ) ;
      add( _outputArea  , "Center" ) ;
      add( _requestField  , "South" ) ;

   }
   public void clear(){
       _topLabel.setText("<cellName>");
       _classLabel.setText("<class>") ;
       _cellPath.setText("<cellPath>") ;
       _outputArea.setText("") ;
       _requestField.setText("") ;
   }
   @Override
   public Insets getInsets(){ return new Insets( 10 ,10 ,10 ,10  ) ; }
   @Override
   public void actionPerformed( ActionEvent event ){
       Object source = event.getSource() ;
       if( source == _requestField ){
           updateCell( _requestField.getText() ) ;
           _requestField.setText("");
       }
   }
   @Override
   public void frameArrived( MessageObjectFrame frame ){
       Object obj = frame.getObject() ;
//       System.out.println( "Class arrived : "+obj.getClass().getName() ) ;
       _outputArea.setText( obj.toString() ) ;

   }
   public void showCell( CellInfo info , String address ){
      _cellInfo    = info ;
      _cellAddress = address ;
      clear() ;
      _topLabel.setText( info.getCellName() ) ;
      _classLabel.setText( info.getCellClass() ) ;
      _cellPath.setText( address ) ;

   }
   private void updateCell( String request ){
      if( _cellAddress == null ) {
          return;
      }
      _connection.send( _cellAddress , request , this ) ;

   }

}
