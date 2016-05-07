package com.lovi.puppy.core.web.impl;

import com.lovi.puppy.core.async.Future;
import com.lovi.puppy.core.common.Message;
import com.lovi.puppy.core.exception.ErrorMessage;
import com.lovi.puppy.core.exception.RequestMapperException;
import com.lovi.puppy.core.exception.ResourceNotFoundException;
import com.lovi.puppy.core.handlers.FailureHandler;
import com.lovi.puppy.core.handlers.Handler;
import com.lovi.puppy.core.web.HttpRequst;
import com.lovi.puppy.core.web.HttpResponse;
import com.lovi.puppy.core.web.HttpServer;
import com.lovi.puppy.core.web.RequestMap;
import com.lovi.puppy.core.web.RequestMapper;
import com.lovi.puppy.core.web.ServerContext;
import com.lovi.puppy.core.web.Session;
import com.lovi.puppy.core.web.SessionStore;
import com.lovi.puppy.core.web.enums.HttpMethod;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * 
 * @author Tharanga Thennakoon
 *
 */
public class HttpServerImpl implements HttpServer {

	final static Logger logger = Logger.getLogger(HttpServer.class);
	private Selector selector;
	private RequestMapper requestMapper;
	private SessionStore sessionStore;

	@Override
	public void run(int port, RequestMapper requestMapper) {
		setRequestMapper(requestMapper);
		setUpServer(null, port, null);
	}

	@Override
	public void run(String hostname, int port, RequestMapper requestMapper) {
		setRequestMapper(requestMapper);
		setUpServer(hostname, port, null);
	}

	@Override
	public void run(int port, RequestMapper requestMapper, Handler<Object> successHandler,
			Handler<Throwable> failureHandler) {
		Future<Object> future = Future.create();
		future.setSussessHandler(successHandler);
		future.setFailureHandler(failureHandler);

		setRequestMapper(requestMapper);
		setUpServer(null, port, future);
	}

	@Override
	public void run(String hostname, int port, RequestMapper requestMapper, Handler<Object> successHandler,
			Handler<Throwable> failureHandler) {
		Future<Object> future = Future.create();
		future.setSussessHandler(successHandler);
		future.setFailureHandler(failureHandler);

		setRequestMapper(requestMapper);
		setUpServer(hostname, port, future);

	}

	private void setUpServer(String hostname, int port, Future<Object> future) {
		try {

			prepareRequestMap();

			selector = Selector.open();
			sessionStore = new SessionStoreImpl();

			ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

			InetSocketAddress serverAddress;
			if (hostname == null)
				serverAddress = new InetSocketAddress(port);
			else
				serverAddress = new InetSocketAddress(hostname, port);

			serverSocketChannel.bind(serverAddress);

			serverSocketChannel.configureBlocking(false);

			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

			if (future != null)
				future.setResult(Message.SERVER_START.getMessage() + port);

			while (true) {

				selector.select();

				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();

				while (iterator.hasNext()) {
					SelectionKey key = iterator.next();

					if (key.isAcceptable()) {
						SocketChannel clientSocketChannel = serverSocketChannel.accept();

						clientSocketChannel.configureBlocking(false);

						clientSocketChannel.register(selector, SelectionKey.OP_READ);
						
						System.out.println();
						logger.info("connection-accepted : " + clientSocketChannel.getLocalAddress());

					} else if (key.isReadable()) {
						SocketChannel clientSocketChannel = null;
						try{
							clientSocketChannel = (SocketChannel) key.channel();
							requestProcess(clientSocketChannel);
						}catch(Exception e){
							clientSocketChannel.close();
							logger.error("socket-channel-read error " + e.getMessage());
						}
						
					} else if (key.isWritable()) {
						SocketChannel clientSocketChannel = null;
						try{
							clientSocketChannel = (SocketChannel) key.channel();
							clientSocketChannel.close();
						}catch(Exception e){
							clientSocketChannel.close();
							logger.error("socket-channel-write error " + e.getMessage());
						}
					}
					iterator.remove();
				}
			}
		} catch (Exception e) {
			if (future != null)
				future.setFailure(e);
		}
	}

	private void prepareRequestMap() throws Exception {
		if (requestMapper != null) {
			for (RequestMap requestMap : requestMapper.getRequestMaps()) {

				StringBuilder regExpPath = new StringBuilder();

				String route = requestMap.getPath();

				if (route.charAt(0) != '/')
					throw new RequestMapperException(ErrorMessage.REQUEST_MAP_MUST_START_WITH_SLASH.getMessage());

				String[] splitRouteStr = route.split("/");

				// prepare reg ex
				for (int i = 0; i < splitRouteStr.length; i++) {

					String str = splitRouteStr[i];

					if (str.matches("\\{.*\\}"))
						regExpPath.append(".*");
					else
						regExpPath.append(str);

					if ((i + 1) != splitRouteStr.length)
						regExpPath.append("/");
				}

				requestMap.setRegExpPath(regExpPath.toString());
			}
		} else
			throw new RequestMapperException();
	}

