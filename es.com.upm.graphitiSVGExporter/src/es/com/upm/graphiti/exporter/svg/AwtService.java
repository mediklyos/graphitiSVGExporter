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

import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.draw2d.geometry.Vector;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.mm.algorithms.AbstractText;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.algorithms.styles.TextStyle;
import org.eclipse.graphiti.mm.algorithms.styles.TextStyleRegion;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.internal.platform.ExtensionManager;
import org.eclipse.graphiti.ui.platform.IImageProvider;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.graphiti.ui.services.IExtensionManager;
import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

import es.com.upm.graphiti.adaptor.provider.LocalImageProvider;

/**
 * A collection of static helper methods regarding AWT management.
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class AwtService {
	/**
	 * Gets a Rectangle from {@link #getBox()} and returns the Point where a
	 * line from the center of the Rectangle to the Point <i>reference</i>
	 * intersects the Rectangle or Ellipse.
	 * Adapted Method from: getChopboxLocationOnBox at PeServiceImpl.
	 * Improved for Graphiti v0.9.2 with the adaptation of Point getLocation(Point reference) from 
	 * org.eclipse.graphiti.ui.internal.util.draw2d.GFChopboxAnchor
	 * @param reference - Point to be referenced.
	 * @param loc_rect - ILocation where to place the base Rectangle.
	 * @param width - int Width of the base Rectangle.
	 * @param height - int Height of the base Rectangle.
	 * @param notMidLoc - boolean, if it is true the loc_rect is not the Center point of the Rectangle. 
	 * @param ga - GraphicsAlgorithm to decide if the Point is on an Ellipse or Rectangle.
	 * @return
	 */
	protected static Point getLocation(Point reference, ILocation loc_rect, int width, int height, boolean notMidLoc, GraphicsAlgorithm ga) {
		java.awt.Rectangle r = new java.awt.Rectangle(loc_rect.getX(), loc_rect.getY(), width, height);
		float centerX = r.x;
		float centerY = r.y;
		if (notMidLoc) {
			centerX = r.x + 0.5f * r.width;
			centerY = r.y + 0.5f * r.height;
		}
		float dx = reference.getX() - centerX;
		float dy = reference.getY() - centerY;
		
		if (ga instanceof Ellipse) {

			if (dx == 0)
				return Graphiti.getGaCreateService().createPoint(reference.getX(), (dy > 0) ? (r.y + r.height) : r.y);
			if (dy == 0)
				return Graphiti.getGaCreateService().createPoint((dx > 0) ? (r.x + r.width) : r.x, reference.getY());

			float refx = (dx > 0) ? 0.5f : -0.5f;
			float refy = (dy > 0) ? 0.5f : -0.5f;

			// ref.x, ref.y, r.width, r.height != 0 => safe to proceed

			float k = (float) (dy * r.width) / (dx * r.height);
			k = k * k;

			return Graphiti.getGaCreateService().createPoint((int) (centerX + r.width * refx / Math.sqrt(1 + k)),
					(int) (centerY + r.height * refy / Math.sqrt(1 + 1 / k)));
		} else{
			// CHANGED: in case of "nearly zero" (divide-by-zero or
			// rounding-problems) would happen.
			// Instead return a point on the border of the figure.
			// Doesn't matter which one, because it is directly in the center, so
			// take top-middle.
			float max = Math.max(Math.abs(dx) / r.width, Math.abs(dy) / r.height);
			if (max <= 0.001f) {
				return Graphiti.getGaCreateService().createPoint((int) centerX, r.y);
			}
			
			// r.width, r.height, dx, and dy are guaranteed to be non-zero.
			float scale = 0.5f / max;
			
			dx *= scale;
			dy *= scale;
			centerX += dx;
			centerY += dy;
			
			return Graphiti.getCreateService().createPoint(Math.round(centerX), Math.round(centerY));
		}
	}
