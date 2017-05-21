package org.docdriven.diagram.editor;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.mxgraph.canvas.mxGraphicsCanvas2D;
import com.mxgraph.canvas.mxICanvas2D;
import com.mxgraph.reader.mxSaxOutputHandler;
import com.mxgraph.util.mxUtils;

public class Renderer {

	/**
	 * Contains an empty image.
	 */
	public static BufferedImage EMPTY_IMAGE;

	/**
	 * Initializes the empty image.
	 */
	static
	{
		try
		{
			EMPTY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		}
		catch (Exception e)
		{
			// ignore
		}
	}
	
	/**
	 * 
	 */
	private transient SAXParserFactory parserFactory = SAXParserFactory.newInstance();

	/**
	 * Cache for all images.
	 */
	protected transient Hashtable<String, Image> imageCache = new Hashtable<String, Image>();

	/**
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 * 
	 */
	public void renderImage(String url, String format, int w, int h, Color bg, String xml, OutputStream out)
			throws IOException, SAXException, ParserConfigurationException {
		BufferedImage image = mxUtils.createBufferedImage(w, h, bg);

		if (image != null) {
			Graphics2D g2 = image.createGraphics();
			mxUtils.setAntiAlias(g2, true, true);
			renderXml(xml, createCanvas(url, g2));

			ImageIO.write(image, format, out);
		}
	}

	/**
	 * Renders the XML to the given canvas.
	 */
	public void renderXml(String xml, mxICanvas2D canvas)
			throws SAXException, ParserConfigurationException, IOException {
		XMLReader reader = parserFactory.newSAXParser().getXMLReader();
		reader.setContentHandler(new mxSaxOutputHandler(canvas));
		reader.parse(new InputSource(new StringReader(xml)));
	}

	/**
	 * Creates a graphics canvas with an image cache.
	 */
	protected mxGraphicsCanvas2D createCanvas(String url, Graphics2D g2) {
		// Caches custom images for the time of the request
		final Hashtable<String, Image> shortCache = new Hashtable<String, Image>();
		final String domain = url.isEmpty() ? url : url.substring(0, url.lastIndexOf("/"));

		mxGraphicsCanvas2D g2c = new mxGraphicsCanvas2D(g2) {
			public Image loadImage(String src) {
				// Uses local image cache by default
				Hashtable<String, Image> cache = shortCache;

				// Uses global image cache for local images
				if (src.startsWith(domain)) {
					cache = imageCache;
				}

				Image image = cache.get(src);

				if (image == null) {
					image = super.loadImage(src);

					if (image != null) {
						cache.put(src, image);
					} else {
						cache.put(src, EMPTY_IMAGE);
					}
				} else if (image == EMPTY_IMAGE) {
					image = null;
				}

				return image;
			}
		};

		return g2c;
	}

}
