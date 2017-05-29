function postClient(url, content) {
	var toInputStreamReader = function(inputStream) {
		if(inputStream==null) {
			inputStream = new java.io.ByteArrayInputStream("".getBytes());
		}
		return new java.io.InputStreamReader(inputStream);
	}

	var obj = new java.net.URL(url);

	var con = obj.openConnection();
	con.setRequestMethod("POST");
	con.setRequestProperty("content-type","application/javascript"); 
	con.setRequestProperty("content-length", java.lang.Integer.toString(content.length()));

	// Send post request
	con.setDoOutput(true);

	var contentWriter = new java.io.DataOutputStream(con.getOutputStream());
	contentWriter.writeBytes(content);
	contentWriter.flush();
	contentWriter.close();

	var responseCode = con.getResponseCode();

	var inStream;
	if (responseCode < 400) {
		inStream = new java.io.BufferedReader(toInputStreamReader(con.getInputStream()));
	} else {
		inStream = new java.io.BufferedReader(toInputStreamReader(con.getErrorStream()));
	}

	var inputLine;
	var response = new java.lang.StringBuffer();

	while ((inputLine = inStream.readLine()) != null) {
		response.append(inputLine);
	}
	inStream.close();

	var responseObj = {
		code : responseCode,
		content : ''+response.toString()
	}
	return responseObj;
}