//	public java.awt.Point getLocation(java.awt.Point reference, java.awt.Rectangle r, GraphicsAlgorithm ga) {
//
//	if (ga instanceof Polyline) {
//
//	java.awt.Point foreignReference = (java.awt.Point) reference.clone();
//
//	// the midpoint
//	java.awt.Point ownReference = new java.awt.Point((int) r.getCenterX(), (int) r.getCenterY());
//
//	// nice feature!
//	// ownReference = normalizeToStraightlineTolerance(foreignReference,
//	// ownReference, STRAIGHT_LINE_TOLERANCE);
//
//	Point location = getLocation(ownReference, foreignReference);
//	if (location == null || getBox().expand(1, 1).contains(foreignReference)
//			&& !getBox().shrink(1, 1).contains(foreignReference))
//		location = getLocation(getBox().getCenter(), foreignReference);
//
//	if (location == null) {
//		location = getBox().getCenter();
//	}
//
//	return location;
//
//
//		return super.getLocation(reference);
//
//	}
	/**
	 * Change all the Point in the Polyline from local coordinates to Absolute coordinates.
	 * @param pol - Polyline to get the list of Point
	 * @return - List of Point with absolute coordinates.
	 */
	protected static List<Point> toAbsoluteCoordinates(Polyline pol) {
		List<Point> origin = pol.getPoints(); 
		List<Point> list = new ArrayList<Point>(origin.size());
		for (Point p : origin) {
			list.add(Graphiti.getGaCreateService().createPoint(pol.getX() + p.getX(),
					pol.getY() + p.getY(), p.getBefore(), p.getAfter()));
		}
		return list;
	}
	/**
	 * Method to calculate BEZIER Points obtained from 
	 *  org.eclipse.graphiti.ui.internal.figures.GFFigureUtil
	 *  and adapted:
	 *  	- avoiding org.eclipse.draw2d.geometry.Vector
	 *  	- Bezier Distance = 15 [used to calculate
	 *            the rounding of the bezier-curve]
	 * @param c - Point The current control-point.
	 * @param q - Point The point following the current control-point.
	 * @param r - Point The bezier-point, which is calculated in this method. This is a "return-value".
	 * @param s - Point The bezier-point, which is calculated in this method. This is a "return-value".
	 */
	protected static void determineBezierPoints(Point c, Point q, Point r, Point s) {
		// Determine v and m
		// Ray v = new Ray();
		int vx = q.getX() - c.getX();
		int vy = q.getY() - c.getY();
		double absV = Math.sqrt(vx * vx + vy * vy);
		// Ray m = new Ray();
		int mx = Math.round(c.getX() + vx / 2);
		int my = Math.round(c.getY() + vy / 2);

		// Determine tolerance
		// Idea:
		// The vector v is the line after the current control-point c.
		// If the sum of the bezier-distances is greater than the half
		// line-length of v,
		// then a simplified calculation for the bezier-points r and s must be
		// done.
		int tolerance = c.getAfter() + q.getBefore();
		if (tolerance < 30) {
			tolerance = 30;
		}

		// Determine the "results" r and s
		if (absV < tolerance) {
			// use the the midpoint m for r and s
			r.setX(mx);
			r.setY(my);
			s.setX(mx);
			s.setY(my);
		} else {
			double dx = (absV - 15) / absV;
			if (q.getBefore() > 0) {
				dx = (absV - q.getBefore()) / absV;
			}
			r.setX(Math.round(c.getX() + (float) dx * vx));
			r.setY(Math.round(c.getY() + (float) dx * vy));
			double dy = 15 / absV;
			if (c.getAfter() > 0) {
				dy = c.getAfter() / absV;
			}
			s.setX(Math.round(c.getX() + (float) dy * vx));
			s.setY(Math.round(c.getY() + (float) dy * vy));
		}
	}
	/**
	 * Obtain the Image corresponding to the id String which is given as parameter.
	 * A ImageProvider is needed in order to identify the image by Id.
	 * Code obtained from
	 * 			org.eclipse.graphiti.ui.internal.services.impl.ImageService
	 *  in the method
	 *  		 createImageDescriptorForId
	 * @param id - String with the Id of the Image registered in the ImageProvider.
	 * @return java.AWT.Image to be painted.
	 */
	protected static java.awt.Image getImage(String diagramTypeId, String id) {
		if (id != null) {
			try  {
				IExtensionManager extensionManager = GraphitiUi.getExtensionManager();
				String diagramTypeProviderId = extensionManager.getDiagramTypeProviderId(diagramTypeId);
				extensionManager.createDiagramTypeProvider(diagramTypeProviderId);
//				This commented line was the way of getting ImageProviders in previous versions of Graphiti. DEPRECATED. 
//				IImageProvider[] imageProviders = ((ExtensionManager) extensionManager).getImageProviders();
				Collection<IImageProvider> imageProviders = ((ExtensionManager) extensionManager).getImageProvidersForDiagramTypeProviderId(diagramTypeProviderId);
				for (IImageProvider imageProvider : imageProviders) {
					String imageFilePath = imageProvider.getImageFilePath(id);
					if (imageFilePath != null) {
						String pluginId = imageProvider.getPluginId();
						if (pluginId != null) {
							 Bundle bundle = Platform.getBundle(pluginId);
							 // look for the image (this will check both the plugin and fragment folders
							 URL fullPathString = BundleUtility.find(bundle, imageFilePath);
							 BufferedImage img = null;
							 if (fullPathString == null) {
						            try {
						                fullPathString = new URL(imageFilePath);
						            } catch (MalformedURLException e) {
						                return null;
						            }
									URL platformURL = FileLocator.find(fullPathString);
									if (platformURL != null) {
										fullPathString = platformURL;
									}
						        }
							 img = ImageIO.read(fullPathString);
							 return img;
						}
					}
				}
			} catch (Exception e) {
				LocalImageProvider fip = new LocalImageProvider();
				String imageFilePath = fip.getImageFilePath(id);
				BufferedImage img = null;
				try {
					img = ImageIO.read(new File(imageFilePath));
				} catch (Exception ex) {};
				return img;
			};
		}
		return null;
	}
	/**
	 * Process the points in the ManhattanConnection.
	 * Method extracted and adapted from: org.eclipse.draw2d.ManhattanConnectionRouter
	 * @see ConnectionRouter#processPositions(Connection)
	 * @param start
	 * @param end
	 * @param positions
	 * @param horizontal
	 * @param connection
	 */
	protected static void processPositions(Vector start, Vector end, List positions,
			boolean horizontal, List<Point> connection) {
		connection.clear();
		double pos[] = new double[positions.size() + 2];
		if (horizontal)
			pos[0] = start.x;
		else
			pos[0] = start.y;
		int i;
		for (i = 0; i < positions.size(); i++) {
			pos[i + 1] = ((Double) positions.get(i)).intValue();
		}
		if (horizontal == (positions.size() % 2 == 1))
			pos[++i] = end.x;
		else
			pos[++i] = end.y;
		connection.add(Graphiti.getGaCreateService().createPoint((int) start.x, (int) start.y));
		Point p;
		double current, prev, min, max;
		boolean adjust;
		for (i = 2; i < pos.length - 1; i++) {
			horizontal = !horizontal;
			prev = pos[i - 1];
			current = pos[i];

			adjust = (i != pos.length - 2);
			if (horizontal) {
				if (adjust) {
					min = pos[i - 2];
					max = pos[i + 2];
					pos[i] = current = getRowNear(current, min, max);
				}
				p = Graphiti.getGaCreateService().createPoint((int) prev, (int) current);
			} else {
				if (adjust) {
					min = pos[i - 2];
					max = pos[i + 2];
					pos[i] = current = getColumnNear(current, min, max);
				}
				p = Graphiti.getGaCreateService().createPoint((int) current, (int) prev);
			}
			connection.add(p);
		}
		connection.add(Graphiti.getGaCreateService().createPoint((int) end.x, (int) end.y));
	}
	/**
	 * Get the row near for processing the points in the ManhattanConnection.
	 * Method extracted and adapted from: org.eclipse.draw2d.ManhattanConnectionRouter
	 * @see ConnectionRouter#getRowNear(Connection)
	 * @param r
	 * @param n
	 * @param x
	 * @return
	 */
	protected static double getRowNear(double r, double n, double x) {
		double min = Math.min(n, x), max = Math.max(n, x);
		if (min > r) {
			max = min;
			min = r - (min - r);
		}
		if (max < r) {
			min = max;
			max = r + (r - max);
		}

		double proximity = 0d;
		double direction = -1d;
		if (r % 2 == 1)
			r--;
		Double i;
		while (proximity < r) {
			i = new Double(r + proximity * direction);
			return i.intValue();
		}
		return r;
	}
	/**
	 * Returns the direction the point <i>p</i> is in relation to the given
	 * rectangle. Possible values are LEFT (-1,0), RIGHT (1,0), UP (0,-1) and
	 * DOWN (0,1).
	 * Method extracted and adapted from: org.eclipse.draw2d.ManhattanConnectionRouter
	 * @see ConnectionRouter#getDirection(Connection)
	 * @param r - java.awt.Rectangle the rectangle
	 * @param midPoint - boolean, if it is true the rectangle is set on the Center point already. 
	 * @param p - Point the point
	 * @return the direction from <i>r</i> to <i>p</i>
	 */
	protected static Vector getDirection(java.awt.Rectangle r, boolean midPoint, Point p) {
		int x = r.x;
		int y = r.y;
		if (!midPoint) {
			x = r.x - r.width / 2;
			y = r.y - r.height / 2;
		}
		int i, distance = Math.abs(x - p.getX());
		Vector direction;

		direction = new Vector(-1, 0);

		i = Math.abs(y - p.getY());
		if (i <= distance) {
			distance = i;
			direction = new Vector(0, -1);
		}

		i = Math.abs((y + r.height) - p.getY());
		if (i <= distance) {
			distance = i;
			direction = new Vector(0, 1);
		}

		i = Math.abs((x + r.width) - p.getX());
		if (i < distance) {
			distance = i;
			direction = new Vector(1, 0);
		}

		return direction;
	}
	/**
	 * Get the column near for processing the points in the ManhattanConnection.
	 * Method extracted and adapted from: org.eclipse.draw2d.ManhattanConnectionRouter
	 * @see ConnectionRouter#getColumnNear(Connection)
	 * @param r
	 * @param n
	 * @param x
	 * @return
	 */
	private static double getColumnNear(double r, double n, double x) {
		double min = Math.min(n, x), max = Math.max(n, x);
		if (min > r) {
			max = min;
			min = r - (min - r);
		}
		if (max < r) {
			min = max;
			max = r + (r - max);
		}
		double proximity = 0d;
		double direction = -1d;
		if (r % 2 == 1)
			r--;
		Double i;
		while (proximity < r) {
			i = new Double(r + proximity * direction);
			return i.intValue();
		}
		return r;
	}
	/**
	 * Calculate the bezier position.
	 * @param i - int control position.
	 * @param n - int total number.
	 * @param t - double factor.
	 * @return 
	 */
	public static double bezier(int i, int n, double t) {
		return binomialCoefficients(n, i) * Math.pow(t, i) * Math.pow((1 - t), (n - i));
	}
	/**
	 * Binomial Coefficients in the distribution.
	 * @param n - Total number.
	 * @param k - Position.
	 * @return
	 */
	private static long binomialCoefficients(int n, int k) {
		long coeff = 1;
		for (int i = n - k + 1; i <= n; i++) {
			coeff *= i;
		}
		for (int i = 1; i <= k; i++) {
			coeff /= i;
		}
		return coeff;
	}
	/**
	 * org.eclipse.graphiti.ui.internal.figures.GFFigureUtil.drawRichText(Graphics, String, int, int, int, boolean, int, IConfigurationProviderInternal, AbstractText)
	 * @param g
	 * @param draw
	 * @param x
	 * @param y
	 * @param bidiLevel
	 * @param mirrored
	 * @param currentOffset
	 * @param configurationProvider
	 * @param text
	 */
	protected static void drawRichText(DiagramGraphicsAdaptor dga, boolean mirrored, AbstractText text, boolean textAsShapes, boolean checkStyles) {
		String title = text.getValue();
		// Set break width to width of Component.
	    AttributedString attributedString = new AttributedString(title);
		attributedString.addAttribute(TextAttribute.FONT, dga.getFont(Graphiti.getGaService().getFont(text, checkStyles)));
		attributedString.addAttribute(TextAttribute.FOREGROUND, dga.getColor(Graphiti.getGaService().getForegroundColor(text, checkStyles)));
		attributedString.addAttribute(TextAttribute.BACKGROUND, dga.getColor(Graphiti.getGaService().getBackgroundColor(text, checkStyles)));
		attributedString.addAttribute(TextAttribute.STRIKETHROUGH, !TextAttribute.STRIKETHROUGH_ON);
		int maxHeight = 0;
		for (TextStyleRegion style : text.getStyleRegions()) {
			TextStyle gTextStyle = style.getStyle();
			int start = style.getStart();
			int end = style.getEnd() + 1;
			if(style.getEnd()==0)
				end = title.length() + 1;
			if (gTextStyle.getFont() != null) {
				attributedString.addAttribute(TextAttribute.FONT, dga.getFont(gTextStyle.getFont()), start, end);
			}
			if (mirrored)
				attributedString.addAttribute(TextAttribute.RUN_DIRECTION, TextAttribute.RUN_DIRECTION_RTL);
			attributedString.addAttribute(TextAttribute.STRIKETHROUGH, gTextStyle.isStrikeout(), start, end);//		textStyle.strikeout = gTextStyle.isStrikeout();
			if (gTextStyle.isUnderline()) {
				switch (gTextStyle.getUnderlineStyle()) {
			case UNDERLINE_DOUBLE:
				attributedString.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_TWO_PIXEL, start, end);//		textStyle.underlineStyle = gTextStyle.getUnderlineStyle().getValue();
				break;
			case UNDERLINE_ERROR:
			case UNDERLINE_SQUIGGLE:
			default:
				attributedString.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_ONE_PIXEL, start, end);				// 	textStyle.underlineStyle = gTextStyle.getUnderlineStyle().getValue();

				break;
				}				
			}
			org.eclipse.graphiti.mm.algorithms.styles.Color foreground = gTextStyle.getForeground();
			if (foreground != null)
				attributedString.addAttribute(TextAttribute.FOREGROUND, dga.getColor(foreground), start, end);
			org.eclipse.graphiti.mm.algorithms.styles.Color background = gTextStyle.getBackground();
			if (background != null) 
				attributedString.addAttribute(TextAttribute.BACKGROUND, dga.getColor(background), start, end);
			org.eclipse.graphiti.mm.algorithms.styles.Color underlineColor = gTextStyle.getUnderlineColor();
			if (underlineColor != null) {
//				attributedString.addAttribute(TextAttribute.RUN_DIRECTION, value)dga.getColor(underlineColor);
			}
			org.eclipse.graphiti.mm.algorithms.styles.Color strikeoutColor = gTextStyle.getStrikeoutColor();
			if (strikeoutColor != null) {
//				textStyle.strikeoutColor = dga.getColor(strikeoutColor);
			}
		}
		int x = text.getX();
		int y = text.getY();
		AttributedCharacterIterator paragraph = attributedString.getIterator();
		dga.drawString(paragraph, x, y, text.getWidth(), text.getHeight(), 
				Graphiti.getGaService().getVerticalAlignment(text, checkStyles).getValue(), Graphiti.getGaService().getHorizontalAlignment(text, checkStyles).getValue() );
	}
}
