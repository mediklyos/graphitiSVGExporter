/**
 * Copyright 2013 Universidad Politécnica de Madrid - Center for Open Middleware (http://www.centeropenmiddleware.com)
 * 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package es.com.upm.batik.svggen;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.DOMTreeManager;
import org.apache.batik.svggen.ErrorConstants;
import org.apache.batik.svggen.SVGSyntax;
import org.w3c.dom.Element;

import es.com.upm.graphiti.exporter.svg.DefaultConfigurationSVGElement;
import es.com.upm.graphiti.exporter.svg.IConfigurationSVGElement;

/**
 * This class is used by the Graphics2D SVG Generator to manage
 * a group of Nodes that can later be added to the SVG DOM Tree
 * managed by the DOMTreeManager.
 *
 * There are two rules that control how children nodes are
 * added to the group managed by this class:
 *
 * + Children node are added to the group as long as
 *   there is no more than n graphic context overrides needed to
 *   describe the children style. A graphic context override
 *   happens when style attributes need to be added to a child
 *   node to reflect the state of the graphic context at the
 *   time the child was added. Note that the opacity is never
 *   reflected in a group node and therefore, is not accounted
 *   for in the number of overrides. The number of overrides can
 *   be configured and defaults to 2.
 * + Children nodes are added to the current group as long as
 *   the associated GraphicContext's transform stack is valid.
 *
 * When children nodes can no longer be added, the group is considered
 * complete and the associated DOMTreeManager is notified of the
 * availability of a completed group. Then, a new group is started.
 * <br>
 * The DOMTreeManager is also notified every thime a new element
 * is added to the current group. This is needed to let the
 * DOMTreeManager handle group managers that would be used concurrently.
 *
 * @author jpsalazar
 * @author jpsilvagallino
 * @author <a href="mailto:cjolif@ilog.fr">Christophe Jolif</a>
 * @author <a href="mailto:vincent.hardy@eng.sun.com">Vincent Hardy</a>
 * @version $Id: DOMGroupManager.java,v 1.14 2005/04/02 12:58:17 deweese Exp $
 */
public class GraphitiDOMGroupManager extends DOMGroupManager implements SVGSyntax, ErrorConstants {
	/**
	 * Interface for placing hooks and managing the SVG DOM tree before and after Apache Batik Project works on it.
	 */
	protected IConfigurationSVGElement icse = new DefaultConfigurationSVGElement();
	/**
     * Constructor
     * @param gc graphic context whose state will be reflected in the
     *           element's style attributes.
     * @param domTreeManager DOMTreeManager instance this group manager
     *        cooperates with.
     */
    public GraphitiDOMGroupManager(GraphicContext gc, DOMTreeManager domTreeManager) {
       super(gc, domTreeManager);
    }
	/**
     * Adds a node to the current group, if possible
     * @param element child Element to add to the group
     */
    public void addElement(Element element) {
        super.addElement(element); //addElement(element, (short)(DRAW|FILL));
    }

    /**
     * Adds a node to the current group, if possible
     * Modified to include configuration interface hooks.
     * @param element child Element to add to the group
     */
    public void addElement(Element element, short method) {
    	icse.premanageElement(element, currentGroup);
    	super.addElement(element, method);
    	icse.postmanageElement(element, currentGroup);
    }
    /**
     * getIConfigurationSVGElement
     * @return
     */
	public IConfigurationSVGElement getIConfigurationSVGElement() {
		return icse;
	}
	/**
	 * setIConfigurationSVGElement
	 * @param icse
	 */
	public void setIConfigurationSVGElement(IConfigurationSVGElement icse) {
		this.icse = icse;
	}
}