	private void requestProcess(SocketChannel socketChannel) throws Exception {
		ByteBuffer clientBuffer = ByteBuffer.allocate(256);

		StringBuilder requestStringBuilder = new StringBuilder();

		int bytesRead = socketChannel.read(clientBuffer); // read into buffer.
		while (bytesRead > 0) {

			clientBuffer.flip();

			while (clientBuffer.hasRemaining()) {
				requestStringBuilder.append((char) clientBuffer.get());
			}

			clientBuffer.clear();

			bytesRead = socketChannel.read(clientBuffer);

		}
		
		HttpRequst httpRequst = new HttpRequstImpl(requestStringBuilder.toString());
		HttpResponse httpResponse = new HttpResponseImpl(selector, socketChannel);
		Session session = createSession(httpRequst, httpResponse);

		// -------------------------mapping request----------------
		ServerContext serverContext = new ServerContextImpl(this, httpRequst, httpResponse, session);

		if (requestMapper != null) {
			String userRequestPath = null;
			boolean found = false;
			for (RequestMap requestMap : requestMapper.getRequestMaps()) {

				String requestPath = requestMap.getPath();
				String regExpPath = requestMap.getRegExpPath();

				String incomePath = httpRequst.getLocation().split("\\?")[0];// remove
																				// query
																				// parameters;
				incomePath = (incomePath.charAt(incomePath.length() - 1) == '/')
						? incomePath.substring(0, incomePath.length() - 1) : incomePath;// remove last slash

						
				userRequestPath = incomePath;
						
				// add index point
				requestPath = "/_INDEX_" + requestPath;
				regExpPath = "/_INDEX_" + regExpPath;
				incomePath = "/_INDEX_" + incomePath;

				String[] splitRequestPath = requestPath.split("/");
				String[] splitImcomePathStr = incomePath.split("/");

				// http method are not equal
				if (!requestMap.getHttpMethod().toString().equals(httpRequst.getMethod()))
					continue;

				// check route and incoming request path separators not equal
				if ((splitRequestPath.length != splitImcomePathStr.length))
					continue;

				// match
				Matcher matcher = Pattern.compile(regExpPath).matcher(incomePath);
				if (matcher.matches()) {

					// extract path variables
					for (int i = 0; i < splitRequestPath.length; i++) {

						String str = splitRequestPath[i];

						Matcher matcherParm = Pattern.compile("\\{(.*)\\}").matcher(str);

						if (matcherParm.matches()) {

							try {
								serverContext.getHttpRequst().getParameters().put(matcherParm.group(1),
										splitImcomePathStr[i]);
							} catch (Exception e) {
								continue;
							}
						}

					}

					//handler process
					try {
						requestMap.getHandler().handle(serverContext);
					} catch (Exception e) {
						
						FailureHandler<ServerContext> failureHandlerRequestMap = requestMap.getFailureHandler();

						if (failureHandlerRequestMap != null)
							failureHandlerRequestMap.handle(serverContext, e, 500,
									ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
						else {
							FailureHandler<ServerContext> failureHandlerRequestMapper = requestMapper
									.getFailureHandler();
							if (failureHandlerRequestMapper != null)
								failureHandlerRequestMapper.handle(serverContext, e, 500,
										ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
							else
								writeServerError(serverContext, e, 500,
										ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
						}
					}
					found = true;
					break;
					
				}
			}

			if (!found) {
				FailureHandler<ServerContext> failureHandlerRequestMapper = requestMapper.getFailureHandler();
				if (failureHandlerRequestMapper != null)
					failureHandlerRequestMapper.handle(
							serverContext, 
							new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND.getMessage() + " " + userRequestPath), 
							404,
							ErrorMessage.RESOURCE_NOT_FOUND.getMessage());
				else
					writeServerError(
							serverContext, 
							new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND.getMessage() + " " + userRequestPath), 
							404,
							ErrorMessage.RESOURCE_NOT_FOUND.getMessage());
			}

		} else {
			throw new RequestMapperException();
		}

	}
	
	@Override
	public void requestProcess(ServerContext serverContext, String newLocation) throws Exception {
		requestProcess(serverContext, newLocation, HttpMethod.GET);
	}

	@Override
	public void requestProcess(ServerContext serverContext, String newLocation, HttpMethod httpMethod) throws Exception {
		
		if (requestMapper != null) {
			String userRequestPath = null;
			boolean found = false;
			for (RequestMap requestMap : requestMapper.getRequestMaps()) {

				String requestPath = requestMap.getPath();
				String regExpPath = requestMap.getRegExpPath();

				String incomePath = newLocation.split("\\?")[0];// remove
																				// query
																				// parameters;
				incomePath = (incomePath.charAt(incomePath.length() - 1) == '/')? incomePath.substring(0, incomePath.length() - 1) : incomePath;// remove
																						// last
																						// slash

				userRequestPath = incomePath;
				
				// add index point
				requestPath = "/_INDEX_" + requestPath;
				regExpPath = "/_INDEX_" + regExpPath;
				incomePath = "/_INDEX_" + incomePath;

				String[] splitRequestPath = requestPath.split("/");
				String[] splitImcomePathStr = incomePath.split("/");

				// http method are not equal
				if (!(requestMap.getHttpMethod() == httpMethod))
					continue;

				// check route and incoming request path separators not equal
				if ((splitRequestPath.length != splitImcomePathStr.length))
					continue;

				// match
				Matcher matcher = Pattern.compile(regExpPath).matcher(incomePath);
				if (matcher.matches()) {

					// extract path variables
					for (int i = 0; i < splitRequestPath.length; i++) {

						String str = splitRequestPath[i];

						Matcher matcherParm = Pattern.compile("\\{(.*)\\}").matcher(str);

						if (matcherParm.matches()) {

							try {
								serverContext.getHttpRequst().getParameters().put(matcherParm.group(1),
										splitImcomePathStr[i]);
							} catch (Exception e) {
								continue;
							}
						}

					}

					//handler process
					try {
						requestMap.getHandler().handle(serverContext);
					} catch (Exception e) {
						
						FailureHandler<ServerContext> failureHandlerRequestMap = requestMap.getFailureHandler();

						if (failureHandlerRequestMap != null)
							failureHandlerRequestMap.handle(serverContext, e, 500,
									ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
						else {
							FailureHandler<ServerContext> failureHandlerRequestMapper = requestMapper
									.getFailureHandler();
							if (failureHandlerRequestMapper != null)
								failureHandlerRequestMapper.handle(serverContext, e, 500,
										ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
							else
								writeServerError(serverContext, e, 500,
										ErrorMessage.INTERNAL_SERVER_ERROR.getMessage());
						}
					}
					found = true;
					break;
					
				}
			}

			if (!found) {
				FailureHandler<ServerContext> failureHandlerRequestMapper = requestMapper.getFailureHandler();
				if (failureHandlerRequestMapper != null)
					failureHandlerRequestMapper.handle(
							serverContext, 
							new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND.getMessage() + " " + userRequestPath), 
							404,
							ErrorMessage.RESOURCE_NOT_FOUND.getMessage());
				else
					writeServerError(
							serverContext, 
							new ResourceNotFoundException(ErrorMessage.RESOURCE_NOT_FOUND.getMessage() + " " + userRequestPath), 
							404,
							ErrorMessage.RESOURCE_NOT_FOUND.getMessage());
			}

		} else {
			throw new RequestMapperException();
		}

	}
	
	@Override
	public void setRequestMapper(RequestMapper requestMapper) {
		this.requestMapper = requestMapper;
	}

	/**
	 * failure -> RequestMap [if not handle] -> RequestMapper -> [if not handle]
	 * -> writeServerError()
	 * 
	 * @param serverContext
	 * @param failure
	 * @param responseCode
	 * @param responseReason
	 */
	private void writeServerError(ServerContext serverContext, Throwable failure, int responseCode,
			String responseReason) {
		HttpResponse response = serverContext.getHttpResponse();
		response.setResponseCode(responseCode);
		response.setResponseReason(responseReason);

		StringBuilder responseStrBuilder = new StringBuilder();
		responseStrBuilder
				.append("<h1 style='background-color:#D50000;color:#FFF'>HTTP Status - " + responseCode + "</h1>");
		responseStrBuilder.append("<h3 style='color:#D50000'>message : " + failure.getMessage() + "</h3>");
		responseStrBuilder.append("<h3 style='color:#D50000'>puppy-io [web]</h3>");

		response.setHeader("Content-type", "text/html");
		response.write(responseStrBuilder.toString());
	}
	
	private Session createSession(HttpRequst httpRequst, HttpResponse httpResponse){
		
		String sessionId = null;
		boolean found = false;
		
		//check for already existing cookie
		//cookie format -> Cookie: puppy-io.sessionid=XXX; .....
		String cookieHeader = httpRequst.getHeader("Cookie");
		
		if(cookieHeader != null){
			String[] cookies = cookieHeader.split(";");
			
			
			for(String checkStr : cookies){
				String pattern = ".*\\b" + SessionStore.sessionIdKey + "\\b=(.*)";

				Pattern r = Pattern.compile(pattern);
				Matcher m = r.matcher(checkStr);
				
				if(m.find()){
					sessionId = m.group(1);
					
					if(sessionStore.checkSessionIdExists(sessionId))
						found = true;
					
					break;
				}
				
			}
			
		}
		
		if(!found){
			//add new user to session store
			sessionId = sessionStore.addNewUser();
			
			String cookie = SessionStore.sessionIdKey + "=" + sessionId;
			
			//add Cookie headers to response
			httpResponse.setHeader("Set-Cookie", cookie);
		}
		
		Session session = new SessionImpl(sessionStore, sessionId);
		
		return session;
	}
}
