package org.statsbiblioteket.digital_pligtaflevering_aviser.ui.sbstaffapp;

import org.apache.log4j.Logger;
import org.jasig.cas.client.util.AbstractCasFilter;
import org.jasig.cas.client.validation.Assertion;

import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * Provide list of AD groups assigned to the session user.
 * <p/>
 * <p/>This class is intended for use in applications where the users are
 * authenticated by the CasClient filter.
 * GroupProvider is implemeted as a Filter. Implementation as a HttpSessionListener
 * was considered, but the session is created before the CAS filter has authenticated
 * the user.
 */

public class GroupProvider implements Filter {
    private final Logger log = Logger.getLogger(GroupProvider.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        //TODO make this a separate filter

        final HttpServletRequest httprequest = (HttpServletRequest) request;
        final HttpSession session = httprequest.getSession(false);
        final Assertion assertion = (Assertion) (session == null ? request.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION) : session.getAttribute(AbstractCasFilter.CONST_CAS_ASSERTION));
        assertion.getPrincipal().getAttributes().put("sbAdGroups",getGroups((HttpServletRequest) request));
        assertion.getAttributes().put("sbAppName", ((HttpServletRequest) request).getContextPath().replace("/", ""));
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.debug("init");
    }

    @Override
    public void destroy() {
        log.debug("destroy");
    }

    protected List<String> getGroups(HttpServletRequest request) throws ServletException {
        HttpSession s = request.getSession();
        if (s.getAttribute("sbAdGroups") == null) {
            setAuthorizationData(request);
        }
        @SuppressWarnings("unchecked")
        List<String> groups = (List<String>) s.getAttribute("sbAdGroups");
        log.debug("Groups: "+ groups);
        return groups;
    }

    protected void setAuthorizationData(HttpServletRequest request) throws ServletException {
        try {
            String user = getLoggedInUser(request);
            ActiveDirectory ad = getActiveDirectory(request);
            log.debug("building group list for "+ user);
            List<String> myGroups = ad.getGroupNames(user);
            HttpSession s = request.getSession();
            s.setAttribute("sbAdGroups", myGroups);
            s.setAttribute("sbAdDisplayName", ad.getDisplayName(user));
            s.setAttribute("sbUserId", user);
            ServletContext application = s.getServletContext();
            String groupFileName = application.getInitParameter("ad-group-permissions");
            if (groupFileName == null) {
                log.error("Context-param ad-group-permissions not set in web.xml");
                return;
            }
            File groupFile = new File(application.getRealPath(groupFileName));
            if (!groupFile.exists()) {
                log.error("Permission file does not exist: "+ groupFile);
                return;
            }
            log.debug("get authorization data from "+ groupFile);

            Properties p = new Properties();
            p.load(new FileInputStream(groupFile));
            Map<String, String> authorizedMap = new HashMap<String, String>();
            for (Object op : p.keySet()) {
                String operation = (String) op;
                boolean authorized = false;
                for (String name : p.getProperty(operation).split(", *")) {
                    if (user.toLowerCase().equals(name.toLowerCase())) {
                        authorized = true;
                        break;
                    }
                    if (myGroups.contains(name.toLowerCase())) {
                        authorized = true;
                        break;
                    }
                }
                authorizedMap.put(operation, authorized ? "allow" : "deny");
            }
            s.setAttribute("authorized", authorizedMap);
        } catch (IOException e) {
            throw new ServletException(e);
        } catch (NamingException e) {
            throw new ServletException(e);
        }
    }

    protected String getLoggedInUser(HttpServletRequest request) throws ServletException {
        String user = request.getRemoteUser();
        if (user == null) {
            user = (String) request.getSession().getAttribute("edu.yale.its.tp.cas.client.filter.user");
        }
        if (user == null) {
            throw new ServletException("No authenticated user found in getRemoteUser or in session");
        }
        return user;
    }

    protected ActiveDirectory getActiveDirectory(HttpServletRequest request) throws ServletException {
        HttpSession session = request.getSession();
        
        String casIdentifier = session.getServletContext().getInitParameter("casServerUrlPrefix");
        if (casIdentifier == null) {
            casIdentifier = session.getAttribute("edu.yale.its.tp.cas.client.filter.receipt").toString();
        }
        if (casIdentifier == null) {
            throw new ServletException("CAS server URL not found in either session or as context param 'casServerUrlPrefix'");
        }

        Properties adProperties;
        if (casIdentifier.contains("casinternal")) {
            adProperties = readProperties("ad.sb.properties");
        } else if (casIdentifier.contains("cassbext")) {
            adProperties = readProperties("ad.sbextern.properties");
        } else {
            log.error("Unrecognized cas: "+ casIdentifier);
            throw new ServletException("GroupProvider: No authorization configuration for cas "+ casIdentifier);
        }
        return new ActiveDirectory(adProperties); 
    }

    private Properties readProperties(String propertiesResource) throws ServletException {
        try {
            InputStream is = getClass().getResourceAsStream(propertiesResource);
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                return props;
            } else {
                throw new ServletException("Property file not found: " + propertiesResource);
            }
        } catch (IOException e) {
            log.error("Could not load "+ propertiesResource, e);
            throw new ServletException("Could not load " + propertiesResource, e);
        }
    }
}
