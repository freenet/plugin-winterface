package freenet.winterface.core;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import freenet.io.AddressMatcher;

/**
 * {@link Filter} for IP filtering.
 * <p>
 * {@link WinterfaceApplication} contains a comma separated list of allowed
 * hosts in its {@link Configuration}. Hosts can be also in CIDR format.
 * Filtering is done in following steps:
 * <ul>
 * <li>Match remote address against allowed hosts. This is done using
 * {@link AddressMatcher} which is also capable of subnet matching.</li>
 * <li>If remote host is not in the list of allowed hosts it is blocked
 * <b>only</b> if required page is not contained in list of white listed paths
 * (see bellow)</li>
 * </ul>
 * </p>
 * <p>
 * <b>White listed paths</b> are paths, which are not filtered under any
 * condition. This paths contain for example the error pages. If remote host is
 * blocked, its request is forwarded to an error page which <i>should</i> be
 * accessible and not filtered.
 * </p>
 * 
 * @author pausb
 * @see Configuration
 */
public class IPFilter implements Filter {

	/** List of allowedHosts as read from filter config */
	private String[] allowedHosts;

	/** Filter parameter name containing allowed hosts */
	public final static String ALLOWED_HOSTS_PARAM = "allowedHosts";

	/** List of urls not to block **/
	private final static List<String> whiteUrls = Arrays.asList("/error", "/static");

	/** Log4j Logger */
	private final static Logger logger = Logger.getLogger(IPFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		String configAllowed = filterConfig.getInitParameter(ALLOWED_HOSTS_PARAM);
		logger.info("Filter initiated with following hosts: " + configAllowed);
		allowedHosts = configAllowed.split(",");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String path = ((HttpServletRequest) request).getServletPath();
		String remoteAddr = request.getRemoteAddr();
		boolean unblock = false;
		// First check if remote address is included in allowed hosts
		for (String allowed : allowedHosts) {
			try {
				unblock |= IPUtils.quietMatches(allowed, remoteAddr);
			} catch (UnknownHostException e) {
				logger.error("Error while matching allowed hosts and remoter address.", e);
				unblock = false;
			}
		}
		// We don't block access to specific URLs such as error pages and static
		// data.
		// This is necessary because a blocking request forwards to an error
		// page with static resources
		if (unblock || whiteUrls.contains(path)) {
			chain.doFilter(request, response);
			return;
		}
		logger.debug("Blocked request from " + remoteAddr + " on path " + path);
		((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN);
	}

	@Override
	public void destroy() {
	}

}
