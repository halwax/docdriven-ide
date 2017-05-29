package org.docdriven.script.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * See https://www.mkyong.com/java/how-to-send-http-request-getpost-in-java/
 */
public class HttpUtils {

	private static final String POST = "POST";
	private static final String GET = "GET";

	public static HttpResponse sendGet(String url) throws Exception {

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod(GET);

		return toResponse(con);
	}

	private static HttpResponse toResponse(HttpURLConnection con) throws IOException {

		int responseCode = con.getResponseCode();

		BufferedReader in;
		if (responseCode < 400) {
			in = new BufferedReader(toInputStreamReader(con.getInputStream()));
		} else {
			in = new BufferedReader(toInputStreamReader(con.getErrorStream()));
		}

		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return new HttpResponse(responseCode, response.toString());
	}

	private static InputStreamReader toInputStreamReader(InputStream in) throws IOException {
		if(in==null) {
			in = new ByteArrayInputStream(new byte[0]);
		}
		return new InputStreamReader(in);
	}

	public static HttpResponse sendPost(String url, String content, String contentType) throws Exception {

		URL obj = new URL(url);

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod(POST);
		con.setRequestProperty("content-type",contentType); 
		con.setRequestProperty("content-length", Integer.toString(content.getBytes().length));

		// Send post request
		con.setDoOutput(true);

		DataOutputStream contentWriter = new DataOutputStream(con.getOutputStream());
		contentWriter.writeBytes(content);
		contentWriter.flush();
		contentWriter.close();

		return toResponse(con);

	}

	public static class HttpResponse {
		private int status;
		private String content;

		public HttpResponse(int status, String content) {
			this.status = status;
			this.content = content;
		}

		public int getStatus() {
			return status;
		}

		public String getContent() {
			return content;
		}
	}

}
