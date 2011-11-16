/*
 * Automatically generated by jrpcgen 1.0.7 on 2/21/09 1:22 AM
 * jrpcgen is part of the "Remote Tea" ONC/RPC package for Java
 * See http://remotetea.sourceforge.net for details
 */
package org.dcache.chimera.nfs.v4.xdr;
/**
 * Enumeration (collection of constants).
 */
public interface nfsstat4 {

    public static final int NFS4_OK = 0;
    public static final int NFS4ERR_PERM = 1;
    public static final int NFS4ERR_NOENT = 2;
    public static final int NFS4ERR_IO = 5;
    public static final int NFS4ERR_NXIO = 6;
    public static final int NFS4ERR_ACCESS = 13;
    public static final int NFS4ERR_EXIST = 17;
    public static final int NFS4ERR_XDEV = 18;
    public static final int NFS4ERR_NOTDIR = 20;
    public static final int NFS4ERR_ISDIR = 21;
    public static final int NFS4ERR_INVAL = 22;
    public static final int NFS4ERR_FBIG = 27;
    public static final int NFS4ERR_NOSPC = 28;
    public static final int NFS4ERR_ROFS = 30;
    public static final int NFS4ERR_MLINK = 31;
    public static final int NFS4ERR_NAMETOOLONG = 63;
    public static final int NFS4ERR_NOTEMPTY = 66;
    public static final int NFS4ERR_DQUOT = 69;
    public static final int NFS4ERR_STALE = 70;
    public static final int NFS4ERR_BADHANDLE = 10001;
    public static final int NFS4ERR_BAD_COOKIE = 10003;
    public static final int NFS4ERR_NOTSUPP = 10004;
    public static final int NFS4ERR_TOOSMALL = 10005;
    public static final int NFS4ERR_SERVERFAULT = 10006;
    public static final int NFS4ERR_BADTYPE = 10007;
    public static final int NFS4ERR_DELAY = 10008;
    public static final int NFS4ERR_SAME = 10009;
    public static final int NFS4ERR_DENIED = 10010;
    public static final int NFS4ERR_EXPIRED = 10011;
    public static final int NFS4ERR_LOCKED = 10012;
    public static final int NFS4ERR_GRACE = 10013;
    public static final int NFS4ERR_FHEXPIRED = 10014;
    public static final int NFS4ERR_SHARE_DENIED = 10015;
    public static final int NFS4ERR_WRONGSEC = 10016;
    public static final int NFS4ERR_CLID_INUSE = 10017;
    public static final int NFS4ERR_RESOURCE = 10018;
    public static final int NFS4ERR_MOVED = 10019;
    public static final int NFS4ERR_NOFILEHANDLE = 10020;
    public static final int NFS4ERR_MINOR_VERS_MISMATCH = 10021;
    public static final int NFS4ERR_STALE_CLIENTID = 10022;
    public static final int NFS4ERR_STALE_STATEID = 10023;
    public static final int NFS4ERR_OLD_STATEID = 10024;
    public static final int NFS4ERR_BAD_STATEID = 10025;
    public static final int NFS4ERR_BAD_SEQID = 10026;
    public static final int NFS4ERR_NOT_SAME = 10027;
    public static final int NFS4ERR_LOCK_RANGE = 10028;
    public static final int NFS4ERR_SYMLINK = 10029;
    public static final int NFS4ERR_RESTOREFH = 10030;
    public static final int NFS4ERR_LEASE_MOVED = 10031;
    public static final int NFS4ERR_ATTRNOTSUPP = 10032;
    public static final int NFS4ERR_NO_GRACE = 10033;
    public static final int NFS4ERR_RECLAIM_BAD = 10034;
    public static final int NFS4ERR_RECLAIM_CONFLICT = 10035;
    public static final int NFS4ERR_BADXDR = 10036;
    public static final int NFS4ERR_LOCKS_HELD = 10037;
    public static final int NFS4ERR_OPENMODE = 10038;
    public static final int NFS4ERR_BADOWNER = 10039;
    public static final int NFS4ERR_BADCHAR = 10040;
    public static final int NFS4ERR_BADNAME = 10041;
    public static final int NFS4ERR_BAD_RANGE = 10042;
    public static final int NFS4ERR_LOCK_NOTSUPP = 10043;
    public static final int NFS4ERR_OP_ILLEGAL = 10044;
    public static final int NFS4ERR_DEADLOCK = 10045;
    public static final int NFS4ERR_FILE_OPEN = 10046;
    public static final int NFS4ERR_ADMIN_REVOKED = 10047;
    public static final int NFS4ERR_CB_PATH_DOWN = 10048;
    public static final int NFS4ERR_BADIOMODE = 10049;
    public static final int NFS4ERR_BADLAYOUT = 10050;
    public static final int NFS4ERR_BAD_SESSION_DIGEST = 10051;
    public static final int NFS4ERR_BADSESSION = 10052;
    public static final int NFS4ERR_BADSLOT = 10053;
    public static final int NFS4ERR_COMPLETE_ALREADY = 10054;
    public static final int NFS4ERR_CONN_NOT_BOUND_TO_SESSION = 10055;
    public static final int NFS4ERR_DELEG_ALREADY_WANTED = 10056;
    public static final int NFS4ERR_BACK_CHAN_BUSY = 10057;
    public static final int NFS4ERR_LAYOUTTRYLATER = 10058;
    public static final int NFS4ERR_LAYOUTUNAVAILABLE = 10059;
    public static final int NFS4ERR_NOMATCHING_LAYOUT = 10060;
    public static final int NFS4ERR_RECALLCONFLICT = 10061;
    public static final int NFS4ERR_UNKNOWN_LAYOUTTYPE = 10062;
    public static final int NFS4ERR_SEQ_MISORDERED = 10063;
    public static final int NFS4ERR_SEQUENCE_POS = 10064;
    public static final int NFS4ERR_REQ_TOO_BIG = 10065;
    public static final int NFS4ERR_REP_TOO_BIG = 10066;
    public static final int NFS4ERR_REP_TOO_BIG_TO_CACHE = 10067;
    public static final int NFS4ERR_RETRY_UNCACHED_REP = 10068;
    public static final int NFS4ERR_UNSAFE_COMPOUND = 10069;
    public static final int NFS4ERR_TOO_MANY_OPS = 10070;
    public static final int NFS4ERR_OP_NOT_IN_SESSION = 10071;
    public static final int NFS4ERR_HASH_ALG_UNSUPP = 10072;
    public static final int NFS4ERR_CONN_BINDING_NOT_ENFORCED = 10073;
    public static final int NFS4ERR_CLIENTID_BUSY = 10074;
    public static final int NFS4ERR_PNFS_IO_HOLE = 10075;
    public static final int NFS4ERR_SEQ_FALSE_RETRY = 10076;
    public static final int NFS4ERR_BAD_HIGH_SLOT = 10077;
    public static final int NFS4ERR_DEADSESSION = 10078;
    public static final int NFS4ERR_ENCR_ALG_UNSUPP = 10079;
    public static final int NFS4ERR_PNFS_NO_LAYOUT = 10080;
    public static final int NFS4ERR_NOT_ONLY_OP = 10081;
    public static final int NFS4ERR_WRONG_CRED = 10082;
    public static final int NFS4ERR_WRONG_TYPE = 10083;
    public static final int NFS4ERR_DIRDELEG_UNAVAIL = 10084;
    public static final int NFS4ERR_REJECT_DELEG = 10085;
    public static final int NFS4ERR_RETURNCONFLICT = 10086;

}
// End of nfsstat4.java