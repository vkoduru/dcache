package org.dcache.acl.parser;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.dcache.acl.ACE;
import org.dcache.acl.ACLException;
import org.dcache.acl.enums.AccessMask;
import org.dcache.acl.enums.AceFlags;
import org.dcache.acl.enums.AceType;
import org.dcache.acl.enums.Who;
import org.dcache.acl.handler.PrincipalHandler;

public class ACEParser {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private static final String SPACE_SEPARATOR = " ";

    private static final String SEPARATOR = ":";

    private static final int ACE_NFS4_TAGNUM = 4;

    private static final int ACES_MIN = 1;

    private static final int ACE_MIN = 4, ACE_MAX = 7;

    private static final int ACE_MIN_ADM = 2, ACE_MAX_ADM = 5;

    private static ACEParser _SINGLETON;
    static {
        _SINGLETON = new ACEParser();
    }

    private ACEParser() {
        super();
    }

    public static ACEParser instance() {
        return _SINGLETON;
    }

    private static PrincipalHandler _principalHandler;

    private static final PrincipalHandler getPrincipalHandler() throws ACLException {
        if ( _principalHandler == null )
            _principalHandler = new PrincipalHandler();
        return _principalHandler;
    }

    /**
     * ace_spec format:
     * 	type:[flags]:who_id:access_msk
     *
     * ace_spec examples:
     * 	A:fd:mdavid:rx
     * 	D::mdavid:w
     *
     * @param order
     *            ACE's order in list
     * @param ace_spec
     *            String representation of ACE
     * @return Access Control Entry object
     */
    public static ACE parseNFSv4(int order, String ace_spec) throws IllegalArgumentException, ACLException {
        if ( order < 0 )
            throw new IllegalArgumentException("Invalid order: " + order);

        if ( ace_spec == null || ace_spec.length() == 0 )
            throw new IllegalArgumentException("ace_spec is " + (ace_spec == null ? "NULL" : "Empty"));

        if ( ace_spec.endsWith(SEPARATOR) )
            throw new IllegalArgumentException("ace_spec ends with \"" + SEPARATOR + "\"");

        String[] split = ace_spec.split(SEPARATOR);
        if ( split == null )
            throw new IllegalArgumentException("ace_spec can't be splitted.");

        int len = split.length;
        if ( len != ACE_NFS4_TAGNUM )
            throw new IllegalArgumentException("Count tags invalid.");

        int index = 0;
        AceType type = AceType.fromAbbreviation(split[index++]);

        int flags = 0;
        try {
            flags = AceFlags.parseInt(split[index++]);
        } catch (IllegalArgumentException Ignore) {
        }

        String sWho = split[index++];
        int whoID = -1;
        Who who = Who.fromAbbreviation(sWho);
        if ( who == null ) {
            if ( AceFlags.IDENTIFIER_GROUP.matches(flags) ) {
                who = Who.GROUP;
                whoID = getPrincipalHandler().getGroupID(sWho);
            } else {
                who = Who.USER;
                whoID = getPrincipalHandler().getUserID(sWho);
            }
            if ( whoID == -1 )
                throw new IllegalArgumentException("whoID is -1, sWho: " + sWho);
        }

        String sAccessMsk = split[index++];
        int accessMsk = AccessMask.parseInt(sAccessMsk);
        if ( accessMsk == 0 )
            throw new IllegalArgumentException("Invalid accessMask: " + sAccessMsk);

        return new ACE(type, flags, accessMsk, who, whoID, ACE.DEFAULT_ADDRESS_MSK, order);
    }

