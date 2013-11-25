/**
 * Copyright 2013 Universidad Politécnica de Madrid - Center for Open Middleware (http://www.centeropenmiddleware.com)
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package es.com.upm.graphiti.export.wizard;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.eclipse.ui.wizards.datatransfer.FileSystemExportWizard;
import org.eclipse.core.resources.IResource;
import org.eclipse.emf.common.util.URI;

import es.com.upm.graphiti.exporter.svg.GraphitiSVGExporter;

/**
 * Workbench wizard for exporting resources from the workspace to the local file system.
 * Select models to export them in SVG files. 
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class SVGExportWizard extends FileSystemExportWizard {
	/**
	 * Main Page.
	 */
	private SVGWizardExportResourcesPage mainPage;
	@Override
	public boolean performFinish() {
		List<Object> ire = (List<Object>) mainPage.getSelection();
		String folderDestination = mainPage.getDestinationValue();
		for (Object obj  :  ire ) {
			String fileName = ((IResource) obj).getFullPath().toString();
			URI uri = URI.createPlatformResourceURI(fileName, true);
			if (fileName != null) {
				GraphitiSVGExporter gSVGExporter = new GraphitiSVGExporter();
				try {
					gSVGExporter.exporter(uri, folderDestination, null);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;		
	}
	@Override
	public void addPages() {
		mainPage = new SVGWizardExportResourcesPage("pageName", null);
		super.addPage(mainPage);
	}
}
