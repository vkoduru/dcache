package org.dcache.services.info;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import diskCacheV111.util.CacheException;
import diskCacheV111.util.TimeoutCacheException;

import dmg.cells.nucleus.CellEndpoint;
import dmg.cells.nucleus.CellPath;
import dmg.util.HttpException;
import dmg.util.HttpRequest;
import dmg.util.HttpResponseEngine;

import org.dcache.cells.CellStub;
import org.dcache.vehicles.InfoGetSerialisedDataMessage;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * This class provides support for querying the info cell via the admin web-interface.  It
 * implements the HttpResponseEngine to handle requests at a particular point (a specific alias).
 * <p>
 * It provides either a complete XML dump of the info cell or the subtree matching the supplied
 * list of path elements.
 * <p>
 * If the info cell cannot be contacted or takes too long to reply, or there was a problem when
 * (de-)serialising the Message then an appropriate HTTP status code (50x server-side error)
 * is generated.
 * <p>
 * It is anticipated that clients query this information (roughly) once per minute.
 * <p>
 * The implementation caches the XML data obtained from the info cell for one second.  This
 * is a safety feature, reducing the impact on the info cell of pathologically broken clients
 * that make many requests per second.
 * <p>
 * Future versions may include additional functionality, such as server-side transformation of
 * the XML data into another format.
 *
 * @author Paul Millar <paul.millar@desy.de>
 */
public class InfoHttpEngine implements HttpResponseEngine {

	private static Logger _log = LoggerFactory.getLogger( HttpResponseEngine.class);

	private static final String INFO_CELL_NAME = "info";

	/** The maximum age of our cache, in milliseconds */
	private static final long MAX_CACHE_AGE = 1000;

	/** How long we should wait for the info cell to reply before timing out, in milliseconds */
	private static final long INFO_CELL_TIMEOUT = 4000;

	private final CellEndpoint _endpoint;
    private final CellStub _infoCell;

    /** Our local cache of the complete XML data */
	private byte _cache[];
	private Date _whenReceived;


	/**
	 * The constructor simply creates a new nucleus for us to use when sending messages.
	 */
	public InfoHttpEngine(CellEndpoint endpoint, String[] args) {
            if( _log.isInfoEnabled()) {
                _log.info("in InfoHttpEngine constructor");
            }
            _endpoint = endpoint;
            _infoCell = new CellStub(_endpoint, new CellPath(INFO_CELL_NAME), INFO_CELL_TIMEOUT, MILLISECONDS);
	}

	/**
	 * Handle a request for data.  This either returns the cached contents (if
	 * still valid), or queries the info cell for information.
	 */
	@Override
        public void queryUrl(HttpRequest request) throws HttpException {

		List<String> pathElements = null;
		byte recv[];

		String[]    urlItems = request.getRequestTokens();
        OutputStream out     = request.getOutputStream();

        if( _log.isInfoEnabled()) {
        	StringBuilder sb = new StringBuilder();

        	for( String urlItem : urlItems) {
        		if( sb.length() > 0) {
                            sb.append("/");
                        }
         		sb.append( urlItem);
        	}
        	_log.info( "Received request for: " + sb.toString());
        }

        if( urlItems.length > 1) {
        	pathElements = new ArrayList<>( urlItems.length-1);

        	for( int i = 1; i < urlItems.length; i++) {
                    pathElements.add(i - 1, urlItems[i]);
                }
        }

        /**
         * Maintain our cache of XML.  This prevents end-users from thrashing the info cell.
         */
       	try {
       		if( pathElements == null) {
       			if( _whenReceived == null || System.currentTimeMillis() - _whenReceived.getTime() > MAX_CACHE_AGE) {
                    updateXMLCache();
                }
       			recv = _cache;
       		} else {
       			recv = fetchXML( pathElements);
       		}
       	} catch( TimeoutCacheException e) {
               throw new HttpException(503, "The info cell took too long to reply, suspect trouble (" + e.getMessage() + ")");
       	} catch (CacheException e) {
               throw new HttpException(500, "Error when requesting info from info cell. (" + e.getMessage() + ")");
       	} catch( NullPointerException e) {
       		throw new HttpException( 500, "Received no sensible reply from info cell.  See info cell for details.");
       	} catch( InterruptedException e) {
       		throw new HttpException( 503, "Received interrupt whilst processing data. Please try again later.");
       	}

        request.setContentType( "application/xml");

        try {
            request.printHttpHeader( recv.length);
        	out.write( recv);
        } catch( IOException e) {
        	_log.error("IOException caught whilst writing output : " + e.getMessage());
        }
	}

        @Override
        public void startup()
        {
            // This class has no background activity.
        }

        @Override
        public void shutdown()
        {
            // No background activity to shutdown.
        }

	/**
	 * Send a message off to the info cell
	 */
	public void updateXMLCache() throws InterruptedException, CacheException
    {
		_cache = fetchXML( null);
		_whenReceived = new Date();
	}

	/**
	 * Attempt to gather XML data for given path, or complete tree if pathElements is null.
	 * @param pathElements
	 * @return
	 */
	private byte[] fetchXML( List<String> pathElements)
            throws InterruptedException, CacheException
    {
		if( _log.isDebugEnabled()) {
            _log.debug("Attempting to fetch XML +" + (pathElements == null ? "complete" : "partial") + " tree");
        }

		try {
            InfoGetSerialisedDataMessage sendMsg =
                    (pathElements == null) ? new InfoGetSerialisedDataMessage() : new InfoGetSerialisedDataMessage(pathElements);
			InfoGetSerialisedDataMessage reply = _infoCell.sendAndWait(sendMsg);
            String serialisedData = reply.getSerialisedData();
			if( serialisedData == null) {
				/**
				 *  TODO: replyStr == null should only come from a problem within the Info cell
				 *  when serialising the content.  This should be handled by a specific Exception
				 *  that is propagated via the vehicle.
				 */
				throw new NullPointerException();
			}
            return serialisedData.getBytes();
		} catch (InterruptedException e) {
			_cache = null;
			_whenReceived = null;
			throw e;
		}
	}
}
