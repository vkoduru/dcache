package org.dcache.webadmin.model.dataaccess.impl;

import diskCacheV111.poolManager.CostModule;
import diskCacheV111.pools.PoolV2Mode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import diskCacheV111.poolManager.PoolMonitorV5;
import diskCacheV111.poolManager.PoolSelectionUnit;
import diskCacheV111.poolManager.PoolSelectionUnit.SelectionPool;
import diskCacheV111.poolManager.PoolSelectionUnit.SelectionPoolGroup;
import diskCacheV111.pools.PoolCostInfo;
import java.util.Collection;
import org.dcache.webadmin.model.dataaccess.communication.ContextPaths;
import org.dcache.webadmin.model.dataaccess.communication.impl.PageInfoCache;
import org.dcache.webadmin.model.exceptions.NoSuchContextException;
import org.dcache.webadmin.model.businessobjects.Pool;
import org.dcache.webadmin.model.dataaccess.PoolsDAO;
import org.dcache.webadmin.model.dataaccess.communication.CellMessageGenerator;
import org.dcache.webadmin.model.dataaccess.communication.CellMessageGenerator.CellMessageRequest;
import org.dcache.webadmin.model.exceptions.DAOException;
import org.dcache.webadmin.model.dataaccess.communication.CommandSender;
import org.dcache.webadmin.model.dataaccess.communication.CommandSenderFactory;
import org.dcache.webadmin.model.dataaccess.communication.impl.PoolModifyModeMessageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an DataAccessObject for the pools offered by the model. It provides
 * the access to the needed data concerning pools.
 * It also sends commands to Pools via a commandSender.
 * @author jan schaefer 29-10-2009
 */
public class StandardPoolsDAO implements PoolsDAO {

    public static final List<String> POOLS_PATH = Arrays.asList("pools");
    public static final String RESPONSE_FAILED = "failed";
    private static final Logger _log = LoggerFactory.getLogger(StandardPoolsDAO.class);
    private PageInfoCache _pageCache;
    private CommandSenderFactory _commandSenderFactory;

    public StandardPoolsDAO(PageInfoCache pageCache,
            CommandSenderFactory commandSenderFactory) {
        _pageCache = pageCache;
        _commandSenderFactory = commandSenderFactory;
    }

    @Override
    public Set<Pool> getPoolsOfPoolGroup(String poolGroup) throws DAOException {
        _log.debug("get pools for Poolgroup {} called", poolGroup);
        try {
            Collection<SelectionPool> poolGroupPools = getPoolMonitor().
                    getPoolSelectionUnit().getPoolsByPoolGroup(poolGroup);
            _log.debug("selectionPools returned: {}", poolGroupPools);
            return createReturnPoolsFromSelectionPools(poolGroupPools);
        } catch (NoSuchContextException ex) {
            throw new DAOException("Data not available yet - PoolManger up already?", ex);
        }
    }

    @Override
    public Set<Pool> getPools() throws DAOException {
        _log.debug("getPools called");
        try {
            Collection<SelectionPool> pools = getPoolMonitor().
                    getPoolSelectionUnit().getAllDefinedPools(false);
            _log.debug("selectionPools returned: {}", pools);
            return createReturnPoolsFromSelectionPools(pools);
        } catch (NoSuchContextException ex) {
            throw new DAOException("Data not available yet - PoolManger up already?", ex);
        }
    }

    private Set<Pool> createReturnPoolsFromSelectionPools(Collection<SelectionPool> pools)
            throws NoSuchContextException {
        Set<Pool> returnPools = new HashSet<Pool>();
        for (SelectionPool selectionPool : pools) {
            PoolCostInfo info = getCostModule().getPoolCostInfo(selectionPool.getName());
            if (info == null) {
                info = new PoolCostInfo(selectionPool.getName());
                info.setSpaceUsage(0, 0, 0, 0, 0);
            }
            Pool pool = new Pool(info, selectionPool);
            returnPools.add(pool);
        }
        return returnPools;
    }

    @Override
    public Set<String> getPoolGroupNames() throws DAOException {
        _log.debug("getPoolGroupNames called");
        try {
            Set<String> poolGroupNames = new HashSet();
            for (SelectionPoolGroup group : getPoolSelectionUnit().getPoolGroups()) {
                poolGroupNames.add(group.getName());
            }
            return poolGroupNames;
        } catch (NoSuchContextException ex) {
            throw new DAOException("Data not available yet - PoolManger up already?", ex);
        }
    }

    private PoolSelectionUnit getPoolSelectionUnit() throws NoSuchContextException {
        return getPoolMonitor().getPoolSelectionUnit();
    }

    private CostModule getCostModule() throws NoSuchContextException {
        return getPoolMonitor().getCostModule();
    }

    private PoolMonitorV5 getPoolMonitor() throws NoSuchContextException {
        PoolMonitorV5 poolMonitor = (PoolMonitorV5) _pageCache.getCacheContent(
                ContextPaths.POOLMONITOR);
        return poolMonitor;
    }

    @Override
    public void changePoolMode(Set<String> poolIds, PoolV2Mode poolMode, String userName)
            throws DAOException {
        try {
            _log.debug("PoolModifyModeMsg-Command: {}", poolMode);
            if (!poolIds.isEmpty()) {
                PoolModifyModeMessageGenerator messageGenerator =
                        new PoolModifyModeMessageGenerator(
                        poolIds, poolMode, userName);
                CommandSender commandSender =
                        _commandSenderFactory.createCommandSender(messageGenerator);
                commandSender.sendAndWait();
                if (!commandSender.allSuccessful()) {
                    Set<String> failedIds = extractFailedIds(messageGenerator);
                    throw new DAOException(failedIds.toString());
                }
            }
            _log.debug("PoolDAO: Modechange-Commands send successfully");
        } catch (InterruptedException e) {
            _log.error("blocking was interrupted, change of PoolModes not yet done completly");
        }
    }

    private Set<String> extractFailedIds(CellMessageGenerator<?> messageGenerator) {
        Set<String> failedIds = new HashSet<String>();
        for (CellMessageRequest request : messageGenerator) {
            if (!request.isSuccessful()) {
                String destination = request.getDestination().toString();
                failedIds.add(destination);
            }
        }
        return failedIds;
    }
}