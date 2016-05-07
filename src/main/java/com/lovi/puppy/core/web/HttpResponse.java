package com.lovi.puppy.core.web;

import java.util.Map;

public interface HttpResponse {

	int getResponseCode();

	String getResponseReason();

	void setResponseCode(int responseCode);

	void setResponseReason(String responseReason);

	String getHeader(String header);

	void setHeader(String key, String value);

	Map<String, String> getHeaders();

	byte[] getContent();

	void write(String content);

	void write(Object content);

	void write(Integer content);

	void write(Short content);

	void write(Long content);

	void write(Float content);

	void write(Double content);

	void write(Boolean content);

	void write(byte[] content);


}
