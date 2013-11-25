/**
 * Copyright 2013 Universidad Politécnica de Madrid - Center for Open Middleware (http://www.centeropenmiddleware.com)
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package es.com.upm.graphiti.exporter.svg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.eclipse.emf.ecore.InternalEObject;

import es.com.upm.batik.svggen.GraphitiDOMGroupManager;
/**
 * Default implementation of IConfigurationSVGElement to example the use of the interface.
 * Implementation based on the development project in COM.
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class DefaultConfigurationSVGElement implements IConfigurationSVGElement {
	/**
	 * Basic Instance.
	 */
	public static DefaultConfigurationSVGElement DEFCONF = new DefaultConfigurationSVGElement();
	/**
	 * String with the path to the temporary folder to be used in the URI creation.
	 */
	private String fTempFolder = null;
	/**
	 * Document Element.
	 */
	private Document document;
	/**
	 * Model Object in the Graphiti Model which the information will be obtained.
	 */
	private EObject businessObject = null;
	/**
	 * Boolean to decide the visibility of the GraphicsAlgorithm element depending in the PictogramElement visibility.
	 */
	private boolean visibleBO = true;
	/**
	 * ResourceSet to re-allocate the model and used to obtained the URI. 
	 */
	private ResourceSet fSaveResourceSet = new ResourceSetImpl();
	/**
	 * Default Constructor.
	 */
	public DefaultConfigurationSVGElement() {
		super();
	}
	/**
	 * Improved Constructor
	 * @param fTempFolder - String to the temp folder.
	 * @param resourceset - ResourceSet where to allocate the model.
	 */
	public DefaultConfigurationSVGElement(String fTempFolder, ResourceSet resourceset) {
		super();
		this.fTempFolder = fTempFolder;
		this.fSaveResourceSet = resourceset;
	}
	/**
	 * Default implementation.
	 */
	public boolean insertGraphicsAlgoritmConfiguration(GraphicsAlgorithm ga, SVGGraphics2D g) {
		return true;
	}
	/**
	 * Default implementation.
	 */
	public boolean insertPictogramElementConfiguration(PictogramElement pe, SVGGraphics2D g) {
		return true;
	}
	/**
	 * Default implementation.
	 */
	public boolean manageDOMFactory(Document domFactory) {
		document = domFactory;
		return false;
	}
	/**
	 * 
	 */
	public boolean manageGroupManager(DOMGroupManager domGroupManager, PictogramElement pe) {
		if (pe != null && pe.getLink() != null) {
//			setBusinessObject(
//					((State)shape.getLink().getBusinessObjects().get(0)));
			setBusinessObject((EObject)
					((EList)pe.getLink().eGet(pe.getLink().eClass().getEStructuralFeature("businessObjects"))).get(0));
			setVisibleBO(pe.isVisible());
		} else {
			setBusinessObject(null);
			setVisibleBO(true);
		}
		return true;
	}
	public boolean premanageElement (Element element, Element currentGroup) {
	   	if (currentGroup.hasChildNodes()) {
    		if (businessObject != null) {
    			EObject obj = businessObject;
    			if(businessObject.eIsProxy())
    			{
    				URI lURIOld = ((InternalEObject)businessObject).eProxyURI(); 
    				URI newURI = fSaveResourceSet.getURIConverter().normalize(lURIOld);
    				String fragment = lURIOld.fragment();
					Resource referencedResource;
					try 
					{		
						referencedResource =  fSaveResourceSet.getResource(newURI.trimFragment(), false);
					}
					catch(Exception e)
					{
						// Create a new empty resource.
						referencedResource =	fSaveResourceSet.createResource(newURI.trimFragment());
					}		
					//Check if referencedResource is in resources to export.
					if(referencedResource != null && fSaveResourceSet.getResources().contains(referencedResource))
					{
						obj = EcoreUtil.resolve(businessObject, fSaveResourceSet);
					}
    			}
    			try {
    				Element svgTitle = document.createElementNS(GraphitiDOMGroupManager.SVG_NAMESPACE_URI, 
    						GraphitiDOMGroupManager.SVG_TITLE_TAG);
    				svgTitle.appendChild(document.createTextNode(
    						(String)(obj.eGet(obj.eClass().getEStructuralFeature("name")))));
    				element.appendChild(svgTitle);
    			} catch (Exception e) {
    				System.out.println("Atribute 'Name' not found");
    				//return false;
    			}
    			Element descTitle = document.createElementNS(GraphitiDOMGroupManager.SVG_NAMESPACE_URI, 
    					GraphitiDOMGroupManager.SVG_DESC_TAG);
    			String objectId;
				try {
					objectId = getIdForObject(obj);
					//System.out.println((String)(businessObject.eGet(businessObject.eClass().getEStructuralFeature("name")))+ ":  "+ object);
					descTitle.appendChild(document.createTextNode(objectId));
					if(!isVisibleBO()) {
						element.setAttributeNS(null, GraphitiDOMGroupManager.CSS_VISIBILITY_PROPERTY, GraphitiDOMGroupManager.CSS_HIDDEN_VALUE );
					}
					element.appendChild(descTitle);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
    		}
    	}
		return true;
	}
	/**
	 * Default implementation.
	 */
	public boolean postmanageElement(Element element, Element currentGroup) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	* An id is composed from a specific object. The id consists of the current id of the object and all ids of the
	* parent object
	* 
	* @param obj The tree node object.
	* @return The composed id is returned.
	 * @throws NoSuchAlgorithmException 
	*/
	public String getIdForObject(EObject obj) throws NoSuchAlgorithmException {
		String result = getId(obj);
		EObject parent = obj.eContainer();
		EObject parentOld = null;
		StringBuffer buf = new StringBuffer(result);
		while ((parent != null) && (!parent.equals(parentOld)) && !(parent instanceof ResourceSet)) {
			buf.insert(0, "|");
			buf.insert(0, getId(parent));
			parentOld = parent;
			parent = parentOld.eContainer();
		}		
		buf.insert(0, "|");
		String uriTotal = null;
		if (fTempFolder != null) {
			uriTotal = URI.createFileURI(fTempFolder+obj.eResource().getURI().lastSegment()).toFileString();
		} else {
			URI uri = obj.eResource().getURI();
			String relativePath = "";
			if(uri.isPlatform() || uri.isPlatformResource())
				relativePath = uri.toPlatformString(true);
			else if (uri.isRelative())
				relativePath = uri.toString();
			uri = URI.createURI(Platform.getLocation().toPortableString()+relativePath);
			uriTotal = uri.toString();
		}
		buf.insert(0, calculateMD5Hash(new java.io.File(uriTotal)));			
		result = buf.toString();
		return result;
		
	}
	/**
	 * Get the ID for the object.
	 * @param object - EObject to calculate the ID.
	 * @return the ID for the object.
	 * @throws NoSuchAlgorithmException
	 */
	public static String getId(EObject object) throws NoSuchAlgorithmException {
		if (object instanceof EObject)
			try {
				return convertToHexFormat(((EObject)object).eResource().getURIFragment(((EObject)object)).toString().getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return String.valueOf(object.hashCode());
			}
		else if (object instanceof org.eclipse.emf.ecore.resource.Resource)
		{	
			URI logicalURI = ((org.eclipse.emf.ecore.resource.Resource)object).getURI();
			URI resourceURI = ((org.eclipse.emf.ecore.resource.Resource)object).getResourceSet().getURIConverter().normalize(logicalURI);
			String uriString;
			if(logicalURI.isRelative())
				uriString = Platform.getLocation().toPortableString()+logicalURI.toString();
			else
				uriString = resourceURI.toFileString(); 
			return calculateMD5Hash(new java.io.File( uriString ));
		}
		else if (object instanceof IFile)
			return calculateMD5Hash(new java.io.File(((IFile)object).getLocationURI()));
		else if (object instanceof java.io.File)
			return calculateMD5Hash((java.io.File)object);		
		else
			return String.valueOf(object.hashCode());
	}
	/**
	 * Calculate the MD5 hash from a File.
	 * @param file - File to be used.
	 * @return String with the Md5 Hash.
	 */
	public static String calculateMD5Hash(File file) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			FileInputStream fis = new FileInputStream(file);
			byte[] dataBytes = new byte[1024];
			int nread = 0;
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			};
			byte[] mdbytes = md.digest();
			return convertToHexFormat(mdbytes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return String.valueOf(file.hashCode());
	}	
	/**
	 * Convert an array of bytes into a String in hexadecimal.
	 * @param mdbytes - Array of byte to be converted.
	 * @return String with the String in Hexadecimal.
	 */
	private static String convertToHexFormat(byte[] mdbytes) {
		//convert the byte to hex format
		StringBuffer hexString = new StringBuffer();
		for (int i=0;i<mdbytes.length;i++) {
			String hex=Integer.toHexString(0xff & mdbytes[i]);
			if (hex.length()==1)
				hexString.append('0');
			hexString.append(hex);
			}
    	return hexString.toString();
	}
	/**
	 * Get BusinessObject.
	 * @return the businessObject.
	 */
	public EObject getBusinessObject() {
		return businessObject;
	}
	/**
	 * Set BusinessObject
	 * @param businessObject - EObject to set.
	 */
	public void setBusinessObject(EObject businessObject) {
		this.businessObject = businessObject;
	}
	/**
	 * Check if is visible.
	 * @return Boolean with the information.
	 */
	public boolean isVisibleBO() {
		return visibleBO;
	}
	/**
	 * Set VisibleBO
	 * @param visibleBO - boolean to set
	 */
	public void setVisibleBO(boolean visibleBO) {
		this.visibleBO = visibleBO;
	}
}
