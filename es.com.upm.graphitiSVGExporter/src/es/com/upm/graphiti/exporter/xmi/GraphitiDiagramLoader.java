/**
 * Copyright 2013 Universidad Politécnica de Madrid - Center for Open Middleware (http://www.centeropenmiddleware.com)
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package es.com.upm.graphiti.exporter.xmi;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramsPackage;
/**
 * 
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class GraphitiDiagramLoader {
	/**
	 * Obtain the Graphiti Diagram from a the URI.
	 * @param uri - URI to obtain the Graphiti Diagram.
	 * @return Diagram of the URI model.
	 */
	public static Diagram create(URI uri) {
		if (uri == null) {
			return null;
		}
		PictogramsPackage.eINSTANCE.eClass();
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("pictograms", new XMIResourceFactoryImpl());
	    ResourceSet resSet = new ResourceSetImpl();
	    // Get the resource
	    try {
	    	Resource resource = resSet.getResource(uri, true);
	    	// Get the first model element and cast it to the right type, in my
	    	// example everything is hierarchical included in this first node
	    	for (EObject obj : resource.getContents()) {
	    		if (obj instanceof Diagram) {
	    			return (Diagram) obj;
	    		}
	    	}
	    } catch (Exception e) {
	    	return null;
	    };
	    return null;
	}
	/**
	 * Main Function to check working.
	 * @param args - null
	 */
	public static void main(String[] args) {
		FileDialog dialog = new FileDialog(new Frame(), "My File Selection");
		dialog.setFilenameFilter(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".pictograms")) {
					return true;
				}
				return false;
			}
		});
		dialog.setVisible(true);
		String file = dialog.getFile();
		URI uri = URI.createURI(file);
		if (file != null) {
			GraphitiDiagramLoader.create(uri);
		}
		System.exit(0);
	}
}
