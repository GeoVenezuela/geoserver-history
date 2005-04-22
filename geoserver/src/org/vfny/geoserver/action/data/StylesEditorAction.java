/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.vfny.geoserver.action.data;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.vfny.geoserver.action.ConfigAction;
import org.vfny.geoserver.config.DataConfig;
import org.vfny.geoserver.config.StyleConfig;
import org.vfny.geoserver.form.data.StylesEditorForm;
import org.vfny.geoserver.global.GeoserverDataDirectory;
import org.vfny.geoserver.global.UserContainer;
import org.vfny.geoserver.util.SLDValidator;
import org.xml.sax.SAXParseException;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * This class takes care of processing new sld files.  It makes use of a nice
 * upload button, checks to make sure the file isn't already in the system,
 * does a bit of validation, and then adds it to data config.
 *
 * @author rgould
 * @author Chris Holmes, Fulbright
 *
 * @task REVISIT: Still need to do the nice text box to edit the sld file
 *       directly. This will probably involve some trickiness - the work flow
 *       I am thinking is that an upload would just put it into the big style
 *       text box.  On a submit the text box would then be validated and
 *       written out to the file location.
 * @task TODO: write to a temp file before validation.  Right now we delete the
 *       file in the style directory.
 */
public class StylesEditorAction extends ConfigAction {
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        UserContainer user, HttpServletRequest request,
        HttpServletResponse response) throws IOException, ServletException {
        DataConfig config = (DataConfig) getDataConfig();
        StylesEditorForm stylesForm = (StylesEditorForm) form;
        FormFile file = stylesForm.getSldFile();
        final String filename = file.getFileName();
        final String styleID = stylesForm.getStyleID();

        StyleConfig style = user.getStyle();
        

        
        boolean doFullValidation = stylesForm.getFullyValidate();
        if (stylesForm.getFullyValidateChecked() == false) {
        	doFullValidation = false;
        }
        
        if (doFullValidation)
        {
        	List l = getSchemaExceptions(file,request);
        	if (l.size() !=0)
        	{
        		handleValidationErrors(l,file,stylesForm);        		
        		return mapping.findForward("schemaErrors");
        	}
        }

        if (style == null) {
            // Must of bookmarked? Redirect so they can select            
            return mapping.findForward("config.data.style");
        }

        //DJB: changed for geoserver_data_dir
        //File rootDir = new File(getServlet().getServletContext().getRealPath("/"));
        File rootDir = GeoserverDataDirectory.getGeoserverDataDirectory(getServlet().getServletContext());

        //All styles are stored in the data/styles directory.  When we move to
        //the geoserver_home this call will need to change.
        File styleDir = new File(rootDir, "data/styles");

        // send content of FormFile to /styles :
        // there nothing to keep the styles in memory for XMLConfigWriter.store() 
        InputStreamReader isr = new InputStreamReader(file.getInputStream());
        File newSldFile = new File(styleDir, filename);

        //here we do a check to see if the file we are trying to upload is
        //overwriting another style file. 
        LOGGER.fine("new sld file is: " + newSldFile + ", exists: "
            + newSldFile.exists());

        if (newSldFile.exists()) {
            StyleConfig styleForID = config.getStyle(styleID);

            if (styleForID == null) {
                //if there is already a file at the location (file.exists()), and
                //the system does not have a record of this styleId then it means
                //we are trying to add a new sld at a location that would overwrite
                //another's sld file.
                doFileExistsError(newSldFile, request);

                return mapping.findForward("config.data.style.editor");
            } else {
                //if the system has a record of the file, then we need to check if this
                //update is being performed on the correct style id, so we see if the
                //file in the system is the same as this one.  
                File oldFile = styleForID.getFilename();
                LOGGER.fine("old file: " + oldFile + ", newFile: " + newSldFile);

                if (!oldFile.equals(newSldFile)) {
                    doFileExistsError(newSldFile, request);

                    return mapping.findForward("config.data.style.editor");
                }
            }
        }

        //When we have time we should put this in a temp file, to be safe, before
        //we do the validation, and only write to the real style directory when we
        //have things set.  If only java had a nice file copy utility.
        FileWriter fw = new FileWriter(newSldFile);
        char[] tampon = new char[1024];
        int charsRead;

        while ((charsRead = isr.read(tampon, 0, 1024)) != -1) {
            fw.write(tampon, 0, charsRead);
        }

        fw.flush();
        fw.close();
        isr.close();
        style.setFilename(new File(styleDir, filename));

        style.setId(styleID);

        StyleFactory factory = StyleFactory.createStyleFactory();
        SLDParser styleReader = new SLDParser(factory, newSldFile.toURL());
        Style[] readStyles = null;
        Style newStyle;

        try {
            readStyles = styleReader.readXML();

            if (readStyles.length == 0) {
                //I think our style parser does pretty much no error reporting.
                //This is one of the many reasons we need a new SLD parser.
                //We could add new exceptions to it, but it's really just 
                //patching a sinking ship.  One option that we could do
                //here is do a xerces validating parse, to make sure the
                //sld matches the schema before we try to pass it to our
                //crappy parser.
                String message = "The xml was valid, but couldn't get a Style"
                    + " from it.  Make sure your style validates against "
                    + " the SLD schema";
                doStyleParseError(message, newSldFile, request);

                return mapping.findForward("config.data.style.editor");
            }

            newStyle = readStyles[0];
            LOGGER.fine("sld is " + newStyle);
        } catch (Exception e) {
            e.printStackTrace();

            String message = (e.getCause() == null) ? e.getLocalizedMessage()
                                                    : e.getCause()
                                                       .getLocalizedMessage();
            doStyleParseError(message, newSldFile, request);

            return mapping.findForward("config.data.style.editor");
        }

        if (newStyle == null) {
            throw new RuntimeException("new style equals null"); //I don't 

            //think this will ever happen, our SLD parser won't return a null.
        }

        // Do configuration parameters here
        config.addStyle(style.getId(), style);
        getApplicationState().notifyConfigChanged();

        return mapping.findForward("config.data.style");
    }

    /**
     *   make the validation report for the bean
     *   its a listing of the original file (prefixed by line #)
     *   and any validation errors
     * 
	 * @param l
	 * @param file
	 * @param stylesForm
	 */
	private void handleValidationErrors(List errors, FormFile file, StylesEditorForm stylesForm) 
	{
		ArrayList lines = new ArrayList();
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new InputStreamReader(file.getInputStream() ));
			String line = reader.readLine();
			int linenumber = 1;
			int exceptionNum = 0;
			
				 //check for lineNumber -1 errors  --> invalid XML
			
			if  (  errors.size()>0  )
			{
				SAXParseException sax = (SAXParseException) errors.get(0);
				if (sax.getLineNumber()<0)
				{
					lines.add("   INVALID XML: "+sax.getLocalizedMessage());
					lines.add(" ");
					exceptionNum = 1;// skip ahead (you only ever get one error in this case)
				}
			}
			
			while (line !=null)
			{
				line.replace('\n',' ');
				line.replace('\r',' ');
				String header = linenumber +": ";
				lines.add(header+line); // record the current line
				
				boolean keep_going = true;
				while (keep_going)
				{
					if  ( (exceptionNum< errors.size() )  )
					{
						SAXParseException sax = (SAXParseException) errors.get(exceptionNum);
						if (sax.getLineNumber() <= linenumber)
						{
							String head = "---------------------".substring(0,header.length()-1);
							String body = "------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------";
							int colNum = sax.getColumnNumber();  //protect against col 0 problems
							if (colNum <1)
								colNum = 1;
							lines.add(head+body.substring(0,sax.getColumnNumber()-1)+"^");
							lines.add("       "+sax.getLocalizedMessage());
							exceptionNum++;
						}
						else
							keep_going = false; //report later (sax.getLineNumber() > linenumber)
					}
					else
						keep_going = false; // no more errors to report
				}
				
				line = reader.readLine();  //will be null at eof
				linenumber++;				
			}
			for (int t=exceptionNum;t<errors.size();t++)
			{
				SAXParseException sax = (SAXParseException) errors.get(t);
				lines.add("       "+sax.getLocalizedMessage());
			}
			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try{
				if ( reader != null)
					reader.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		stylesForm.setValidationReport( (String[]) lines.toArray( new String[1] ));
	}

	/**
     *   Check the .sld file and check to see if it passes the validation test! 
     * 
	 * @param file
	 * @return
	 */
	private List getSchemaExceptions(FormFile file, HttpServletRequest request) 
	{
		SLDValidator validator = new SLDValidator();
		
		ServletContext sc = request.getSession().getServletContext();
		try{
			List l = validator.validateSLD(file.getInputStream(),sc );
			return l;
		}
		catch (Exception e)
		{
			ArrayList al = new ArrayList();
			al.add( new SAXParseException(e.getLocalizedMessage(),null) );
			return al;
		}	
	}

	/*
     * Called when there is trouble parsing the file.  Note that we
     * also delete the file here, so it doesn't stick on the system.
     * Would be a bit better to write to a temp file before putting
     * it in the style directory, but so it goes.
     */
    private void doStyleParseError(String message, File newSldFile,
        HttpServletRequest request) {
        LOGGER.fine("parse error message is: " + message);

        ActionErrors errors = new ActionErrors();
        errors.add(ActionErrors.GLOBAL_ERROR,
            new ActionError("error.style.noParse", message));
        saveErrors(request, errors);
        newSldFile.delete();
    }

    /*
     * reports an error for an attempt to upload an sld file that is already
     * in the system.*/
    private void doFileExistsError(File file, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        errors.add(ActionErrors.GLOBAL_ERROR,
            new ActionError("error.style.sldFileExists", file.getName()));
        saveErrors(request, errors);
    }
}
