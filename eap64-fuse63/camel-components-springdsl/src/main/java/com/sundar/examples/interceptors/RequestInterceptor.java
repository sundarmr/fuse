package com.sundar.examples.interceptors;

import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Resource;

import org.apache.camel.CamelContext;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingMessage;
import org.apache.cxf.io.CacheAndWriteOutputStream;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.io.CachedOutputStreamCallback;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class RequestInterceptor extends AbstractPhaseInterceptor<Message> {

	private static final String LOG_SETUP = RequestInterceptor.class.getName() + ".log-setup";
	private CamelContext context=null;
	public RequestInterceptor(String phase) {
		super(phase);
	
	}

	public RequestInterceptor(CamelContext context) {
		super(Phase.PRE_STREAM);
		this.context=context;
	}
	
	
	
	

	@Override
	public void handleMessage(Message message) throws Fault {

		
		try {
			final OutputStream os = message.getContent(OutputStream.class);
			if (os == null) {
				return;
			}

			boolean hasLogged = message.containsKey(LOG_SETUP);
			if (!hasLogged) {
				message.put(LOG_SETUP, Boolean.TRUE);
				final CacheAndWriteOutputStream newOut = new CacheAndWriteOutputStream(os);
				message.setContent(OutputStream.class, newOut);
				newOut.registerCallback(new LoggingCallback(message, os));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class LoggingCallback implements CachedOutputStreamCallback {

		private final Message message;
		private final OutputStream origStream;
		
		
		
		public LoggingCallback(final Message msg, final OutputStream os) {
			this.message = msg;
			this.origStream = os;
			
		}

		public void onFlush(CachedOutputStream cos) {

		}

		@Override
		public void onClose(CachedOutputStream os) {
			try {
				InputStream stream = os.getInputStream();
				byte[] bytes = IOUtils.readBytesFromStream(stream);

				String id = (String) message.getExchange().get(LoggingMessage.ID_KEY);
				if (id == null) {
					id = LoggingMessage.nextId();
					message.getExchange().put(LoggingMessage.ID_KEY, id);
				}

				String response = new String(bytes);
				context.createProducerTemplate().asyncSendBody("direct:activemqlog", response);
				System.out.println("Request is : " + response);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
