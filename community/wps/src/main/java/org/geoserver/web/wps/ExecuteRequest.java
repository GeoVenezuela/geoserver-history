/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.web.wps;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple bean holding the Execute request parameters, to be used by {@link WPSExecuteTransformer}
 * in order to generate the Execute XML
 */
class ExecuteRequest implements Serializable {
	String processName;
	List<InputParameterValues> inputs;
	List<OutputParameter> outputs;

	public ExecuteRequest(String processName, List<InputParameterValues> inputs,
			List<OutputParameter> outputs) {
		this.processName = processName;
		this.inputs = inputs;
		this.outputs = outputs;
	}
	
	public ExecuteRequest() {
		this.processName = null;
		this.inputs = new ArrayList<InputParameterValues>();
		this.outputs = new ArrayList<OutputParameter>();
	}

}