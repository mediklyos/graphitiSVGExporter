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

import java.util.HashMap;

import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.SVGGraphics2D;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.graphiti.dt.AbstractDiagramTypeProvider;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.tb.IDecorator;
import org.eclipse.graphiti.tb.ImageDecorator;
import org.eclipse.graphiti.ui.internal.platform.ExtensionManager;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Decorator implementation of IConfigurationSVGElement to example the use of the interface.
 * Implementation based on exporting Decorators.
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class DecoratorConfigurationSVGElement implements IConfigurationSVGElement {
	/**
	 * Basic Instance.
	 */
	public static DecoratorConfigurationSVGElement DECCONF = new DecoratorConfigurationSVGElement();
	
	private HashMap<PictogramElement, IDecorator[]> valDecor =  new HashMap<PictogramElement, IDecorator[]>();
	/**
	 * Default Constructor.
	 */
	public DecoratorConfigurationSVGElement() {
		super();
	}
	/**
	 * Obtain the Decorator Array based on diagram Type 
	 * @param cs - Container Shape to get Decorator Array.
	 * @return
	 */
	private IDecorator[] defaultDecorators(ContainerShape cs) {
		EObject root = cs;
		while (root.eContainer() != null) {
			root = root.eContainer();
		}
		String diagramTypeId = ((Diagram) root).getDiagramTypeId();
		String providerId = ExtensionManager.getSingleton().getDiagramTypeProviderId(diagramTypeId);
		IDiagramTypeProvider dtp = ExtensionManager.getSingleton().createDiagramTypeProvider(providerId);
		((AbstractDiagramTypeProvider) dtp).resourceReloaded((Diagram) root);
		return ((AbstractDiagramTypeProvider) dtp).getCurrentToolBehaviorProvider().getDecorators(cs);
	}
	/**
	 * Decorator implementation.
	 * Just for ImageDecorator.
	 */
	public boolean insertGraphicsAlgoritmConfiguration(GraphicsAlgorithm ga, SVGGraphics2D g) {
		IDecorator[] listDecor = valDecor.get(ga.getPictogramElement());
		if (listDecor != null && listDecor.length > 0) {
			try  {
				ImageDecorator imDec = (ImageDecorator)listDecor[0];
				Image im = GraphitiUi.getGaService().createImage(null, imDec.getImageId());
				im.setX(imDec.getX());
				im.setY(imDec.getY());
				im.setBackground(GraphitiUi.getGaService().getBackgroundColor(ga, true));
				im.setForeground(GraphitiUi.getGaService().getForegroundColor(ga, true));
				g.translate(ga.getX(), ga.getY());
				((DiagramGraphicsAdaptor) g).paintGraphicsAlgorithm(im, false);
				g.translate(-ga.getX(), -ga.getY());
			} catch(Exception e) {
				return false;
			}
		}
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
		return true;
	}
	/**
	 * @param featureProvider 
	 * 
	 */
	public boolean manageGroupManager(DOMGroupManager domGroupManager, PictogramElement pe) {
		IDecorator[] listDec = null;
		if (pe != null && pe instanceof ContainerShape) {
			if (valDecor.get(pe) == null) {
				listDec = defaultDecorators((ContainerShape) pe);
				valDecor.put(pe, listDec);
			}
		}
		return true;
	}
	public boolean premanageElement (Element element, Element currentGroup) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * Default implementation.
	 */
	public boolean postmanageElement(Element element, Element currentGroup) {
		// TODO Auto-generated method stub
		return false;
	}
}
