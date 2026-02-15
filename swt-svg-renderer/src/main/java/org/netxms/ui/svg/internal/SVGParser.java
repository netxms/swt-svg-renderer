package org.netxms.ui.svg.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.netxms.ui.svg.SVGParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * DOM-based SVG parser. Parses SVG XML into an immutable SVGDocument tree.
 */
public final class SVGParser
{
   private static final Logger logger = Logger.getLogger(SVGParser.class.getName());

   private SVGParser()
   {
   }

   /**
    * Parse SVG from an input stream.
    *
    * @param input the input stream containing SVG XML data
    * @return parsed SVG document tree
    * @throws SVGParseException if the SVG is malformed or the root element is not &lt;svg&gt;
    */
   public static SVGDocument parse(InputStream input) throws SVGParseException
   {
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(false);
         // Disable external entities for security
         factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
         factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
         factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(input);
         return parseDocument(doc);
      }
      catch(SVGParseException e)
      {
         throw e;
      }
      catch(Exception e)
      {
         throw new SVGParseException("Failed to parse SVG: " + e.getMessage(), e);
      }
   }

   /**
    * Parse SVG from a string.
    *
    * @param svgContent the SVG XML content string
    * @return parsed SVG document tree
    * @throws SVGParseException if the SVG is malformed or the root element is not &lt;svg&gt;
    */
   public static SVGDocument parse(String svgContent) throws SVGParseException
   {
      return parse(new ByteArrayInputStream(svgContent.getBytes(StandardCharsets.UTF_8)));
   }

   private static SVGDocument parseDocument(Document doc) throws SVGParseException
   {
      Element root = doc.getDocumentElement();
      if (root == null || !root.getTagName().equalsIgnoreCase("svg"))
      {
         throw new SVGParseException("Root element must be <svg>");
      }

      // Parse viewBox
      float vbX = 0, vbY = 0, vbW = -1, vbH = -1;
      String viewBox = root.getAttribute("viewBox");
      if (viewBox != null && !viewBox.isEmpty())
      {
         String[] parts = viewBox.trim().split("[\\s,]+");
         if (parts.length == 4)
         {
            try
            {
               vbX = Float.parseFloat(parts[0]);
               vbY = Float.parseFloat(parts[1]);
               vbW = Float.parseFloat(parts[2]);
               vbH = Float.parseFloat(parts[3]);
            }
            catch(NumberFormatException e)
            {
               logger.warning("Invalid viewBox: " + viewBox);
            }
         }
      }

      // Parse width/height
      float width = parseDimension(root.getAttribute("width"));
      float height = parseDimension(root.getAttribute("height"));

      // If no viewBox, use width/height as viewBox
      if (vbW < 0 && width > 0)
      {
         vbX = 0;
         vbY = 0;
         vbW = width;
         vbH = height;
      }

      // If no viewBox and no width/height, default to 100x100
      if (vbW < 0)
      {
         logger.warning("SVG has no viewBox or width/height, defaulting to 0 0 100 100");
         vbX = 0;
         vbY = 0;
         vbW = 100;
         vbH = 100;
      }

      // If width/height not specified, use viewBox dimensions
      if (width <= 0)
         width = vbW;
      if (height <= 0)
         height = vbH;

      List<SVGNode> children = parseChildren(root);

      return new SVGDocument(vbX, vbY, vbW, vbH, width, height, children);
   }

   private static List<SVGNode> parseChildren(Element parent)
   {
      List<SVGNode> children = new ArrayList<>();
      NodeList nodes = parent.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++)
      {
         Node node = nodes.item(i);
         if (node.getNodeType() != Node.ELEMENT_NODE)
            continue;

         Element element = (Element)node;
         SVGNode svgNode = parseElement(element);
         if (svgNode != null)
            children.add(svgNode);
      }
      return children;
   }

   private static SVGNode parseElement(Element element)
   {
      String tag = element.getTagName().toLowerCase();

      // Check display:none
      boolean display = true;
      String displayAttr = element.getAttribute("display");
      if ("none".equalsIgnoreCase(displayAttr))
         display = false;

      // Also check inline style for display:none
      String style = element.getAttribute("style");
      if (style != null && style.toLowerCase().contains("display") && style.toLowerCase().contains("none"))
      {
         // More precise check
         String[] decls = style.split(";");
         for (String decl : decls)
         {
            int colon = decl.indexOf(':');
            if (colon >= 0)
            {
               String prop = decl.substring(0, colon).trim().toLowerCase();
               String val = decl.substring(colon + 1).trim().toLowerCase();
               if ("display".equals(prop) && "none".equals(val))
                  display = false;
            }
         }
      }

      StyleProps styleProps = StyleProps.parse(element);
      float[] transform = SVGTransform.parse(element.getAttribute("transform"));

      switch(tag)
      {
         case "g":
         {
            List<SVGNode> children = parseChildren(element);
            return new SVGGroup(styleProps, transform, display, children);
         }
         case "path":
         {
            String d = element.getAttribute("d");
            if (d == null || d.isEmpty())
            {
               logger.warning("Path element missing 'd' attribute, skipping");
               return null;
            }
            List<PathSegment> segments = PathDataParser.parse(d);
            if (segments.isEmpty())
            {
               logger.warning("Path element has malformed path data, skipping");
               return null;
            }
            return new SVGPath(styleProps, transform, display, segments);
         }
         case "rect":
         {
            float x = parseFloatAttr(element, "x", 0);
            float y = parseFloatAttr(element, "y", 0);
            float w = parseFloatAttr(element, "width", 0);
            float h = parseFloatAttr(element, "height", 0);
            float rx = parseFloatAttr(element, "rx", 0);
            float ry = parseFloatAttr(element, "ry", 0);
            // SVG spec: if only one of rx/ry is specified, the other equals it
            if (rx > 0 && ry == 0)
               ry = rx;
            else if (ry > 0 && rx == 0)
               rx = ry;
            if (w <= 0 || h <= 0)
               return null;
            return new SVGRect(styleProps, transform, display, x, y, w, h, rx, ry);
         }
         case "circle":
         {
            float cx = parseFloatAttr(element, "cx", 0);
            float cy = parseFloatAttr(element, "cy", 0);
            float r = parseFloatAttr(element, "r", 0);
            if (r <= 0)
               return null;
            return new SVGCircle(styleProps, transform, display, cx, cy, r);
         }
         case "ellipse":
         {
            float cx = parseFloatAttr(element, "cx", 0);
            float cy = parseFloatAttr(element, "cy", 0);
            float rx = parseFloatAttr(element, "rx", 0);
            float ry = parseFloatAttr(element, "ry", 0);
            if (rx <= 0 || ry <= 0)
               return null;
            return new SVGEllipse(styleProps, transform, display, cx, cy, rx, ry);
         }
         case "line":
         {
            float x1 = parseFloatAttr(element, "x1", 0);
            float y1 = parseFloatAttr(element, "y1", 0);
            float x2 = parseFloatAttr(element, "x2", 0);
            float y2 = parseFloatAttr(element, "y2", 0);
            return new SVGLine(styleProps, transform, display, x1, y1, x2, y2);
         }
         case "polyline":
         {
            float[] points = parsePointsAttr(element.getAttribute("points"));
            if (points == null || points.length < 4)
               return null;
            return new SVGPolyline(styleProps, transform, display, points);
         }
         case "polygon":
         {
            float[] points = parsePointsAttr(element.getAttribute("points"));
            if (points == null || points.length < 4)
               return null;
            return new SVGPolygon(styleProps, transform, display, points);
         }
         case "svg":
         {
            // Nested SVG — treat as group
            List<SVGNode> children = parseChildren(element);
            return new SVGGroup(styleProps, transform, display, children);
         }
         default:
         {
            // Unknown element — log and skip
            logger.fine("Unsupported SVG element: <" + tag + ">, skipping");
            return null;
         }
      }
   }

   private static float parseFloatAttr(Element element, String name, float defaultValue)
   {
      String value = element.getAttribute(name);
      if (value == null || value.isEmpty())
         return defaultValue;
      try
      {
         // Strip "px" suffix if present
         value = value.trim();
         if (value.endsWith("px"))
            value = value.substring(0, value.length() - 2).trim();
         return Float.parseFloat(value);
      }
      catch(NumberFormatException e)
      {
         return defaultValue;
      }
   }

   private static float parseDimension(String value)
   {
      if (value == null || value.isEmpty())
         return -1;
      value = value.trim();
      // Strip common unit suffixes
      if (value.endsWith("px"))
         value = value.substring(0, value.length() - 2).trim();
      else if (value.endsWith("pt"))
         value = value.substring(0, value.length() - 2).trim();
      else if (value.endsWith("em") || value.endsWith("ex"))
         value = value.substring(0, value.length() - 2).trim();
      // Ignore percentage
      if (value.endsWith("%"))
         return -1;
      try
      {
         return Float.parseFloat(value);
      }
      catch(NumberFormatException e)
      {
         return -1;
      }
   }

   private static float[] parsePointsAttr(String value)
   {
      if (value == null || value.isEmpty())
         return null;
      String[] tokens = value.trim().split("[\\s,]+");
      List<Float> points = new ArrayList<>();
      for (String token : tokens)
      {
         if (!token.isEmpty())
         {
            try
            {
               points.add(Float.parseFloat(token));
            }
            catch(NumberFormatException e)
            {
               // skip invalid
            }
         }
      }
      // Must have even number of coordinates
      int count = points.size() & ~1;
      float[] result = new float[count];
      for (int i = 0; i < count; i++)
         result[i] = points.get(i);
      return result;
   }
}
