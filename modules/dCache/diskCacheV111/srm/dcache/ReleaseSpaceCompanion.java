// $Id$
// $Log: not supported by cvs2svn $
// Revision 1.8  2006/09/25 20:30:58  timur
// modify srm companions and srm cell to use ThreadManager
//
// Revision 1.7  2005/10/03 19:00:23  timur
// fixed spelling
//
// Revision 1.6  2005/03/23 18:17:20  timur
// SpaceReservation working for the copy case as well
//
// Revision 1.5  2005/03/07 22:59:26  timur
// more work on space reservation
//
// Revision 1.4  2005/03/01 23:12:09  timur
// Modified the database scema to increase database operations performance and to account for reserved space"and to account for reserved space
//
// Revision 1.3  2005/01/25 05:17:31  timur
// moving general srm stuff into srm repository
//
// Revision 1.2  2004/08/06 19:35:23  timur
// merging branch srm-branch-12_May_2004 into the trunk
//
// Revision 1.1.2.2  2004/06/15 22:15:42  timur
// added cvs logging tags and fermi copyright headers at the top
//
// Revision 1.1.2.1  2004/05/18 21:40:30  timur
// incorporation of the new scheduler into srm, repackaging of all the srm classes
//
// Revision 1.1  2003/11/09 19:51:29  cvs
// first alfa version of srm v2  space reservation functions is complete
//
// Revision 1.1  2003/10/30 00:15:57  cvs
// srmReserveSpace implemented, started working on srmPrepareToPut
//
// Revision 1.6  2003/10/02 18:50:28  cvs
// timur: added cvs version and log in comments
//
/*
COPYRIGHT STATUS:
  Dec 1st 2001, Fermi National Accelerator Laboratory (FNAL) documents and
  software are sponsored by the U.S. Department of Energy under Contract No.
  DE-AC02-76CH03000. Therefore, the U.S. Government retains a  world-wide
  non-exclusive, royalty-free license to publish or reproduce these documents
  and software for U.S. Government purposes.  All documents and software
  available from this server are protected under the U.S. and Foreign
  Copyright Laws, and FNAL reserves all rights.


 Distribution of the software available from this server is free of
 charge subject to the user following the terms of the Fermitools
 Software Legal Information.

 Redistribution and/or modification of the software shall be accompanied
 by the Fermitools Software Legal Information  (including the copyright
 notice).

 The user is asked to feed back problems, benefits, and/or suggestions
 about the software to the Fermilab Software Providers.


 Neither the name of Fermilab, the  URA, nor the names of the contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.



  DISCLAIMER OF LIABILITY (BSD):

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT
  LIMITED TO, THE IMPLIED  WARRANTIES OF MERCHANTABILITY AND FITNESS
  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL FERMILAB,
  OR THE URA, OR THE U.S. DEPARTMENT of ENERGY, OR CONTRIBUTORS BE LIABLE
  FOR  ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
  OF SUBSTITUTE  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE  POSSIBILITY OF SUCH DAMAGE.


  Liabilities of the Government:

  This software is provided by URA, independent from its Prime Contract
  with the U.S. Department of Energy. URA is acting independently from
  the Government and in its own private capacity and is not acting on
  behalf of the U.S. Government, nor as its contractor nor its agent.
  Correspondingly, it is understood and agreed that the U.S. Government
  has no connection to this software and in no manner whatsoever shall
  be liable for nor assume any responsibility or obligation for any claim,
  cost, or damages arising out of or resulting from the use of the software
  available from this server.


  Export Control:

  All documents and software available from this server are subject to U.S.
  export control laws.  Anyone downloading information from this server is
  obligated to secure any necessary Government licenses before exporting
  documents or software obtained from this server.
 */

/*
 * StageAndPinCompanion.java
 *
 * Created on January 2, 2003, 2:08 PM
 */

package diskCacheV111.srm.dcache;

import dmg.cells.nucleus.CellAdapter;
import dmg.cells.nucleus.CellPath;
import dmg.cells.nucleus.CellMessage;
import dmg.cells.nucleus.CellMessageAnswerable;

import diskCacheV111.util.PnfsId;
import org.dcache.srm.ReleaseSpaceCallbacks;

import diskCacheV111.vehicles.Message;
import diskCacheV111.vehicles.spaceManager.SpaceManagerMessage;
import diskCacheV111.vehicles.spaceManager.SpaceManagerReleaseSpaceMessage;

import java.net.InetAddress;

/**
 *
 * @author  timur
 */
/**
 * this class does all the dcache specific work needed for staging and pinning a
 * file represented by a path. It notifies the caller about each next stage
 * of the process via a StageAndPinCompanionCallbacks interface.
 * Boolean functions of the callback interface need to return true in order for
 * the process to continue
 */
public class ReleaseSpaceCompanion implements CellMessageAnswerable {
    private  static final int NOT_WAITING_STATE=0;
    private  static final int WAITING_RELEASE_RESPONSE_STATE=1;
    private  static final int RECEIVED_RELEASE_RESPONSE_STATE=2;
    private volatile int state = NOT_WAITING_STATE;
    private dmg.cells.nucleus.CellAdapter cell;
    private ReleaseSpaceCallbacks callbacks;
    private CellMessage request = null;
    // this is a path of the root directory of the user
    // this is pnfsid of the root
    private PnfsId pnfsId;
    // this is the name of the pool that we will reserve space on
    private String poolname;
    private Long spaceSize;
    private void say(String words_of_wisdom) {
        if(cell!=null) {
            cell.say(" ReleaseSpaceCompanion : "+words_of_wisdom);
        }
    }