    /**
     * aces_spec format:
     * 	type:[flags]:who_id:access_msk
     * 	type:[flags]:who_id:access_msk ...
     *
     * aces_spec example:
     * 	A:fd:mdavid:rx
     * 	D::mdavid:w
     *
     * @param aces_spec
     *            String representation of ACEs NFSv4
     * @return List of ACEs
     */
    public static List<ACE> parseNFSv4(String aces_spec) throws IllegalArgumentException, ACLException {
        if ( aces_spec == null || aces_spec.length() == 0 )
            throw new IllegalArgumentException("aces_spec is " + (aces_spec == null ? "NULL" : "Empty"));

        String[] split = aces_spec.split(LINE_SEPARATOR);
        if ( split == null )
            throw new IllegalArgumentException("aces_spec can't be splitted.");

        int len = split.length;
        if ( len < ACES_MIN )
            throw new IllegalArgumentException("Count ACEs invalid.");

        List<ACE> aces = new ArrayList<ACE>(len);
        for (int order = 0; order < len; order++)
            aces.add(ACEParser.parseNFSv4(order, split[order]));

        return aces;
    }

    /**
     * ace_spec format:
     * 	ace_order:who[:who_id]:access_msk[:flags]:type[:address_msk]
     *
     * ace_spec examples:
     * 	0:USER:7:rlwfx:o:ALLOW:FFFF
     * 	1:EVERYONE@:w:DENY
     *
     * @param ace_spec
     *            String representation of ACE
     */
    public static ACE parse(String ace_spec) throws IllegalArgumentException {
        if ( ace_spec == null || ace_spec.length() == 0 )
            throw new IllegalArgumentException("ace_spec is " + (ace_spec == null ? "NULL" : "Empty"));

        if ( ace_spec.endsWith(SEPARATOR) )
            throw new IllegalArgumentException("ace_spec ends with \"" + SEPARATOR + "\"");

        String[] split = ace_spec.split(SEPARATOR);
        if ( split == null )
            throw new IllegalArgumentException("ace_spec can't be splitted.");

        int len = split.length;
        if ( len < ACE_MIN || len > ACE_MAX )
            throw new IllegalArgumentException("Count tags invalid.");

        int index = 0, order;
        String sOrder = split[index++];
        try {
            order = Integer.parseInt(sOrder);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid ACE order. NumberFormatException: " + e.getMessage());
        }

        String sWho = split[index++];
        Who who = Who.fromAbbreviation(sWho);
        if ( who == null )
            throw new IllegalArgumentException("Invalid who abbreviation: " + sWho);

        int whoID = -1;
        if ( who == Who.USER || who == Who.GROUP ) {
            String sWhoID = split[index++];
            try {
                whoID = Integer.parseInt(sWhoID);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid whoID. NumberFormatException: " + e.getMessage());
            }
        }

        String sAccessMsk = split[index++];
        int accessMsk = AccessMask.parseInt(sAccessMsk);
        if ( accessMsk == 0 )
            throw new IllegalArgumentException("Invalid accessMask: " + sAccessMsk);

        if ( index >= len )
            throw new IllegalArgumentException("Unspecified ACE type.");

        String s = split[index++];
        int flags = 0;
        try {
            flags = AceFlags.parseInt(s);
            s = null;
        } catch (IllegalArgumentException Ignore) {
        }

        if ( s == null ) {
            if ( index >= len )
                throw new IllegalArgumentException("Unspecified ACE type.");
            s = split[index++];
        }

        AceType type = AceType.fromAbbreviation(s);

        String addressMsk = ACE.DEFAULT_ADDRESS_MSK;
        if ( index < len ) {
            addressMsk = split[index++];

            try {
                new BigInteger(addressMsk, 16);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid addressMask. NumberFormatException: " + e.getMessage());
            }
        }
        if ( index != len )
            throw new IllegalArgumentException("Check index failure. Invalid ace_spec: " + ace_spec);

        return new ACE(type, flags, accessMsk, who, whoID, addressMsk, order);
    }

    /**
     * ace_spec format:
     * 	who[:who_id]:+/-access_msk[:flags][:address_msk]
     *
     * ace_spec examples:
     * 	USER:3750:+rlx:o:FFFF
     * 	EVERYONE@:-w
     *
     * @param order
     *            ACE's order in list
     * @param ace_spec
     *            String representation of ACE (without 'order')
     */

