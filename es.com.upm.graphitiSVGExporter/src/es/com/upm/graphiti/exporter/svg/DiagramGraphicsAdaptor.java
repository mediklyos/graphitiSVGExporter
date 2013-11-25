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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.LinearGradientPaint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.batik.ext.awt.g2d.GraphicContext;
import org.apache.batik.svggen.DOMGroupManager;
import org.apache.batik.svggen.DOMTreeManager;
import org.apache.batik.svggen.ExtensionHandler;
import org.apache.batik.svggen.ImageHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DRuntimeException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.draw2d.geometry.Vector;
import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.algorithms.AbstractText;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.PlatformGraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polygon;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.Rectangle;
import org.eclipse.graphiti.mm.algorithms.RoundedRectangle;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.AdaptedGradientColoredAreas;
import org.eclipse.graphiti.mm.algorithms.styles.Color;
import org.eclipse.graphiti.mm.algorithms.styles.Font;
import org.eclipse.graphiti.mm.algorithms.styles.GradientColoredArea;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.algorithms.styles.Orientation;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.algorithms.styles.PrecisionPoint;
import org.eclipse.graphiti.mm.algorithms.styles.RenderingStyle;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.BoxRelativeAnchor;
import org.eclipse.graphiti.mm.pictograms.ChopboxAnchor;
import org.eclipse.graphiti.mm.pictograms.CompositeConnection;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.CurvedConnection;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.FixPointAnchor;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.ManhattanConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRenderer;
import org.eclipse.graphiti.platform.ga.IGraphicsAlgorithmRendererFactory;
import org.eclipse.graphiti.platform.ga.RendererContext;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.internal.platform.ExtensionManager;
import org.eclipse.graphiti.util.IGradientType;
import org.eclipse.graphiti.util.PredefinedColoredAreas;
/**
 * This class is private in Graphiti. It should be made public in order for getting this exporter to work.
 */
import org.eclipse.graphiti.export.batik.GraphicsToGraphics2DAdaptor;

import org.w3c.dom.Document;

import es.com.upm.batik.svggen.GraphitiDOMGroupManager;


