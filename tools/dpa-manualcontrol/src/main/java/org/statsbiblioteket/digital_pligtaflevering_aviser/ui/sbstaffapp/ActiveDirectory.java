package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.sbstaffapp;


import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;


/**
 * Access the Windows Active Directory at Statsbiblioteket.
 * <p/>
 * <p/>This class provides the interface to retrieve information about
 * a user (typically an authenticated web user) from the Active Directory
 * at Statsbiblioteket.
 * <p/>
 * <p/>This class is primarily intended to be a helper class for {@link GroupProvider}.
 */

public class ActiveDirectory {
    private final Logger log = Logger.getLogger(ActiveDirectory.class);
    private String adDomain;
    private String adHost;
    private String adUser;
    private String adPassword;
    private String adFilterAttribute;
    private Properties env;

    public ActiveDirectory(Properties initProperties) {
        loadConfiguration(initProperties);
    }

    private String getRequiredProperty(Properties props, String propertyName) {
        String p = (String) props.get(propertyName);
        if (p == null) {
            throw new RuntimeException("Missing ad property: " + propertyName);
        }
        return p;
    }

    private void loadConfiguration(Properties props) {
        adUser = getRequiredProperty(props, "ad_user");
        adPassword = getRequiredProperty(props, "ad_password");
        adDomain = getRequiredProperty(props, "ad_domain");
        adHost = getRequiredProperty(props, "ad_domain_dc");
        adFilterAttribute = getRequiredProperty(props, "ad_filterAttribute");
    }

    private void createEnvironment() {
        env = new Properties();

        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        env.put(Context.SECURITY_PRINCIPAL, adUser + "@" + adDomain);
        env.put(Context.SECURITY_CREDENTIALS, adPassword);

        env.put(Context.PROVIDER_URL, "ldaps://" + adHost + "/");
    }

    protected Properties getEnv() {
        if (env == null) {
            createEnvironment();
        }
        return env;
    }

    protected String getDomainDc() {
        return "dc=" + adDomain.replace(".", ",dc=");
    }

    /**
     * Get a fresh copy of all AD attributes for a user.
     *
     * @param userName user to authorize
     * @return LDAP attributes for this user
     * @throws NamingException if any called method throws it
     */

    protected Attributes getAttributes(String userName) throws NamingException {
        log.debug("Get list of user attributes for: " + userName);
        LdapContext ctx = new InitialLdapContext(getEnv(), null);
        SearchControls sc = new SearchControls(SearchControls.SUBTREE_SCOPE,
                0,
                0,
                null,
                true,
                true);
        NamingEnumeration searchresults = ctx.search(getDomainDc(),
                adFilterAttribute + "=" + userName,
                sc);
        if (!searchresults.hasMore()) {
            throw new IllegalArgumentException("User not found in Active Directory " + adDomain + ": " + userName);
        }
        SearchResult result = (SearchResult) searchresults.next();
        return result.getAttributes();
    }

    /**
     * Get the AD groups for a user.
     * <p/>
     * <p/>Get the list of group names from the <code>memberOf</code> attribute
     * as retrieved by {@link #getAttributes}.
     *
     * @param userName user for which to retrieve the group list
     * @return list of AD groups for this user
     * @throws NamingException if any called method throws it
     */

    public List<String> getGroupNames(String userName) throws NamingException {
        List<String> theresult = new LinkedList<String>();
        Attributes attr = getAttributes(userName);
        Attribute memberOfAttribute = attr.get("memberOf");
        if (memberOfAttribute != null) {
            @SuppressWarnings("unchecked")
            NamingEnumeration<String> memberOfValues = (NamingEnumeration<String>) memberOfAttribute.getAll();
            while (memberOfValues.hasMore()) {
                String memberOfValue = memberOfValues.next();
                String[] memberEntry = memberOfValue.split(",");
                if (memberEntry.length == 0) {
                    throw new RuntimeException("AD member of bad element count: " + memberEntry.length);
                }
                String[] groupName = memberEntry[0].split("=");
                if (groupName.length != 2 || !(groupName[0].equalsIgnoreCase("CN"))) {
                    throw new RuntimeException("Unexpected structure of member entry: " + memberEntry[0]);
                }
                theresult.add(groupName[1].toLowerCase());
            }
        }
        log.debug("Groups for " + userName + ": " + theresult);
        return theresult;
    }

    /**
     * Get the AD display name for a user.
     * <p/>
     * <p/>Get the contents of the <code>displayName</code> attribute
     * as retrieved by {@link #getAttributes}.
     *
     * @param userName user whose real name is requested
     * @return the user's name from AD
     * @throws NamingException if any called method throws it
     */

    public String getDisplayName(String userName) throws NamingException {
        Attributes attr = getAttributes(userName);
        Attribute a = attr.get("displayName");
        if (a == null) {
            return "Ups, hvem er jeg";
        }
        Object dn = a.get();
        return (String) dn;
    }

}