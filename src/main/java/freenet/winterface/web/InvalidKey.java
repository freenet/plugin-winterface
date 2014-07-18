package freenet.winterface.web;

import java.net.MalformedURLException;

import freenet.client.FetchException;
import freenet.client.FetchResult;
import freenet.keys.FreenetURI;
import freenet.winterface.core.HighLevelSimpleClientInterface;
import freenet.winterface.core.VelocityBase;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Invalid key page.
 */
public class InvalidKey extends VelocityBase {

	@Override
	protected void subFillContext(Context context, HttpServletRequest request) {
	}
		
}