/**
 * Main Class to export Graphiti to SVG via Pictograms Package.
 * Extends SVGGraphics2D.
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class DiagramGraphicsAdaptor extends SVGGraphics2D {
	/**
	 * Interface to include modifications in the exporting process.
	 */
	protected IConfigurationSVGElement icse = null;
	/**
	 * List of Colors.
	 */
	protected List<org.eclipse.graphiti.mm.algorithms.styles.Color> colors;
	/**
	 * List of fonts.
	 */
	protected List<Font> fonts;
	/**
	 * List of points in the connection.
	 */
	protected List<Point> connection = new ArrayList<Point>();
	/**
	 * Identification for the diagram Type.
	 */
	private String diagramTypeId;
	/**
	 * Extended Constructor from SVGGraphics2D
	 * @param domFactory - Factory which will produce Elements for the DOM tree this Graphics2D generates.
	 */
	public DiagramGraphicsAdaptor(Document domFactory) {
		super(domFactory);
	}
	/**
	 * Extended Constructor from SVGGraphics2D
	 * @param document - Factory which will produce Elements for the DOM tree this Graphics2D generates.
	 * @param imageHandler - defines how images are referenced in the generated SVG fragment
	 * @param extensionHandler -defines how Java 2D API extensions map to SVG Nodes.
	 */
	public DiagramGraphicsAdaptor(Document document, ImageHandler imageHandler,
			ExtensionHandler extensionHandler) {
		super(document, imageHandler, extensionHandler, false);
	}
	/**
	 * Extended Constructor from <code>SVGGraphics2D</code>
	 * @param graphics - <code>SVGGraphics2D</code> to clone.
	 */
	public DiagramGraphicsAdaptor(SVGGraphics2D graphics) {
		super(graphics);
	}
	/**
	 * 
     * @param ctx  -<code>SVGGeneratorContext</code> instance that will provide all useful information to the generator.
     * @param textAsShapes - boolean if true, all text is turned into SVG shapes in the conversion. No SVG text is output.
     * @param ic - IConfigurationSVGElement to enable the particular configuration issues.
     * @exception SVGGraphics2DRuntimeException if generatorContext is null.
	 */
	public DiagramGraphicsAdaptor(SVGGeneratorContext ctx, boolean textAsShapes, IConfigurationSVGElement ic) {
		super(ctx, textAsShapes);
		if (ic !=  null) {
			icse = ic;
		}
		setDOMGroupManager(getDOMGroupManager(this.gc, this.domTreeManager, ctx));
	}
	/**
	 * Analyze the elements included in Diagram in order to paint them into Graphics.
	 * @param example - Diagram to be painted.
	 */
	public void analyze(Diagram example) {
		colors = example.getColors();
		fonts = example.getFonts();
		diagramTypeId = example.getDiagramTypeId();
		setDOMGroupManager(getDOMGroupManager(this.gc, this.domTreeManager, this.generatorCtx));
		setDefaults();
		analyzeElement((ContainerShape) example);
		analyzeConnector(example);
	}
	/**
	 * Establish the default paint and font.
	 */
	public void setDefaults() {
		if(!colors.isEmpty())
			setPaint(getColor(colors.get(0)));
		if(!fonts.isEmpty())
			setFont(getFont(fonts.get(0)));
	}
	/**
	 * Analyze List of Shape contained in Container Shape.
	 * @param cs - Container Shape to be analyzed.
	 * @return
	 */
	protected boolean analyzeElement(ContainerShape cs) {
		paint(cs, true);
		List<Shape> shapes = cs.getChildren();
		Iterator<Shape> it = shapes.iterator();
		while (it.hasNext()) {
			Shape s = it.next();
//			setFont(fonts.get(0));
			if (s instanceof ContainerShape) {
				translate(cs.getGraphicsAlgorithm().getX(), cs.getGraphicsAlgorithm().getY());
				getIConfigurationSVGElement().manageGroupManager(domGroupManager, s);
				analyzeElement((ContainerShape) s);
				getIConfigurationSVGElement().manageGroupManager(domGroupManager, null);
				translate(-cs.getGraphicsAlgorithm().getX(), -cs.getGraphicsAlgorithm().getY());
			} else if (s instanceof Shape) {
				getIConfigurationSVGElement().manageGroupManager(domGroupManager, s);
				paint(s, true);
				getIConfigurationSVGElement().manageGroupManager(domGroupManager, null);
			}
		}
		setDefaults();
		return false;
	}
	/**
	 * Analyze List of Connection contained in Diagram.
	 * @param d - Diagram to be analyzed.
	 */
	protected void analyzeConnector(Diagram d) {
		List<Connection> connections = d.getConnections();
		Iterator<Connection> itc = connections.iterator();
		while (itc.hasNext()) {
			Connection c = itc.next();
			getIConfigurationSVGElement().manageGroupManager(domGroupManager, c);
			paint(c, true);
			getIConfigurationSVGElement().manageGroupManager(domGroupManager, null);
		}		
	}
	/**
	 * Paint Connection in the SVGGraphics2D.
	 * @param c - Connection to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(Connection c, boolean checkStyles) {
		connection.clear();
		if (c instanceof FreeFormConnection) {
			paint((FreeFormConnection)c, checkStyles);
		} else if (c instanceof ManhattanConnection) {
			paint((ManhattanConnection) c, checkStyles);
		} else if (c instanceof CurvedConnection) {
			paint((CurvedConnection) c, checkStyles);
		} else if (c instanceof CompositeConnection) {
			paint((CompositeConnection) c, checkStyles);
		}
		getIConfigurationSVGElement().insertPictogramElementConfiguration(c, this);
	}
	/**
	 * Paint the FeeFormConnection in the SVGGraphics2D.
	 * @param ffc - FreeFormConnection to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(FreeFormConnection ffc, boolean checkStyles) {
		GraphicsAlgorithm ga = ffc.getGraphicsAlgorithm();
		int x = 0;
		int y = 0;
		double theta = 0;
		List<Point> bendP = ffc.getBendpoints();
		Anchor origin = ffc.getStart();
		ILocation il = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(origin);
		x = il.getX();
		y = il.getY();
		boolean notMidPoint = true;
		GraphicsAlgorithm g = origin.getGraphicsAlgorithm();
		if (origin instanceof FixPointAnchor || origin instanceof BoxRelativeAnchor) {
			notMidPoint = true;
		} else if (origin instanceof ChopboxAnchor) {
			notMidPoint = false;
			g = ((PictogramElement)origin.eContainer()).getGraphicsAlgorithm();
		}
		if (!bendP.isEmpty()) {
			Point firstPoint = AwtService.getLocation(bendP.get(0), 
					il, g.getWidth(), g.getHeight(), notMidPoint, g);
			connection.add(firstPoint);
			
			// Watch Out. The points are COPIED.
			for (int i = 0; i < bendP.size(); i++) {
				connection.add(Graphiti.getGaService().createPoint(bendP.get(i).getX(), bendP.get(i).getY()));
			}
			x = 0;
			y = 0;
			Anchor target = ffc.getEnd();
			il = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(target);
			x = il.getX();
			y = il.getY();
			g = target.getGraphicsAlgorithm();
			if (target instanceof FixPointAnchor || target instanceof BoxRelativeAnchor) {
				notMidPoint = true;
			} else if (target instanceof ChopboxAnchor) {
				notMidPoint = false;
				g = ((PictogramElement)target.eContainer()).getGraphicsAlgorithm();
			}
			Point lastPoint = AwtService.getLocation(bendP.get(bendP.size() - 1),
					il, g.getWidth(), g.getHeight(), notMidPoint, g);
			connection.add(lastPoint);
			x = lastPoint.getX();
			y = lastPoint.getY();
			theta += Math.atan2(y - bendP.get(bendP.size() - 1).getY(), x - bendP.get(bendP.size() - 1).getX());
		} else {
			Anchor target = ffc.getEnd();
			ILocation ilt = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(target);
			Point targetPoint = Graphiti.getGaCreateService().createPoint(ilt.getX(), ilt.getY());
			Point firstPoint = AwtService.getLocation(targetPoint, 
					il, g.getWidth(), g.getHeight(), notMidPoint, g);
			connection.add(firstPoint);
			g = target.getGraphicsAlgorithm();
			if (target instanceof FixPointAnchor || target instanceof BoxRelativeAnchor) {
				notMidPoint = true;
			} else if (target instanceof ChopboxAnchor) {
				notMidPoint = false;
				g = ((PictogramElement)target.eContainer()).getGraphicsAlgorithm();
			}
			targetPoint = AwtService.getLocation(firstPoint, ilt,  g.getWidth(),g.getHeight(), notMidPoint, g);
			connection.add(targetPoint);
			x = targetPoint.getX();
			y = targetPoint.getY();
			theta += Math.atan2(y - firstPoint.getY(), x - firstPoint.getX());
		}
		paintGraphicsAlgorithm(ga, checkStyles);
		paintConnectionDecorators(ffc, theta, checkStyles);
	}
	/**
	 * Paint the list of ConnectionDecorator of a Connection in the SVGGraphics2D.
	 * @param c - Connection from which obtain the list of ConnectionDecorator.
	 * @param theta - double Angle to locate the list of ConnectionDecorator.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paintConnectionDecorators(Connection c, double theta, boolean checkStyles) {
		List<ConnectionDecorator> cDeco = c.getConnectionDecorators();
		Iterator<ConnectionDecorator> itcd = cDeco.iterator();
		while (itcd.hasNext()) {
			ConnectionDecorator cd = itcd.next();
			ILocation ilcd = Graphiti.getPeService().getLocationRelativeToDiagram(cd);
//			translate(x + cd.getLocation(), y);
			translate(ilcd.getX(), ilcd.getY());
			if (!cd.isActive()) {
				rotate(theta);
			}
			paintGraphicsAlgorithm(cd.getGraphicsAlgorithm(), checkStyles);
			if (!cd.isActive()) {
				rotate(-theta);
			}
//			translate(-(x + cd.getLocation()), -y);
			translate(-ilcd.getX(), -ilcd.getY());
		}
	}
	/**
	 * Paint the ManhattanConnection in the SVGGraphics2D.
	 * @param mc - ManhattanConnection to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(ManhattanConnection mc, boolean checkStyles) {
		createPoints(mc);
		paintGraphicsAlgorithm(mc.getGraphicsAlgorithm(), checkStyles);
		paintConnectionDecorators(mc, 0d, checkStyles);
	}
	/**
	 * Create the points in the ManhattanConnection.
	 * Method extracted and adapted from: org.eclipse.draw2d.ManhattanConnectionRouter
	 * @see ConnectionRouter#route(Connection)
	 * @param mc - ManhattanConnection from which obtain the info to create the points.
	 */
	private void createPoints(ManhattanConnection mc) {
		if ((mc.getStart() == null)
				|| (mc.getEnd() == null))
			return;
		double i;
		Anchor origin = mc.getStart();
		Anchor target = mc.getEnd();
		ILocation ilorigin = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(origin);
		Point pori = Graphiti.getGaCreateService().createPoint(ilorigin.getX(), ilorigin.getY());
		ILocation ilend = Graphiti.getPeService().getLocationRelativeToDiagram(target);
		Point pend = Graphiti.getGaCreateService().createPoint(ilend.getX(), ilend.getY());
		boolean notMidPoint = true;
		GraphicsAlgorithm gOrigin = origin.getGraphicsAlgorithm();
		if (origin instanceof FixPointAnchor || origin instanceof BoxRelativeAnchor) {
			notMidPoint = true;
		} else if (origin instanceof ChopboxAnchor) {
			notMidPoint = false;
			gOrigin = ((PictogramElement)origin.eContainer()).getGraphicsAlgorithm();
		}
		connection.clear();
		Point startPoint = AwtService.getLocation(pend, ilorigin, gOrigin.getWidth(), gOrigin.getHeight(), notMidPoint, gOrigin);
		Vector start = new Vector(startPoint.getX(), startPoint.getY());
		Vector startNormal = AwtService.getDirection(new java.awt.Rectangle(ilorigin.getX(), ilorigin.getY(), gOrigin.getWidth(), gOrigin.getHeight()), notMidPoint, startPoint);

		GraphicsAlgorithm gTarget = target.getGraphicsAlgorithm();
		if (target instanceof FixPointAnchor || target instanceof BoxRelativeAnchor) {
			notMidPoint = true;
		} else if (target instanceof ChopboxAnchor) {
			notMidPoint = false;
			gTarget = ((PictogramElement)target.eContainer()).getGraphicsAlgorithm();
		}
		Point endPoint = AwtService.getLocation(pori, ilend, gTarget.getWidth(), gTarget.getHeight(), notMidPoint, gTarget);
		
		Vector end = new Vector(endPoint.getX(), endPoint.getY());
		Vector endNormal = AwtService.getDirection(new java.awt.Rectangle(ilend.getX(), ilend.getY(), gTarget.getWidth(), gTarget.getHeight()), notMidPoint, endPoint);

		Vector average = start.getAveraged(end);
		Vector direction = new Vector(start, end);

		List<Double> positions = new ArrayList<Double>(5);
		boolean horizontal = startNormal.isHorizontal();
		if (horizontal)
			positions.add(new Double(start.y));
		else
			positions.add(new Double(start.x));
		horizontal = !horizontal;

		if (startNormal.getDotProduct(endNormal) == 0) {
			if ((startNormal.getDotProduct(direction) >= 0)
					&& (endNormal.getDotProduct(direction) <= 0)) {
				// 0
			} else {
				// 2
				if (startNormal.getDotProduct(direction) < 0)
					i = startNormal.getSimilarity(start.getAdded(startNormal
							.getMultiplied(10)));
				else {
					if (horizontal)
						i = average.y;
					else
						i = average.x;
				}
				positions.add(new Double(i));
				horizontal = !horizontal;

				if (endNormal.getDotProduct(direction) > 0)
					i = endNormal.getSimilarity(end.getAdded(endNormal
							.getMultiplied(10)));
				else {
					if (horizontal)
						i = average.y;
					else
						i = average.x;
				}
				positions.add(new Double(i));
				horizontal = !horizontal;
			}
		} else {
			if (startNormal.getDotProduct(endNormal) > 0) {
				// 1
				if (startNormal.getDotProduct(direction) >= 0)
					i = startNormal.getSimilarity(start.getAdded(startNormal
							.getMultiplied(10)));
				else
					i = endNormal.getSimilarity(end.getAdded(endNormal
							.getMultiplied(10)));
				positions.add(new Double(i));
				horizontal = !horizontal;
			} else {
				// 3 or 1
				if (startNormal.getDotProduct(direction) < 0) {
					i = startNormal.getSimilarity(start.getAdded(startNormal
							.getMultiplied(10)));
					positions.add(new Double(i));
					horizontal = !horizontal;
				}

				if (horizontal)
					i = average.y;
				else
					i = average.x;
				positions.add(new Double(i));
				horizontal = !horizontal;

				if (startNormal.getDotProduct(direction) < 0) {
					i = endNormal.getSimilarity(end.getAdded(endNormal
							.getMultiplied(10)));
					positions.add(new Double(i));
					horizontal = !horizontal;
				}
			}
		}
		if (horizontal)
			positions.add(new Double(end.y));
		else
			positions.add(new Double(end.x));

		AwtService.processPositions(start, end, positions, startNormal.isHorizontal(), connection);
	}
	/**
	 * Paint the CurvedConnection in the SVGGraphics2D.
	 * @param cc - CurvedConnection to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(CurvedConnection cc, boolean checkStyles) {
		GraphicsAlgorithm ga = cc.getGraphicsAlgorithm();
		if (createPoints(cc, cc.getStart(), cc.getEnd()))
			paintGraphicsAlgorithm(ga, checkStyles);
		paintConnectionDecorators(cc, 0d, checkStyles);
	}
	/**
	 * Create the points in the CurvedConnection.
	 * @param cc - CurvedConnection from which obtain the info to create the points.
	 * @param origin - Anchor from which the list of points starts.
	 * @param target - Anchor to which the list of points ends.
	 * @return
	 */
	private boolean createPoints(CurvedConnection cc, Anchor origin, Anchor target) {
		List<PrecisionPoint> points = cc.getControlPoints();
		if (origin == null) {
			return false;
		}
		if (target == null) {
			return false;
		}
		ILocation ilorigin = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(origin);
		Point pori = Graphiti.getGaCreateService().createPoint(ilorigin.getX(), ilorigin.getY());
		ILocation ilend = Graphiti.getPeService().getLocationRelativeToDiagram(target);
		Point pend = Graphiti.getGaCreateService().createPoint(ilend.getX(), ilend.getY());
		boolean notMidPoint = true;
		GraphicsAlgorithm g = origin.getGraphicsAlgorithm();
		if (origin instanceof FixPointAnchor || origin instanceof BoxRelativeAnchor) {
			notMidPoint = true;
		} else if (origin instanceof ChopboxAnchor) {
			notMidPoint = false;
			g = ((PictogramElement)origin.eContainer()).getGraphicsAlgorithm();
		}
		connection.clear();
		List<Point> controlPoints = new ArrayList<Point>();
		Point start = AwtService.getLocation(pend, ilorigin, g.getWidth(), g.getHeight(), notMidPoint, g);
		controlPoints.add(start);
		g = target.getGraphicsAlgorithm();
		if (target instanceof FixPointAnchor || target instanceof BoxRelativeAnchor) {
			notMidPoint = true;
		} else if (target instanceof ChopboxAnchor) {
			notMidPoint = false;
			g = ((PictogramElement)target.eContainer()).getGraphicsAlgorithm();
		}
		Point end = AwtService.getLocation(pori, ilend, g.getWidth(), g.getHeight(), notMidPoint, g);
		double gradient = (double) (end.getY() - start.getY()) / (double) (-end.getX() + start.getX());
		double ortho_gradient = -Math.pow(gradient, -1);
		double orthovector_x = 1;
		double orthovector_y = ortho_gradient;
		double factor_to_length = 1 / Math.sqrt((Math.pow(orthovector_y, 2) + Math.pow(orthovector_x, 2)));
		for (PrecisionPoint precisionPoint : points) {

			double orthovector_x_cp = factor_to_length * orthovector_x * precisionPoint.getY();
			double orthovector_y_cp = factor_to_length * orthovector_y * precisionPoint.getY();
			if (Double.isNaN(orthovector_x_cp)) {
				orthovector_x_cp = 0;
			}
			if (Double.isNaN(orthovector_y_cp)) {
				orthovector_y_cp = 1 * precisionPoint.getY();
			}
			Point anchor = Graphiti.getGaCreateService().createPoint((int)
					(start.getX() + (end.getX() - start.getX()) * precisionPoint.getX() - orthovector_x_cp),
					(int)((start.getY() - (start.getY() - end.getY()) * precisionPoint.getX()) + orthovector_y_cp));

			controlPoints.add(anchor);
		}
		
		controlPoints.add(end);
		int precision = 10;
		double factor = 1.0d / precision;
		connection.add(start);
		for (int i = 1; i < precision; i++) {
			int j = 0;
			int sum_x = 0;
			int sum_y = 0;
			for (Point point : controlPoints) {
				sum_x += (AwtService.bezier(j, controlPoints.size() - 1, i * factor) * point.getX());
				sum_y += (AwtService.bezier(j, controlPoints.size() - 1, i * factor) * point.getY());
				j++;
			}
			Point bezierPoint = Graphiti.getGaCreateService().createPoint(sum_x, sum_y);
			connection.add(bezierPoint);
		}
		connection.add(end);
		return true;
	}
	/**
	 * Paint the CompositeConnection in the SVGGraphics2D.
	 * @param cc - CompositeConnection to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	public void paint(CompositeConnection cc, boolean checkStyles) {
		for(CurvedConnection child :cc.getChildren()) {
			GraphicsAlgorithm ga = child.getGraphicsAlgorithm();
			if (createPoints(child, cc.getStart(), cc.getEnd()))
				paintGraphicsAlgorithm(ga, checkStyles);
			paintConnectionDecorators(child, 0d, checkStyles);
		}
		paintConnectionDecorators(cc, 0d, checkStyles);
	}
	/**
	 * Helper method to paint a grid. Painting is optimized as it is restricted
	 * to the Graphics' clip.
	 * Method obtained from FigureUtilities.java in org.eclipse.draw2d.FigureUtilities
	 *  and adapted
	 *  	- less parameters.
	 * @param diagram - Diagram which Grid has to be painted.
	 */
	protected void paintDefaultGrid(Diagram diagram) {
		GraphicsAlgorithm graphicsAlgorithm = diagram.getGraphicsAlgorithm();
		java.awt.Rectangle clip = new java.awt.Rectangle(
				graphicsAlgorithm.getWidth(), graphicsAlgorithm.getHeight());
		int x = 12, y = 12;
		int distanceX = diagram.getGridUnit();
		int distanceY = diagram.getVerticalGridUnit();
		if (distanceY == -1) {
			// No vertical grid unit set (or old diagram before 0.8): use
			// vertical grid unit
			distanceY = distanceX;
		}
		if (distanceX > 0) {
			if (x >= clip.x)
				while (x - distanceX >= clip.x)
					x -= distanceX;
			else
				while (x < clip.x)
					x += distanceX;
			for (int i = x; i < clip.x + clip.width; i += distanceX)
				
				drawLine(i, clip.y, i, clip.y + clip.height);
		}
		if (distanceY > 0) {
			if (y >= clip.y)
				while (y - distanceY >= clip.y)
					y -= distanceY;
			else
				while (y < clip.y)
					y += distanceY;
			for (int i = y; i < clip.y + clip.height; i += distanceY)
				drawLine(clip.x, i, clip.x + clip.width, i);
		}
	}
	/**
	 * Paint Shape into SVGGraphics2D
	 * @param s - Shape to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	protected void paint(Shape s, boolean checkStyles) {
		GraphicsAlgorithm ga = s.getGraphicsAlgorithm();
		List<Anchor> anchors = s.getAnchors();
		Iterator<Anchor> itg = anchors.iterator();
		int x = ga.getX();
		int y = ga.getY();
		if (s instanceof Diagram) {
//			Another Grid will be able to set in future versions.
			paintDefaultGrid((Diagram) s);
		} else if (s instanceof ContainerShape) {
			paintGraphicsAlgorithm(ga, checkStyles);
		} else {
			ILocation il = Graphiti.getPeLayoutService().getLocationRelativeToDiagram(s.getContainer());
			x = il.getX();
			y = il.getY();
			translate(x, y);
			paintGraphicsAlgorithm(ga, checkStyles);
			translate(-x, -y);
		}
		while (itg.hasNext()) {
			Anchor anc = itg.next();
			ILocation ila = Graphiti.getPeService().getLocationRelativeToDiagram(anc);
			translate(ila.getX(), ila.getY());
			paintGraphicsAlgorithm(anc.getGraphicsAlgorithm(), checkStyles);
			translate(-ila.getX(), -ila.getY());
		}
		getIConfigurationSVGElement().insertPictogramElementConfiguration(s, this);
	}
	/**
	 * Paint GraphicsAlgorithm. Set the common attributes to all the AWT elements.
	 * @param ga - GraphicsAlgorithm to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 * @return boolean to check.
	 */
	protected boolean paintGraphicsAlgorithm(GraphicsAlgorithm ga, boolean checkStyles) {
		if (ga == null) {
			return false;
		}
		double transparency = Graphiti.getGaService().getTransparency(ga, checkStyles);
		float alpha = (float) (1.0 - transparency);
		setComposite(AlphaComposite.getInstance(((AlphaComposite) getComposite()).getRule(), alpha));
		setBackground(getColor(Graphiti.getGaService().getBackgroundColor(ga, checkStyles)));
		setColor(getColor(Graphiti.getGaService().getForegroundColor(ga, checkStyles)));
		if (ga.getStyle() != null) {
			TexturePaint lgp = createTextureStyle(ga, checkStyles);
			if (lgp!= null) {
				setPaint(lgp);
			}
		}
		if (Graphiti.getGaService().isLineVisible(ga, checkStyles)) {
			setStroke(createStroke(ga, checkStyles));
		} else {
			setStroke(new BasicStroke(0));
		}
		if (ga instanceof Rectangle) {
			paint((Rectangle) ga, checkStyles);
		} else if (ga instanceof RoundedRectangle) {
			paint((RoundedRectangle) ga, checkStyles);
		} else if (ga instanceof Ellipse) {
			paint((Ellipse) ga, checkStyles);
		} else if (ga instanceof Text) {
			paint((AbstractText) ga, checkStyles);
		} else if (ga instanceof MultiText) {
			paint((MultiText) ga, checkStyles);
		} else if (ga instanceof Polygon) {
			if (ga.getPictogramElement() instanceof ConnectionDecorator) {
				paint((Polygon) ga, false, checkStyles);
			} else {
				paint((Polygon) ga, false, checkStyles);
			}
		} else if (ga instanceof Polyline) {
			if (ga.getPictogramElement() instanceof ConnectionDecorator) {
				paint((Polyline) ga, false, checkStyles);
			} else {
				paint((Polyline) ga, true, checkStyles);
			}
		} else if (ga instanceof Image) {
			paint((Image) ga, checkStyles);
		} else if (ga instanceof PlatformGraphicsAlgorithm) {
			paint((PlatformGraphicsAlgorithm) ga, checkStyles);
		}
		EList<GraphicsAlgorithm> gAlgo = ga.getGraphicsAlgorithmChildren();
		Iterator<GraphicsAlgorithm> itg = gAlgo.iterator();
		while (itg.hasNext()) {
			GraphicsAlgorithm g = itg.next();
			translate(ga.getX(), ga.getY());
			paintGraphicsAlgorithm(g, checkStyles);
			translate(-ga.getX(), -ga.getY());
		}
		getIConfigurationSVGElement().insertGraphicsAlgoritmConfiguration(ga, this);
		return true;
	}
	/**
	 * Paint the element Rectangle in Graphiti.mm.algorithms.
	 * Link the attributes in Rectangle to java.AWT.Rectangle. 
	 * @param rect - Rectangle to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(Rectangle rect, boolean checkStyles) {
		java.awt.Rectangle rectangle = new java.awt.Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
		if (Graphiti.getGaService().isFilled(rect, checkStyles)) {
			// It is needed to add twice the element while the fill and the stroke attributes have to be different.
			if (rect.getStyle() == null) {
				setPaint(getColor(Graphiti.getGaService().getBackgroundColor(rect, checkStyles)));
			}
			fill(rectangle);
		}
		
		// It is needed to add twice the element while the fill and the stroke attributes have to be different.
		setPaint(getColor(Graphiti.getGaService().getForegroundColor(rect, checkStyles)));
		if (Graphiti.getGaService().isLineVisible(rect, checkStyles)) {
			draw(rectangle);
		}
	}
	/**
	 * Paint the element RoundedRectangle in Graphiti.mm.algorithms.
	 * Link the attributes in RoundedRectangle to java.AWT.RoundedRectangle2D.Float. 
	 * @param rrect - RoundedRectangle to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(RoundedRectangle rrect, boolean checkStyles) {
		RoundRectangle2D.Float rrectangle = new RoundRectangle2D.Float(
				rrect.getX(), rrect.getY(), rrect.getWidth(), rrect.getHeight(), rrect.getCornerWidth(), rrect.getCornerHeight());
		if (Graphiti.getGaService().isFilled(rrect, checkStyles)) {
			// It is needed to add twice the element while the fill and the stroke attributes have to be different.
			if (rrect.getStyle() == null) {
				setPaint(getColor(Graphiti.getGaService().getBackgroundColor(rrect, checkStyles)));
			}
			fill(rrectangle);
		}
		// It is needed to add twice the element while the fill and the stroke attributes have to be different.
		setColor(getColor(Graphiti.getGaService().getForegroundColor(rrect, checkStyles)));			
		if (Graphiti.getGaService().isLineVisible(rrect, checkStyles)) {
			draw(rrectangle);
		}
	}
	/**
	 * Paint the element Ellipse in Graphiti.mm.algorithms.
	 * Link the attributes in Ellipse to paint an Oval.
	 * @param ell - Ellipse to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(Ellipse ell, boolean checkStyles) {
		if (Graphiti.getGaService().isFilled(ell, checkStyles))  {
			// It is needed to add twice the element while the fill and the stroke attributes have to be different.
			if (ell.getStyle() == null) {
				setPaint(getColor(Graphiti.getGaService().getBackgroundColor(ell, checkStyles)));
			}
			fillOval(ell.getX(), ell.getY(), ell.getWidth(), ell.getHeight());
		}
		// It is needed to add twice the element while the fill and the stroke attributes have to be different.
		setPaint(getColor(Graphiti.getGaService().getForegroundColor(ell, checkStyles)));			
		if (Graphiti.getGaService().isLineVisible(ell, checkStyles)) {
			drawOval(ell.getX(), ell.getY(), ell.getWidth(), ell.getHeight());
		}
	}
	/**
	 * Paint the element Text in Graphiti.mm.algorithms.
	 * Link the attributes in Text to draw a String in Graphics2D.
	 * @param text - AbstractText to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(AbstractText text, boolean checkStyles) {
		rotate(Graphiti.getGaService().getAngle(text, checkStyles));
		//Clip, Transform, Paint, Font and Composite
		setFont(getFont(Graphiti.getGaService().getFont(text, checkStyles)));
		FontMetrics fm   = getFontMetrics();
		String title = text.getValue();
		int textWidth = text.getWidth();
		int textHeight = text.getHeight();
		if (title != null && title.length() > 0) {
			if (text.getStyleRegions().isEmpty()) {
				java.awt.geom.Rectangle2D rect = fm.getStringBounds(title, this);
				if (textWidth == 0) {
					textWidth  = (int)(rect.getWidth());
				}
				if (textHeight == 0) {
					textHeight = (int)(rect.getHeight());			
				}
				boolean reduced = false;
				if (text.getWidth() > 0 && fm.stringWidth(title) > textWidth) {
					while (fm.stringWidth(title) > textWidth - fm.stringWidth("...")) {
						title = title.substring(0, title.length() - 1);
						reduced = true;
					}
				}
				if (reduced) {
					title = title.concat("...");
				}
				// Align text horizontally and vertically
				int x = text.getX();
				switch (Graphiti.getGaService().getHorizontalAlignment(text, checkStyles).getValue()) {
				case Orientation.ALIGNMENT_CENTER_VALUE:
				case Orientation.ALIGNMENT_MIDDLE_VALUE:
					x += (textWidth - fm.stringWidth(title)) /2;
					break;
				case Orientation.ALIGNMENT_BOTTOM_VALUE:
				case Orientation.ALIGNMENT_RIGHT_VALUE:
					x += textWidth - fm.stringWidth(title);
					break;
				default:
					break;
				}
				int y = text.getY();
				switch (Graphiti.getGaService().getVerticalAlignment(text, checkStyles).getValue()) {
				case Orientation.ALIGNMENT_TOP_VALUE:
				case Orientation.ALIGNMENT_CENTER_VALUE:
				case Orientation.ALIGNMENT_MIDDLE_VALUE:
					y += (textHeight - fm.getHeight()) /2 + fm.getAscent();
					break;
				case Orientation.ALIGNMENT_BOTTOM_VALUE:
				case Orientation.ALIGNMENT_RIGHT_VALUE:
					y += textHeight - fm.getDescent();
					break;
				default:
					y += textHeight;
					break;
				}
				drawString(title, x, y);  // Draw the string.
			} else {
				AwtService.drawRichText(this, false, text, textAsShapes, checkStyles);				
			}
		}
		rotate(-Graphiti.getGaService().getAngle(text, checkStyles));
	}
	/**
	 * Paint the element MultiText in Graphiti.mm.algorithms.
	 * Link the attributes in MultiText to draw several String in Graphics2D.
	 * @param text - MultiText to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(MultiText text, boolean checkStyles) {
		rotate(Graphiti.getGaService().getAngle(text, checkStyles));
		//Clip, Transform, Paint, Font and Composite
		String title = text.getValue();
		AttributedString attributedString;
		java.awt.Font f = getFont(Graphiti.getGaService().getFont(text, checkStyles));
		setFont(f);
		if (title != null && title.length() > 0) {
			if (!text.getStyleRegions().isEmpty()) {
				AwtService.drawRichText(this, false, text, textAsShapes, checkStyles);
			} else {
				attributedString = new AttributedString(title);
				attributedString.addAttribute(TextAttribute.FONT, f);
				attributedString.addAttribute(TextAttribute.FOREGROUND, getColor(Graphiti.getGaService().getForegroundColor(text, checkStyles)));
				AttributedCharacterIterator paragraph = attributedString.getIterator();
				int x = text.getX();
				int y = text.getY();
				int verticalAlignment = Graphiti.getGaService().getVerticalAlignment(text, checkStyles).getValue();
				int horizontalAlignment = Graphiti.getGaService().getHorizontalAlignment(text, checkStyles).getValue();
				drawString(paragraph, x, y, text.getWidth(), text.getHeight(), verticalAlignment, horizontalAlignment);
			}
		}
		rotate(-Graphiti.getGaService().getAngle(text, checkStyles));
	}
	/** 
	 * Paint the element Polygon in Graphiti.mm.algorithms.
	 * Link the attributes in Polygon to java.AWT.Polygon. 
	 * @param pol - Polygon to be painted.
	 * @param polAsPath - boolean to check if Polygon is painted as a Path or as a java.AWT.Polygon.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(Polygon pol, boolean polAsPath, boolean checkStyles) {
		if (polAsPath) {
			GeneralPath path = new GeneralPath();
			List<Point> points = AwtService.toAbsoluteCoordinates(pol);
			points.add(points.get(0));
			Point lineTo = Graphiti.getCreateService().createPoint(0, 0);
			Point quadTo = Graphiti.getCreateService().createPoint(0, 0);;
			path.moveTo(points.get(0).getX(), points.get(0).getY());
			AwtService.determineBezierPoints(points.get(0), points.get(1), lineTo, quadTo);
			
			for (int i = 1; i < points.size() - 1; i++) {
				path.lineTo(lineTo.getX(), lineTo.getY());
				AwtService.determineBezierPoints(points.get(i), points.get(i + 1), lineTo, quadTo);
				path.quadTo(points.get(i).getX(), points.get(i).getY(), quadTo.getX(), quadTo.getY());
			}
			path.lineTo(points.get(points.size() - 1).getX(), points.get(points.size() - 1).getY());
			if (Graphiti.getGaService().isLineVisible(pol, checkStyles)) {
				draw(path);
			}
		} else {
			List<Point> points = AwtService.toAbsoluteCoordinates(pol);
			java.awt.Polygon p = new java.awt.Polygon();
			for (int i = 0; i < points.size(); i++) {
				p.addPoint(points.get(i).getX(), points.get(i).getY());
			}
			if (Graphiti.getGaService().isFilled(pol, checkStyles))  {
				// It is needed to add twice the element while the fill and the stroke attributes have to be different.
				if (pol.getStyle() == null) {
					setPaint(getColor(Graphiti.getGaService().getBackgroundColor(pol, checkStyles)));
				}
				fill(p);
			}
			// It is needed to add twice the element while the fill and the stroke attributes have to be different.
			setPaint(getColor(Graphiti.getGaService().getForegroundColor(pol, checkStyles)));
			if (Graphiti.getGaService().isLineVisible(pol, checkStyles)) {
				draw(p);
			}
		}
	}
	/**
	 * Paint the element Polyline in Graphiti.mm.algorithms.
	 * Link the attributes in Polyline to java.AWT.geom.GeneralPath. 
	 * @param pol - Polyline to be painted.
	 * @param polAsPath - boolean to check if Polygon is painted as a Path or as a simple Polyline.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(Polyline pol, boolean polAsPath, boolean checkStyles) {
		if (polAsPath) {
			List<Point> points = AwtService.toAbsoluteCoordinates(pol);
			GeneralPath path = new GeneralPath();
			
			Point lineTo = Graphiti.getCreateService().createPoint(0, 0);
			Point quadTo = Graphiti.getCreateService().createPoint(0, 0);;
			if (!points.isEmpty()) {
				connection.addAll(points);
			}
			path.moveTo(connection.get(0).getX(), connection.get(0).getY());
			AwtService.determineBezierPoints(connection.get(0), connection.get(1), lineTo, quadTo);
			
			for (int i = 1; i < connection.size() - 1; i++) {
				path.lineTo(lineTo.getX(), lineTo.getY());
				AwtService.determineBezierPoints(connection.get(i), connection.get(i + 1), lineTo, quadTo);
				path.quadTo(connection.get(i).getX(), connection.get(i).getY(), quadTo.getX(), quadTo.getY());
//				path.quadTo(connection.get(i).getX(), connection.get(i).getY(),
//						connection.get(i + 1).getX(), connection.get(i + 1).getY() );
			}
			path.lineTo(connection.get(connection.size() - 1).getX(), connection.get(connection.size() - 1).getY());
			if (Graphiti.getGaService().isLineVisible(pol, checkStyles)) {
				draw(path);
			}
		} else {
			List<Point> points = AwtService.toAbsoluteCoordinates(pol);
			if (!points.isEmpty()) {
				connection.addAll(points);
			}
			int[] xPoints = new int[connection.size()];
			int[] yPoints = new int[connection.size()];
			for (int i = 0; i < connection.size(); i++) {
				xPoints[i] = connection.get(i).getX();
				yPoints[i] = connection.get(i).getY();
			}
			if (Graphiti.getGaService().isLineVisible(pol, checkStyles)) {
				drawPolyline(xPoints, yPoints, connection.size());
			}
		}
		connection.clear();
	}
	/**
	 * Paint the element Image in Graphiti.mm.algorithms.
	 * Link the attributes in Image to java.AWT.Image. 
	 * @param im - Image to be painted.
	 * @param checkStyles - boolean to check styles recursively.
	 */
	private void paint(Image im, boolean checkStyles) {
		if (im.getId() != null) {
			java.awt.Image image = AwtService.getImage(diagramTypeId, im.getId());
			if (image != null) {
				drawImage(image, im.getX(), im.getY(), im.getWidth(), im.getHeight(), null);
			}
			
		}
	}
	/**
	 * Paint PlatformGraphicsAlgorithm in the SVGGraphics2D.
	 * @param pga - PlatformGraphicsAlgorithm
	 * @param checkStyles - boolean to check styles recursively.
	 * This method use a class that is private in Graphiti. It should be made public in order for getting this exporter to work.
	 */
	private void paint(PlatformGraphicsAlgorithm pga, boolean checkStyles) {
		String providerId = ExtensionManager.getSingleton().getDiagramTypeProviderId(diagramTypeId);
		IDiagramTypeProvider dtp = ExtensionManager.getSingleton().createDiagramTypeProvider(providerId);
		IGraphicsAlgorithmRendererFactory garf = dtp.getGraphicsAlgorithmRendererFactory();
		IGraphicsAlgorithmRenderer gar = garf.createGraphicsAlgorithmRenderer(new RendererContext(pga, dtp));
		org.eclipse.draw2d.Shape draw2dShape = (org.eclipse.draw2d.Shape) gar;
		GraphicsToGraphics2DAdaptor gga = new GraphicsToGraphics2DAdaptor(this, draw2dShape.getBounds());
		draw2dShape.paint(gga);
	}

	/**
	 * Sets the java.AWT.Font to paint in Graphics2D. Also returns it.
	 * @param font - Font in Graphiti.mm.algorithms.style.
	 * @return java.AWT.Font painted.
	 */
	protected java.awt.Font getFont(Font font) {
		if (font != null) {
			int style = java.awt.Font.PLAIN;
			if (font.isBold()) {
				if (font.isItalic()) {
					style = java.awt.Font.BOLD | java.awt.Font.ITALIC;
				} else {
					style = java.awt.Font.BOLD;
				}
			} else if (font.isItalic()) {
				style = java.awt.Font.ITALIC;
			}		
//		@SuppressWarnings("restriction")
//		org.eclipse.swt.graphics.Font f = DataTypeTransformation.toSwtFont(font);
//		java.awt.Font awtFont = new java.awt.Font(f.getFontData()[0].getName(), f.getFontData()[0].getStyle(), f.getFontData()[0].getHeight());
			int fontSizeScreen = Math.round(Toolkit.getDefaultToolkit().getScreenResolution() * font.getSize() / 72f);
			java.awt.Font awtFont = new java.awt.Font(font.getName(), style, fontSizeScreen);
			return awtFont;
		}
		return null;
	}
	/**
	 * Copied from GraphicsToGraphics2DAdaptor because there it was private.
	 * Form the Stroke to be painted with the needed GraphicsAlgorithm attributes  
	 * @param ga - GraphicsAlgorithm 
	 * @param checkStyles 
	 */
	private Stroke createStroke(GraphicsAlgorithm ga, boolean checkStyles) {
		int lineWidth = Graphiti.getGaService().getLineWidth(ga, checkStyles);
		
		// line style
		LineStyle lineStyle = Graphiti.getGaService().getLineStyle(ga, checkStyles);
		
		float factor = lineWidth > 0 ? lineWidth : 3;
		float awt_dash[];
		int awt_cap;
		int awt_join;

		if (lineStyle == LineStyle.DASH) {
			awt_dash = new float[] { factor * 6, factor * 3 };
		} else if (lineStyle == LineStyle.DASHDOT) {
			awt_dash = new float[] { factor * 3, factor, factor, factor };
		} else if (lineStyle == LineStyle.DASHDOTDOT) {
			awt_dash = new float[] { factor * 3, factor, factor, factor, factor, factor };
		} else if (lineStyle == LineStyle.DOT) {
			awt_dash = new float[] { factor, factor };
		} else {
			awt_dash = null;
		}
//		switch (currentState.lineAttributes.cap) {
//		case SWT.CAP_FLAT:
//			awt_cap = BasicStroke.CAP_BUTT;
//			break;
//		case SWT.CAP_ROUND:
//			awt_cap = BasicStroke.CAP_ROUND;
//			break;
//		case SWT.CAP_SQUARE:
//			awt_cap = BasicStroke.CAP_SQUARE;
//			break;
//		default:
			awt_cap = BasicStroke.CAP_BUTT;
//		}

//		switch (currentState.lineAttributes.join) {
//		case SWT.JOIN_BEVEL:
//			awt_join = BasicStroke.JOIN_BEVEL;
//			break;
//		case SWT.JOIN_MITER:
//			awt_join = BasicStroke.JOIN_MITER;
//			break;
//		case SWT.JOIN_ROUND:
//			awt_join = BasicStroke.JOIN_ROUND;
//		default:
			awt_join = BasicStroke.JOIN_MITER;
//		}

		/*
		 * SWT paints line width == 0 as if it is == 1, so AWT is synced up with
		 * that below.
		 */
		Stroke stroke = new BasicStroke(lineWidth != 0 ? lineWidth : 1, awt_cap, awt_join,
				(float) 1.0, awt_dash, 0);
		return stroke;
	}
	/**
	 * Create the LinearGradientPaint which links to the Style
	 * Include in the HashMap the Style with the java.AWT.LinearGradientPaint that it's new
	 * @param ga - GraphicsAlgorithm with the style to create the TexturePaint
	 * @param checkStyles - boolean to check styles recursively.
	 * @return
	 */
	private TexturePaint createTextureStyle(GraphicsAlgorithm ga, boolean checkStyles) {
		try {
			RenderingStyle rs = Graphiti.getGaService().getRenderingStyle(ga, checkStyles);
			AdaptedGradientColoredAreas adaptedGradientColoredAreas = rs.getAdaptedGradientColoredAreas();
			List<GradientColoredArea> gc = adaptedGradientColoredAreas.getAdaptedGradientColoredAreas().get(0).getGradientColor();
			Rectangle2D rect = new Rectangle2D.Float(ga.getX(), ga.getY(), ga.getWidth(), ga.getHeight());
			BufferedImage im = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = im.createGraphics();
			Point2D start = new Point2D.Float(0, 0);
			Point2D end = new Point2D.Float(0,100);
			float[] dist = { 0, 1};
			java.awt.Color[] colors = new java.awt.Color[2];
			float length = ga.getHeight();
			if (adaptedGradientColoredAreas.getGradientType().equals(IGradientType.HORIZONTAL)){
				length = ga.getWidth();
			}
			if (length == 0) {
				return null;
			}
			for (int i = 0; i < gc.size(); i++) {
				GradientColoredArea gca = gc.get(i);
				int startPosition = PredefinedColoredAreas.getLocation(gca.getStart(), (int) length, 1d);
				int endPosition = PredefinedColoredAreas.getLocation(gca.getEnd(), (int) length, 1d);
				Rectangle2D r = null;
				if (adaptedGradientColoredAreas.getGradientType().equals(IGradientType.HORIZONTAL)) {
					start = new Point2D.Float(startPosition, 0);
					end = new Point2D.Float(endPosition, 0);
					r = new Rectangle2D.Float(startPosition, 0, endPosition - startPosition, ga.getHeight());
				} else {
					start = new Point2D.Float(0, startPosition);
					end = new Point2D.Float(0, endPosition);
					r = new Rectangle2D.Float(0, startPosition, ga.getWidth(), endPosition - startPosition);
				}
				colors [0] = getColor(gca.getStart().getColor());
				colors [1] = getColor(gca.getEnd().getColor());
				LinearGradientPaint gp = new LinearGradientPaint(start, end, dist, colors);
				g.setPaint(gp);
				g.fill(r);
			}
//			return (MultiLinearGradientPaint) addStyle(p, ((Style)rs.eContainer()).getId());
			TexturePaint tp = new TexturePaint(im, rect);
			return tp;
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * Converts a Graphiti.mm.algorithms.style.Color into a java.AWT.Color
	 * @param toConvert - Color to be converted.
	 * @return java.AWT.Color obtained from Color in Graphiti.
	 */
	protected java.awt.Color getColor(Color toConvert) {
		if (toConvert != null) {
			return new java.awt.Color(toConvert.getRed(), toConvert.getGreen(), toConvert.getBlue());
		}
		return null;
	}
	/**
	 * Advanced Method to paint String like a paragraph where the alignment can be set. 
	 * @param iterator - AttributedCharacterIterator element with the list of String elements
	 * @param x - float Initial Horizontal position.
	 * @param y - float Initial Vertical position.
	 * @param width - int Width of the text container.
	 * @param height - int Height of the text container.
	 * @param verticalAlignment - int Kind of Vertical Alignment:
	 * 		 Orientation.ALIGNMENT_BOTTOM_VALUE,
	 * 		 Orientation.ALIGNMENT_RIGHT_VALUE,
	 * 		 Orientation.ALIGNMENT_CENTER_VALUE,
	 * 		 Orientation.ALIGNMENT_MIDDLE_VALUE
	 * @param horizontalAlignment - int Kind of Horizontal Alignment:
	 * 		 Orientation.ALIGNMENT_BOTTOM_VALUE,
	 * 		 Orientation.ALIGNMENT_RIGHT_VALUE,
	 * 		 Orientation.ALIGNMENT_CENTER_VALUE,
	 * 		 Orientation.ALIGNMENT_MIDDLE_VALUE
	 */
	public void drawString(AttributedCharacterIterator iterator, float x, float y, int width, int height, int verticalAlignment, int horizontalAlignment) {
//		if ((textAsShapes) || (usesUnsupportedAttributes(iterator))) {
			int paragraphStart = iterator.getBeginIndex();
			int paragraphEnd = iterator.getEndIndex();
			FontMetrics fm = getFontMetrics();
			FontRenderContext fontRenderContext = fm.getFontRenderContext();
			LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, fontRenderContext);
			int nLines = 0;
			while (measurer.getPosition() < paragraphEnd) {
				measurer.nextLayout(width);
				nLines++;
			}
			float drawPosY = y;
			switch (verticalAlignment) {	
			case Orientation.ALIGNMENT_BOTTOM_VALUE:	
			case Orientation.ALIGNMENT_RIGHT_VALUE:
				drawPosY += height - nLines * fm.getHeight();
				break;
			case Orientation.ALIGNMENT_CENTER_VALUE:
			case Orientation.ALIGNMENT_MIDDLE_VALUE:
				drawPosY += (height - nLines * fm.getHeight())/2;
				break;
			default:
			}
			// Set position to the index of the first character in the paragraph.
			measurer.setPosition(paragraphStart);
			// Get lines until the entire paragraph has been displayed.
			while (measurer.getPosition() < paragraphEnd) {
				TextLayout textLayout = measurer.nextLayout(width);
				
				// Align text horizontally and vertically
				// Compute pen x position.
				
				float drawPosX;
				switch (horizontalAlignment) {
				case Orientation.ALIGNMENT_BOTTOM_VALUE:
				case Orientation.ALIGNMENT_RIGHT_VALUE:
					drawPosX = (float) x + width - textLayout.getAdvance();
					break;
				case Orientation.ALIGNMENT_CENTER_VALUE:
				case Orientation.ALIGNMENT_MIDDLE_VALUE:
					drawPosX = (float) x + (width - textLayout.getAdvance())/2;
					break;
				default:
					drawPosX = (float) x;
				}
				// Move y-coordinate by the ascent of the layout.
	
				drawPosY += textLayout.getAscent();
				
				// Draw the TextLayout at (drawPosX, drawPosY).
				textLayout.draw(this, drawPosX, drawPosY);
				
				// Move y-coordinate in preparation for next layout.
				drawPosY += textLayout.getDescent() + textLayout.getLeading();
			}
//		} else {
//			super.drawString(iterator, (float) x, (float) y); // versión batik - 1.7 MEJORABLE
//		}
	}
	@Override
	public void draw(java.awt.Shape s) {
		// TODO Auto-generated method stub
		super.draw(s);
	}
	@Override
	public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
		// TODO Auto-generated method stub
		super.drawRenderableImage(img, xform);
	}
	@Override
	public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
		// TODO Auto-generated method stub
		super.drawRenderedImage(img, xform);
	}
	@Override
	public void drawString(String str, float x, float y) {
		// TODO Auto-generated method stub
		super.drawString(str, x, y);
	}
	@Override
	public void drawString(AttributedCharacterIterator iterator, float x, float y) {
		// TODO Auto-generated method stub
		super.drawString(iterator, x, y);
	}
	@Override
	public void fill(java.awt.Shape s) {
		// TODO Auto-generated method stub
		super.fill(s);
	}
	@Override
	public GraphicsConfiguration getDeviceConfiguration() {
		// TODO Auto-generated method stub
		return super.getDeviceConfiguration();
	}
	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy) {
		// TODO Auto-generated method stub
		super.copyArea(x, y, width, height, dx, dy);
	}
	@Override
	public Graphics create() {
		// TODO Auto-generated method stub
		return super.create();
	}
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		super.dispose();
	}
	@Override
	public boolean drawImage(java.awt.Image img, int x, int y, ImageObserver observer) {
		// TODO Auto-generated method stub
		return super.drawImage(img, x, y, observer);
	}
	@Override
	public boolean drawImage(java.awt.Image img, int x, int y, int width, int height,
			ImageObserver observer) {
		// TODO Auto-generated method stub
		return super.drawImage(img, x, y, width, height, observer);
	}
	@Override
	public FontMetrics getFontMetrics(java.awt.Font f) {
		// TODO Auto-generated method stub
		return super.getFontMetrics(f);
	}
	@Override
	public void setXORMode(java.awt.Color c1) {
		// TODO Auto-generated method stub
		super.setXORMode(c1);
	}
	/**
	 * Get DiagramTypeId
	 * @return diagramTypeId.
	 */
	public String getDiagramTypeId() {
		return diagramTypeId;
	}
	/**
	 * Set DiagramTypeId
	 * @param diagramTypeId - String
	 */
	public void setDiagramTypeId(String diagramTypeId) {
		this.diagramTypeId = diagramTypeId;
	}
	/**
	 * Create an instance of GraphitiDOMGroupManager to manage particularly the SVG DOM tree.
	 * @param gc - GraphicContext
	 * @param domTreeManager - DOMTreeManager
	 * @param genc - SVGGeneratorContext
	 * @return DOMGroupManager to be set.
	 */
	public DOMGroupManager getDOMGroupManager(GraphicContext gc,
			DOMTreeManager domTreeManager, SVGGeneratorContext genc) {
		getIConfigurationSVGElement().manageDOMFactory(genc.getDOMFactory());
		GraphitiDOMGroupManager gdgm = new GraphitiDOMGroupManager(gc, domTreeManager);
		gdgm.setIConfigurationSVGElement(getIConfigurationSVGElement());
		return gdgm;
	}
	/**
	 * Get the IConfigurationSVGElement by Default ( DefaultConfigurationSVGElement ) or defined in an extension point.
	 * @return IConfigurationSVGElement to manage locally the model.
	 */
	public IConfigurationSVGElement getIConfigurationSVGElement() {
		if (icse == null) {
			IExtensionPoint extensionPoint =  RegistryFactory.getRegistry().getExtensionPoint(IConfigurationSVGElement.EXTENSION);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] config = extension.getConfigurationElements();
				try {
					for (IConfigurationElement element : config) {
						String currDiagramType = element.getAttribute(IConfigurationSVGElement.EP_ATTRIBUTE_DIAGRAM);
						if (diagramTypeId.equals(currDiagramType)) {
							Object executableExtension = element.createExecutableExtension("class");
							if (executableExtension instanceof IConfigurationSVGElement) {
								icse = (IConfigurationSVGElement) executableExtension;
								return icse;
							}
						}
					}
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			icse = DefaultConfigurationSVGElement.DEFCONF;
		}
		return icse;
	}
}