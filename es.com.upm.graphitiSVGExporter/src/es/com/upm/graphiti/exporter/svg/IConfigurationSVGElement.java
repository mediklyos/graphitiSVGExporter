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

import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * Interface for placing hooks and managing the SVG DOM tree before and after Apache Batik Project works on it.
 * On this way, it is possible to include some particular details in the DOM tree which aren't in the model.
 * One example can be find in DefaultConfigurationSVGElement.
 * @author jpsalazar
 * @author jpsilvagallino
 */
public interface IConfigurationSVGElement {
	/**
	 * Extension definition for the extension point in the plugin.
	 */
	public static String EXTENSION = "es.com.upm.graphiti.exporter.svg.configurationSVGElement";
	/**
	 * Attribute needed in the extension definition for the plugin.
	 */
	public static String EP_ATTRIBUTE_DIAGRAM = "diagramTypeID";
	/**
	 * Allow to insert any kind of modification in the GraphicsAlgorithm element which hadn't been included previously in the model.
	 * @param ga - GraphicsAlgorithm to be modify in the model.
	 * @param g - SVGGraphics2D g to configure to perform the modifications included.
	 * @return 
	 */
	public boolean insertGraphicsAlgoritmConfiguration(GraphicsAlgorithm ga, SVGGraphics2D g);
	/**
	 * Allow to insert any kind of modification in the PictogramElement element which hadn't been included previously in the model.
	 * @param pe - PictogramElement to be modify in the model.
	 * @param g - SVGGraphics2D g to configure to perform the modifications included.
	 * @return
	 */
	public boolean insertPictogramElementConfiguration(PictogramElement pe, SVGGraphics2D g);
	/**
	 * Allow to manage the whole document and set it on the ConfigurationElement to be used in the interface.
	 * @param domFactory - Document to be used.
	 * @return
	 */
	public boolean manageDOMFactory(Document domFactory);
	/**
	 * Allow to get, to set or manage information from the PictogramElement in the model to the DOMGroupManager.
	 * @param domGroupManager - DOMGroupManager 
	 * @param pe
	 * @return
	 */
	public boolean manageGroupManager(DOMGroupManager domGroupManager, PictogramElement pe);
	/**
	 * Allow to manage the DOM element in the group and the group element before it is set on the group. 
	 * @param element - Element to be managed.
	 * @param currentGroup - Element Group to be managed.
	 * @return
	 */
	public boolean premanageElement(Element element, Element currentGroup);
	/**
	 * Allow to manage the DOM element in the group and the group element after it has been set on the group. 
	 * @param element - Element to be managed.
	 * @param currentGroup - Element Group to be managed.
	 * @return
	 */
	public boolean postmanageElement(Element element, Element currentGroup);
}