    public static ACE parseAdm(int order, String ace_spec) throws IllegalArgumentException {
        if ( order < 0 )
            throw new IllegalArgumentException("Invalid order: " + order);

        if ( ace_spec == null || ace_spec.length() == 0 )
            throw new IllegalArgumentException("ace_spec is " + (ace_spec == null ? "NULL" : "Empty"));

        if ( ace_spec.endsWith(SEPARATOR) )
            throw new IllegalArgumentException("ace_spec ends with \"" + SEPARATOR + "\"");

        String[] split = ace_spec.split(SEPARATOR);
        if ( split == null )
            throw new IllegalArgumentException("ace_spec can't be splitted.");

        int len = split.length;
        if ( len < ACE_MIN_ADM || len > ACE_MAX_ADM )
            throw new IllegalArgumentException("Count tags invalid.");

        int index = 0;
        String sWho = split[index++];
        Who who = Who.fromAbbreviation(sWho);
        if ( who == null )
            throw new IllegalArgumentException("Invalid who abbreviation: " + sWho);

        int whoID = -1;
        if ( Who.USER.equals(who) || Who.GROUP.equals(who) ) {
            String sWhoID = split[index++];
            if ( index >= len )
                throw new IllegalArgumentException("Unspecified accessMask.");

            try {
                whoID = Integer.parseInt(sWhoID);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid whoID. NumberFormatException: " + e.getMessage());
            }
        }

        String sAccessMsk = split[index++];
        AceType type;
        char operator = sAccessMsk.charAt(0);
        if ( operator == '+' )
            type = AceType.ACCESS_ALLOWED_ACE_TYPE;
        else if ( operator == '-' )
            type = AceType.ACCESS_DENIED_ACE_TYPE;
        else
            throw new IllegalArgumentException("Invalid operator: \"" + operator + "\"");

        int accessMsk = AccessMask.parseInt(sAccessMsk.substring(1));
        if ( accessMsk == 0 )
            throw new IllegalArgumentException("Invalid accessMask: " + sAccessMsk);

        String addressMsk = ACE.DEFAULT_ADDRESS_MSK;
        int flags = 0;
        if ( index < len ) {
            String s = split[index++];
            if ( s.trim().length() == 0 )
                throw new IllegalArgumentException("ACE flags is Empty.");

            try {
                flags = AceFlags.parseInt(s);
                s = null;
            } catch (IllegalArgumentException Ignore) {
            }

            if ( s == null && index < len )
                s = split[index++];

            if ( s != null ) {
                try {
                    new BigInteger(s, 16);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid addressMask. NumberFormatException: " + e.getMessage());
                }
                addressMsk = s;
            }
        }

        if ( index != len )
            throw new IllegalArgumentException("Check index failure. Invalid ace_spec: " + ace_spec);

        return new ACE(type, flags, accessMsk, who, whoID, addressMsk, order);
    }

    /**
     * aces_spec format:
     * 	who[:who_id]:+/-access_msk[:flags][:address_msk] who[:who_id]:+/-access_msk[:flags][:address_msk]
     *
     * aces_spec example:
     * 	USER:3750:+rlx:o:FFFF EVERYONE@:-w
     *
     * @param aces_spec
     *            String representation of ACEs
     */
    public static List<ACE> parseAdm(String aces_spec) throws IllegalArgumentException {
        if ( aces_spec == null || aces_spec.length() == 0 )
            throw new IllegalArgumentException("aces_spec is " + (aces_spec == null ? "NULL" : "Empty"));

        String[] split = aces_spec.split(SPACE_SEPARATOR);
        if ( split == null )
            throw new IllegalArgumentException("aces_spec can't be splitted.");

        int len = split.length;
        if ( len < ACES_MIN )
            throw new IllegalArgumentException("Count ACEs invalid.");

        List<ACE> aces = new ArrayList<ACE>(len);
        for (int order = 0; order < len; order++)
            aces.add(ACEParser.parseAdm(order, split[order]));

        return aces;
    }

}