    private void esay(String words_of_despare) {
        if(cell!=null) {
            cell.esay(" ReleaseSpaceCompanion : "+words_of_despare);
        }
    }
    private void esay(Throwable t) {
        if(cell!=null) {
            cell.esay(" ReleaseSpaceCompanion exception : ");
            cell.esay(t);
        }
    }

    public static final String getStateString(int state) {
        switch(state) {
            case NOT_WAITING_STATE:
                return "NOT_WAITING_STATE";
            case WAITING_RELEASE_RESPONSE_STATE:
                return "WAITING_RELEASE_RESPONSE_STATE";
            default:
                return "UNKNOWN";
        }
    }


    /** Creates a new instance of StageAndPinCompanion */

    private ReleaseSpaceCompanion(
    String poolname,
    ReleaseSpaceCallbacks callbacks,
    long spaceSize,
    CellAdapter cell) {
        this.cell = cell;
        this.spaceSize = new Long(spaceSize);
        this.callbacks = callbacks;
        this.poolname = poolname;
        say(" constructor poolname = "+poolname+" releaseSpaceSize="+spaceSize);
    }

    private ReleaseSpaceCompanion(
    String poolname,
    ReleaseSpaceCallbacks callbacks,
    CellAdapter cell) {
        this.cell = cell;
        this.callbacks = callbacks;
        this.poolname = poolname;
        say(" constructor poolname = "+poolname);
    }

     public void answerArrived( final CellMessage req , final CellMessage answer ) {
        say("answerArrived");
        diskCacheV111.util.ThreadManager.execute(new Runnable() {
            public void run() {
                processMessage(req,answer);
            }
        });
    }

    private void processMessage( CellMessage req , CellMessage answer ) {
        int current_state = state;
        say("answerArrived, state="+getStateString(current_state));
        request = req;
        Object o = answer.getMessageObject();
        if(o instanceof Message) {
            Message message = (Message)answer.getMessageObject() ;
            if( message instanceof SpaceManagerReleaseSpaceMessage  &&
            current_state == WAITING_RELEASE_RESPONSE_STATE) {
                state=RECEIVED_RELEASE_RESPONSE_STATE;
                say("SpaceManagerReleaseSpaceMessage  arrived");
                if(message.getReturnCode() != 0) {
                    esay("SpaceManagerReleaseSpaceMessage message.getReturnCode () != 0");
                    callbacks.ReleaseSpaceFailed(
                    "SpaceManagerReleaseSpaceMessage."+
                    "getReturnCode () != 0 ");
                    return ;
                }
                say(" Pool freed space reservation");
                SpaceManagerReleaseSpaceMessage releaseSpaceMessage =
                (SpaceManagerReleaseSpaceMessage)message;

                callbacks.SpaceReleased();
                return;
            }
            else {
                esay("ignoring unexpected message : "+message);
                //callbacks.ReserveSpaceFailed("unexpected message arrived:"+message);
                return ;
            }
        }
        else {
            esay(" got unknown object. ignoring "+
            " : "+o);
            //callbacks.Error(this.toString ()+" got unknown object "+
            //" : "+o) ;
        }
    }

    public void exceptionArrived( CellMessage request , Exception exception ) {
        esay("exceptionArrived "+exception+" for request "+request);
        callbacks.Exception(exception);
    }
    public void answerTimedOut( CellMessage request ) {
        esay("answerTimedOut for request "+request);
        callbacks.Timeout();
    }

    public String toString() {

        return this.getClass().getName()+
        "poolname = "+poolname+" releaseSpaceSize="+spaceSize;
    }

    public static void releaseSpace(
    String spaceToken,
    long spaceSize,
    ReleaseSpaceCallbacks callbacks,
    CellAdapter cell) {
        cell.say(" ReleaseSpaceCompanion.releaseSpace(spaceToken = "+
        spaceToken+" releaseSpaceSize="+spaceSize+")");

        SpaceManagerReleaseSpaceMessage releaseSpaceMessage =
        new SpaceManagerReleaseSpaceMessage(Long.parseLong(spaceToken) ,spaceSize);

        ReleaseSpaceCompanion companion =
        new ReleaseSpaceCompanion(
        spaceToken,
        callbacks,
        spaceSize,
        cell);

        companion.state = WAITING_RELEASE_RESPONSE_STATE;
        try {
            cell.sendMessage( new CellMessage(
            new CellPath("SpaceManager") ,
            releaseSpaceMessage ) ,
            true , true ,
            companion ,
            1*24*60*60*1000) ;
        }
        catch(Exception ee ) {
            callbacks.ReleaseSpaceFailed("can not contact space manager: " +
            ee.toString());
        }
    }

    public static void releaseSpace(
    String spaceToken,
    ReleaseSpaceCallbacks callbacks,
    CellAdapter cell) {
        cell.say(" ReleaseSpaceCompanion.releaseSpace(spaceToken = "+
        spaceToken+" (releasing all available space))");

        SpaceManagerReleaseSpaceMessage releaseSpaceMessage =
        new SpaceManagerReleaseSpaceMessage(Long.parseLong(spaceToken));

        ReleaseSpaceCompanion companion =
        new ReleaseSpaceCompanion(
        spaceToken,
        callbacks,
        cell);

        companion.state = WAITING_RELEASE_RESPONSE_STATE;
        try {
            cell.sendMessage( new CellMessage(
            new CellPath("SpaceManager") ,
            releaseSpaceMessage ) ,
            true , true ,
            companion ,
            1*24*60*60*1000) ;
        }
        catch(Exception ee ) {
            callbacks.ReleaseSpaceFailed("can not contact space manager: " +
            ee.toString());
        }
    }

}
