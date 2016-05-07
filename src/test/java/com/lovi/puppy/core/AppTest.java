package com.lovi.puppy.core;

import org.apache.log4j.Logger;
import com.lovi.puppy.core.async.AsyncExecutor;
import com.lovi.puppy.core.web.HttpResponse;
import com.lovi.puppy.core.web.HttpServer;
import com.lovi.puppy.core.web.RequestMapper;
import com.lovi.puppy.core.web.enums.HttpMethod;
import junit.framework.TestCase;

/**
 * Test case for puppy-io core web server
 * 
 * @author Tharanga Thennakoon
 *
 */
public class AppTest extends TestCase {

	final static Logger logger = Logger.getLogger(AppTest.class);

	public void testWebApp() {
		
		RequestMapper requestMapper = RequestMapper.create();

		requestMapper.map("/users", HttpMethod.GET).setHandler(ctx -> {

			logger.info("/users");

			AsyncExecutor<String> executor = AsyncExecutor.create();
			
			executor.run(() -> {
				
				String q = ctx.getHttpRequst().getParameter("q");
				if (q.equals("a")) {
					
					logger.info("I am going to wait....");
					try {
						Thread.sleep(5000);
					} catch (Exception e) {
						logger.error(e.getMessage());
					}
				}

				return "Hello users , q parameter - " + q;

			} , result -> {

				HttpResponse response = ctx.getHttpResponse();
				response.write(result);

			} , fail -> {

				HttpResponse response = ctx.getHttpResponse();
				response.setResponseCode(500);
				response.write(fail.getMessage());

			});
		});

		requestMapper.map("/users/{id}", HttpMethod.GET).setHandler(ctx -> {
			
			logger.info("/users/{id}");

			String id = ctx.getHttpRequst().getParameter("id");

			HttpResponse response = ctx.getHttpResponse();
			response.write("Hello users , id path parameter : " + id);

		});

		HttpServer httpServer = HttpServer.create();
		httpServer.run(9000, requestMapper, sussess -> {
			logger.info(sussess);
		} , fail -> {
			logger.error(fail.getMessage());
		});
	}
}
