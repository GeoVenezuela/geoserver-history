/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.vfny.geoserver.action.validation;

import java.beans.PropertyDescriptor;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.vfny.geoserver.action.ConfigAction;
import org.vfny.geoserver.config.validation.TestConfig;
import org.vfny.geoserver.config.validation.TestSuiteConfig;
import org.vfny.geoserver.config.validation.ValidationConfig;
import org.vfny.geoserver.form.validation.ValidationTestEditorForm;

/**
 * ValidationTestEditorAction purpose.
 * <p>
 * Description of ValidationTestEditorAction ...
 * </p>
 * 
 * <p>
 * Capabilities:
 * </p>
 * <ul>
 * <li>
 * Feature: description
 * </li>
 * </ul>
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * ValidationTestEditorAction x = new ValidationTestEditorAction(...);
 * </code></pre>
 * 
 * @author User, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: ValidationTestEditorAction.java,v 1.1 2004/01/31 00:27:26 jive Exp $
 */
public class ValidationTestEditorAction extends ConfigAction {
    public ActionForward execute(ActionMapping mapping,
            ActionForm incomingForm, HttpServletRequest request,
            HttpServletResponse response) {
        
        ValidationTestEditorForm form = (ValidationTestEditorForm) incomingForm;
        
        String name = form.getName();
        String description = form.getDescription();
        List attributeKeys = form.getAttributeKeys();
        List attributeValues = form.getAttributeValues();
       
        ServletContext context = getServlet().getServletContext();
        ValidationConfig validationConfig = (ValidationConfig) context.getAttribute(ValidationConfig.CONFIG_KEY);
        TestSuiteConfig suiteConfig = (TestSuiteConfig) request.getSession().getAttribute(TestSuiteConfig.CURRENTLY_SELECTED_KEY);
        TestConfig testConfig = (TestConfig) request.getSession().getAttribute(TestConfig.CURRENTLY_SELECTED_KEY);
        
        //this allows renaming. If they change the test's name, we just remove it add a new one
        suiteConfig.removeTest(testConfig.getName());
        
        testConfig.setName(name);
        testConfig.setDescription(description);
        
        for (int i = 0; i < attributeKeys.size(); i++) {
            System.out.println((String) attributeKeys.get(i)+"="+ (String) attributeValues.get(i));
            System.out.println(testConfig.getArgs());
            testConfig.setArgStringValue((String) attributeKeys.get(i), (String) attributeValues.get(i));
        }
       
        suiteConfig.addTest(testConfig);
        
        request.getSession().removeAttribute(TestConfig.CURRENTLY_SELECTED_KEY);
        
        return mapping.findForward("validationTest");
    }
}
