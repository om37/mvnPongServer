package om37;
import java.io.IOException;
import java.net.*;

import static common.Global.*;
import common.*;

/**
 * Start the game server
 *  The call to makeActiveObject() in the model 
 *   starts the play of the game
 */
class Server
{
  private NetObjectWriter p0, p1;
  ServerSocket ss;
  
  public static void main( String args[] )
  {
   ( new Server() ).start();
  }

  /**
   * Start the server
   */
  public void start()
  {
    DEBUG.set( true );
    DEBUG.trace("Pong Server");
    DEBUG.set(false);
    //DEBUG.set( false );               // Otherwise lots of debug info
    S_PongModel model = new S_PongModel();
    
    try
    {
    	DEBUG.trace("opening server socket...");
		ss = new ServerSocket(Global.PORT);
	} 
    catch (IOException e)
    {
    	e.printStackTrace();
    }  
    
    makeContactWithClients( model );
    
    S_PongView  view  = new S_PongView(p0, p1 );
                        //new S_PongController( model, view );

    model.addObserver( view );       // Add observer to the model
    model.makeActiveObject();        // Start play
  }
  
  /**
   * Make contact with the clients who wish to play
   * Players will need to know about the model
   * @param model  Of the game
   */
  public void makeContactWithClients( S_PongModel model )
  {
	  try
	  {
		  DEBUG.trace("Waiting for first connection");
		  Socket pOneSocket = ss.accept();//Accept first connection/player
		  DEBUG.trace("First connected");
		  PlayerS pZero = new PlayerS(0, model, pOneSocket);//Initialise zeroth player (first to connect)
		  DEBUG.trace("PlayerCreated... Fetching Writer");
		  p0 = pZero.getWriter();//Initialise the writer (player needs to be initialised first)
		  DEBUG.trace("Got first writer");
    	  //Same as above
		  DEBUG.trace("Waiting for second connection");
		  Socket pTwoSocket = ss.accept();
		  DEBUG.trace("Second connected");
		  PlayerS pOne = new PlayerS(1, model, pTwoSocket);
		  p1 = pOne.getWriter();
		  DEBUG.trace("Got second writer");
		  //Start the threads
		  pZero.start();
		  pOne.start();
	  }
	  catch(Exception e){
		  System.out.println("ERROR in SERVER MAKE CONTACT");
	  }
	  DEBUG.trace("Returning from server make contact");
  }
}

/**
 * Individual player run as a separate thread to allow
 * updates to the model when a player moves there bat
 */
class PlayerS extends Thread
{
	private S_PongModel		pModel;//Player's model
	private NetObjectWriter pWriter;//Player's writer
	private NetObjectReader pReader;//Player's reader
	private int 			pNumber;//Player's number
	
  /**
   * Constructor
   * @param player Player 0 or 1
   * @param model Model of the game
   * @param s Socket used to communicate the players bat move
   */
  public PlayerS( int player, S_PongModel model, Socket s  )
  {  
	  //Setup player and give them the model
	  pNumber = player;
	  pModel = model;
	  
	  DEBUG.trace("PlayerS Constructor start");
	  try
	  {
		  pWriter = new NetObjectWriter(s);//and their writer
		  pReader = new NetObjectReader(s);//Initialise their reader		  
		
		DEBUG.trace("PlayerS Constructor end");
	  }
	  catch ( Exception e) {
		  DEBUG.trace("Player Exception");
		  e.printStackTrace();
	  }
	
  }
  
  //Gets the player's writer
  public NetObjectWriter getWriter()
  {
	  DEBUG.trace("Getting player writer....");
	  return pWriter;
  }
  
  
  /**
   * Get and update the model with the latest bat movement
   */
  public void run()                             // Execution
  {
	  //Endless loop, read from client, listen for keyPress message from client's controller
	  //Send message to model, calc new position(inside model)
	  while(true)
	  {
		  String move = (String)pReader.get();//Gets the data from the reader
		  
		  //If data was read, determine if player wants to move up or down
		  
		  if(move != null && !move.equals(""))
		  {			  
			  if(move.equals("UP"))
			  {
				  GameObject pBat = pModel.getBat(pNumber);//Create dummy bat (get player's bat from model)
				  pBat.moveY(-BAT_MOVE);//Move the bat
				  pModel.setBat(pNumber, pBat);//Set the bat again (with new coords)
				  pModel.modelChanged();//Update observers
			  }
			  else if(move.equals("DOWN"))
			  {
				  GameObject pBat = pModel.getBat(pNumber);
				  pBat.moveY(BAT_MOVE);
				  pModel.setBat(pNumber, pBat);
				  pModel.modelChanged();
			  }
		  }
	  }
  }
  
  
}
