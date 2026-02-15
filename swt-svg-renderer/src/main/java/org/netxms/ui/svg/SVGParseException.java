package org.netxms.ui.svg;

/**
 * Checked exception thrown when SVG parsing fails due to malformed XML,
 * missing required attributes, or other structural errors.
 */
public class SVGParseException extends Exception
{
   private static final long serialVersionUID = 1L;

   /**
    * Create a new parse exception with a message.
    *
    * @param message description of the parse error
    */
   public SVGParseException(String message)
   {
      super(message);
   }

   /**
    * Create a new parse exception with a message and cause.
    *
    * @param message description of the parse error
    * @param cause the underlying exception that caused the parse failure
    */
   public SVGParseException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
