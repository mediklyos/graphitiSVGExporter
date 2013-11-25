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

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.internal.wizards.datatransfer.WizardFileSystemResourceExportPage1;
/**
 * Page of the base resource export-to-file-system Wizard
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class SVGWizardExportResourcesPage extends WizardFileSystemResourceExportPage1//WizardExportResourcesPage
{
	/**
	 * Constructor.
	 * @param pageName
	 * @param selection
	 */
	protected SVGWizardExportResourcesPage(String pageName,
			IStructuredSelection selection) {
		super(pageName, selection);
		// TODO Auto-generated constructor stub
	}
	/**
	 * Check if the resourceName is a valid extension.
	 * @return true always.
	 */
	protected boolean hasExportableExtension(String resourceName) {
		//if (resourceName.endsWith(_EXTENSION)) {
			return true;
		//}
		//return false;
	}
	/**
	 * Get DestinationValue.
	 * @return String with the DestinationValue.
	 */
	public String getDestinationValue() {
		// TODO Auto-generated method stub
		return super.getDestinationValue();
	}
	/**
	 * Get Selection
	 * @return List of the selected Resources.
	 */
	public List getSelection() {
		// TODO Auto-generated method stub
		return getSelectedResources();
	}
}
