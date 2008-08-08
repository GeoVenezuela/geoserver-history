/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.platform.util;

import org.geotools.util.Version;
import java.beans.PropertyEditorManager;
import java.beans.PropertyEditorSupport;


/**
 * Property editor for the {@link Version} class.
 * <p>
 * Registering this property editor allows versions to be used in a spring
 * context like:
 * <pre>
 * <code>
 * &lt;bean id="..." class="..."&gt;
 *    &lt;constructor-arg value="1.0.0"/&gt;
 * &lt;bean&gt;
 * </code>
 * </pre>
 * </p>
 * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
 *
 */
public class VersionPropertyEditor extends PropertyEditorSupport {
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(new Version(text));
    }
}
