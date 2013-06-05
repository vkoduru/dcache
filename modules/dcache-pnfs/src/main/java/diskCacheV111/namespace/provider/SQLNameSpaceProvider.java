/*
 * $Id: SQLNameSpaceProvider.java,v 1.19 2007-08-22 12:24:38 tigran Exp $
 */

package diskCacheV111.namespace.provider;

import dmg.util.Args;
import diskCacheV111.util.*;
import diskCacheV111.namespace.*;

import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import javax.sql.DataSource;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Required;

import org.dcache.commons.util.SqlHelper;

public class SQLNameSpaceProvider extends AbstractNameSpaceProvider
{
    private static Logger _logNamespace = LoggerFactory.getLogger("logger.org.dcache.namespace.provider");

    private String _tableName = "cacheinfo";

    private DataSource _dbConnectionsPool;

    private final String _addCacheLocationSQL;
    private final String _getCacheLocationSQL;
    private final String _clearCacheLocationSQL;


    public SQLNameSpaceProvider(String arguments)
        throws Exception
    {
        Args args = new Args(arguments);

        String __cfTableName = args.getOpt("cachelocation-provider-tableName");
        if( __cfTableName != null ) {
            _tableName = __cfTableName;
        }

        _addCacheLocationSQL = "INSERT INTO " + _tableName + " VALUES(?,?,?)";
        _getCacheLocationSQL = "SELECT pool FROM " + _tableName + " WHERE pnfsid=? ORDER BY ctime DESC";
        _clearCacheLocationSQL = "DELETE  FROM " + _tableName + " WHERE pnfsid=? AND pool LIKE ?";
    }

    @Required
    public void setDataSource(DataSource dataSource)
    {
        _dbConnectionsPool = dataSource;
    }

    public void addCacheLocation(Subject subject, PnfsId pnfsId, String cacheLocation) throws FileNotFoundCacheException {

    	Connection dbConnection = null;
    	PreparedStatement ps = null;

        try {
            boolean deleted = PnfsFile.isDeleted(pnfsId);
            if (deleted)
                throw new FileNotFoundCacheException("no such file or directory " + pnfsId.getId() );

            dbConnection = _dbConnectionsPool.getConnection();

            ps = dbConnection.prepareStatement(_addCacheLocationSQL);

            ps.setString(1, pnfsId.toString() );
            ps.setString(2,  cacheLocation );
            ps.setTimestamp(3, new Timestamp( System.currentTimeMillis()) );

            int result = ps.executeUpdate( );

        }catch( SQLException e) {
        	String sqlState = e.getSQLState();

        	// according to SQL-92 standard, class-code 23 is
        	// Constraint Violation, in our case
        	// same pool for the same file,
        	// which is OK
        	if( !sqlState.startsWith("23") ) {
        		_logNamespace.error("Failed to add cache location: " + e);
        	}
        }finally{
            SqlHelper.tryToClose(ps);
            tryToClose(dbConnection);
        }

    }

    public void clearCacheLocation(Subject subject, PnfsId pnfsId, String cacheLocation, boolean removeIfLast) throws CacheException
    {
    	Connection dbConnection = null;
    	PreparedStatement ps = null;
        try {

            dbConnection = _dbConnectionsPool.getConnection();

            ps = dbConnection.prepareStatement(_clearCacheLocationSQL);

            ps.setString(1, pnfsId.toString() );
            ps.setString(2,  cacheLocation.equals("*") ? "%" : cacheLocation );

            int result = ps.executeUpdate( );


        }catch( SQLException e) {
        	_logNamespace.error("Failed to clear cache location: " + e);
        }finally{
            SqlHelper.tryToClose(ps);
            tryToClose(dbConnection);
        }
    }

    public List<String> getCacheLocation(Subject subject, PnfsId pnfsId) throws CacheException
    {
        List<String> locations = new ArrayList<String>();

        Connection dbConnection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {

            dbConnection = _dbConnectionsPool.getConnection();

            ps = dbConnection.prepareStatement(_getCacheLocationSQL);
            ps.setString(1, pnfsId.toString() );

            result = ps.executeQuery();

            while( result.next()) {
                locations.add( result.getString("pool") );
            }


        }catch( SQLException e) {
        	_logNamespace.error("Failed to get cache location: " + e);
        }finally{
            SqlHelper.tryToClose(result);
            SqlHelper.tryToClose(ps);
            tryToClose(dbConnection);
        }
        return locations;

    }

   /**
     * database resource cleanup
     */
     static void tryToClose(Connection o) {
         try {
             if (o != null) o.close();
         } catch (SQLException e) {
        	 _logNamespace.error("Failed to close Connection: " + e);
         }
     }


    public String toString() {
        return "$Id: SQLNameSpaceProvider.java,v 1.19 2007-08-22 12:24:38 tigran Exp $";
    }
}