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

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.CachedImageHandlerBase64Encoder;
import org.apache.batik.svggen.GenericImageHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.util.URI;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.internal.util.ui.print.IDiagramsExporter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import es.com.upm.graphiti.exporter.xmi.GraphitiDiagramLoader;
/**
 * 
 * @author jpsalazar
 * @author jpsilvagallino
 */
public class GraphitiSVGExporter implements IDiagramsExporter {
	/**
	 * Graphiti Model Extension.
	 */
	private static final String _EXTENSION = ".pictograms"; //$NON-NLS-1$
	/**
	 * Adaptor Graphiti to AWT.Graphics to export to SVG.
	 */
	private DiagramGraphicsAdaptor svgGenerator; 
	
	private  Document document;
	/**
	 * Selection of the file via Dialog
	 * @return
	 */
	public String getFile() {
		FileDialog dialog = new FileDialog(new Frame(), "My Model Selection");
		dialog.setFilenameFilter(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(_EXTENSION)) {
					return true;
				}
				return false;
			}
		});
		dialog.setAlwaysOnTop(true);
		dialog.setVisible(true);
		return dialog.getFile();
	}
	/**
	 * Get a DOM implementation and initialize DiagramGraphitiAdaptor.
	 */
	public void createSVGDocument(IConfigurationSVGElement ic) {
		// Get a DOMImplementation.
		DOMImplementation domImpl =
	    	      GenericDOMImplementation.getDOMImplementation();
	    
	    // Create an instance of org.w3c.dom.Document.
	    String svgNS = "http://www.w3.org/2000/svg";
	    document = domImpl.createDocument(svgNS, "svg", null);
	    
	    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
	    ctx.setExtensionHandler(new GradientExtensionHandler());
	    GenericImageHandler ihandler = new CachedImageHandlerBase64Encoder();
	    ctx.setGenericImageHandler(ihandler);
	    ctx.setEmbeddedFontsOn(false);
	    
	    // Create an instance of the SVG Generator.
//	    svgGenerator = new DiagramGraphicsAdaptor(document, ctx.getImageHandler(), ctx.getExtensionHandler());
	    svgGenerator = new DiagramGraphicsAdaptor(ctx, false, ic);
	}
	/**
	 * Exports the SVGGraphics into a SVG File.
	 * @param file 
	 * @return String - Path to the SVG File.
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 * @throws SVGGraphics2DIOException 
	 */
	public String exporter(URI uri, String folderDestination,  IConfigurationSVGElement ic) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		createSVGDocument(ic);
	    Diagram dModel = GraphitiDiagramLoader.create(uri);
		if (dModel == null) {
//			//if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() instanceof DiagramEditor) {
//			org.eclipse.bpmn2.modeler.ui.editor.BPMN2MultiPageEditor de = ((org.eclipse.bpmn2.modeler.ui.editor.BPMN2MultiPageEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
//				dModel = de.getDesignEditor().getDiagramTypeProvider().getDiagram();
//			//} else {
				return null; 
//			//}
		}
		svgGenerator.analyze(dModel);
		if (uri.isPlatform() || uri.isPlatformResource()) 
			folderDestination += uri.trimFileExtension().toPlatformString(true)+".svg";
		else if (uri.isRelative())
			folderDestination += uri.trimFileExtension().toString()+".svg";
		else 
			folderDestination += uri.trimFileExtension().lastSegment()+".svg";
		// Finally, stream out SVG to the standard output using
	    // UTF-8 encoding.
		File svgFile = new File(folderDestination);
		svgFile.getParentFile().mkdirs();
		boolean useCSS = false; // we dont want to use CSS style attributes
	    Writer out = new OutputStreamWriter(
	    		new FileOutputStream(svgFile), "UTF-8");
	    svgGenerator.stream(out, useCSS);
	    return folderDestination;
	}
	/**
	 * Exports the SVGGraphics into a SVG File.
	 * @param file 
	 * @return String - Path to the SVG File.
	 * @throws FileNotFoundException 
	 * @throws UnsupportedEncodingException 
	 * @throws SVGGraphics2DIOException 
	 */
	public String exporter(Diagram dModel, String file, String folderDestination) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		createSVGDocument(null);
		if (dModel == null) {
			return null; 
		}
		svgGenerator.analyze(dModel);
		folderDestination += file.substring(0, file.lastIndexOf('.'))+".svg";
		// Finally, stream out SVG to the standard output using
	    // UTF-8 encoding.
		File svgFile = new File(folderDestination);
		svgFile.getParentFile().mkdirs();
	    boolean useCSS = false; // we dont want to use CSS style attributes
	    Writer out = new OutputStreamWriter(
	    		new FileOutputStream(svgFile), "UTF-8");
	    svgGenerator.stream(out, useCSS);
	    return folderDestination;
	}

	public static void main(String[] args) throws IOException {
		GraphitiSVGExporter gSVGExporter = new GraphitiSVGExporter();
		String file = gSVGExporter.getFile();
		URI uri = URI.createURI(file);
		String folderPath = "R:/Juan Pablo/WebTool/SVG/";
		System.out.println("FILE EXPORTED::> " + gSVGExporter.exporter(uri, folderPath, null));
		System.exit(0);
	}
	public void export(Image im, IFigure figure, String fileName,
			Double scaleFactor) throws Exception {
		createSVGDocument(null);
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor() instanceof DiagramEditor) {
			DiagramEditor de = ((DiagramEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
			IDiagramTypeProvider diagramTypeProvider = de.getDiagramTypeProvider();
			Diagram dModel = diagramTypeProvider.getDiagram();
    	    svgGenerator.analyze(dModel);
    	    // Finally, stream out SVG to the standard output using
    	    // UTF-8 encoding.
    	    File svgFile = new File(fileName);
    	    svgFile.getParentFile().mkdirs();
    	    boolean useCSS = false; // we dont want to use CSS style attributes
    	    Writer out = new OutputStreamWriter(
    	    		new FileOutputStream(svgFile), "UTF-8");
    	    svgGenerator.stream(out, useCSS);
		} else {
//			org.eclipse.bpmn2.modeler.ui.editor.BPMN2MultiPageEditor de = ((org.eclipse.bpmn2.modeler.ui.editor.BPMN2MultiPageEditor)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor());
//			Diagram dModel = de.getDesignEditor().getDiagramTypeProvider().getDiagram();
//    	    svgGenerator.analyze(dModel);
//    	    // Finally, stream out SVG to the standard output using
//    	    // UTF-8 encoding.
//    	    File svgFile = new File(fileName);
//    	    svgFile.getParentFile().mkdirs();
//    	    boolean useCSS = false; // we dont want to use CSS style attributes
//    	    Writer out = new OutputStreamWriter(
//    	    		new FileOutputStream(svgFile), "UTF-8");
//    	    svgGenerator.stream(out, useCSS);
		}
	}
	public DiagramGraphicsAdaptor getSvgGenerator() {
		return svgGenerator;
	}
	public void setSvgGenerator(DiagramGraphicsAdaptor svgGenerator) {
		this.svgGenerator = svgGenerator;
	}
	public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
}