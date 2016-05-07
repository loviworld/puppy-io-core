package com.lovi.puppy.core.web.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.lovi.puppy.core.web.HttpResponse;

public class HttpResponseImpl implements HttpResponse{
	
	private Selector selector;
	private SocketChannel channel;
	private String version = "HTTP/1.1";
    private int responseCode = 200;
    private String responseReason = "OK";
    private Map<String, String> headers = new LinkedHashMap<String, String>();
    private byte[] content;
    
    private Charset charset = Charset.forName("UTF-8");
    private CharsetEncoder encoder = charset.newEncoder();

    public HttpResponseImpl(Selector selector, SocketChannel channel) {
    	this.selector = selector;
    	this.channel = channel;
        headers.put("Date", new Date().toString());
        headers.put("Server", "puppy-io web server");
        headers.put("Connection", "close");
    }

    @Override
    public int getResponseCode() {
        return responseCode;
    }

    @Override
    public String getResponseReason() {
        return responseReason;
    }
    
    @Override
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public void setResponseReason(String responseReason) {
        this.responseReason = responseReason;
    }

    @Override
    public String getHeader(String header) {
        return headers.get(header);
    }
    
    @Override
    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
    
    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void write(Object content) {
        write(content.toString().getBytes());
    }
    
    @Override
    public void write(String content) {
        write(content.getBytes());
    }
    
    @Override
    public void write(Integer content) {
    	write(String.valueOf(content).getBytes());
    }
    
    @Override
    public void write(Short content) {
    	write(String.valueOf(content).getBytes());
    }
    
    @Override
    public void write(Long content) {
    	write(String.valueOf(content).getBytes());
    }
    
    @Override
    public void write(Float content) {
    	write(String.valueOf(content).getBytes());
    }
    
    @Override
    public void write(Double content) {
    	write(String.valueOf(content).getBytes());
    }
    
    @Override
    public void write(Boolean content) {
    	write(String.valueOf(content).getBytes());
    }

    @Override
    public void write(byte[] content) {
        this.content = content;
        headers.put("Content-Length", Integer.toString(content.length));
        
        try {
            writeLine(version + " " + responseCode + " " + responseReason);
            for (Map.Entry<String, String> header : headers.entrySet()) {
                writeLine(header.getKey() + ": " + header.getValue());
            }
            writeLine("");
            channel.write(ByteBuffer.wrap(content));
            channel.register(selector, SelectionKey.OP_WRITE);
           
        } catch (IOException ex) {
           System.err.println(ex.getMessage());
        }
    }

    private void writeLine(String line) throws IOException {
        channel.write(encoder.encode(CharBuffer.wrap(line + "\r\n")));
    }
}
