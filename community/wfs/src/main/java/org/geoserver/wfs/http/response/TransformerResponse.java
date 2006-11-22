package org.geoserver.wfs.http.response;

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;

import org.geoserver.ows.Operation;
import org.geoserver.ows.http.Response;
import org.geotools.xml.transform.TransformerBase;

public class TransformerResponse extends Response {

	public TransformerResponse() {
		super( TransformerBase.class );
	}

	public String getMimeType(Operation operation) {
		return "application/xml";
	}
	
	public void write( Object value, OutputStream output, Operation operation )
			throws IOException {
		
		TransformerBase tx = (TransformerBase) value;
		try {
			tx.transform( this, output );
		} 
		catch (TransformerException e) {
			throw (IOException) new IOException().initCause( e );
		}
	}

	public void abort(Object value, OutputStream output, Operation operation)
			throws IOException {
		
		//do nothing
	}

}
