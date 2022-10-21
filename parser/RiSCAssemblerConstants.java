/* Generated By:JJTree&JavaCC: Do not edit this line. RiSCAssemblerConstants.java */
package parser;


/** 
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface RiSCAssemblerConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int REGISTER = 4;
  /** RegularExpression Id. */
  int LABEL_DEC = 5;
  /** RegularExpression Id. */
  int IDENTIFIER = 6;
  /** RegularExpression Id. */
  int DIRECTIVE = 7;
  /** RegularExpression Id. */
  int IMMEDIATE = 8;
  /** RegularExpression Id. */
  int NEG_IMMEDIATE = 9;
  /** RegularExpression Id. */
  int POS_IMMEDIATE = 10;
  /** RegularExpression Id. */
  int DECIMAL_LITERAL = 11;
  /** RegularExpression Id. */
  int HEX_LITERAL = 12;
  /** RegularExpression Id. */
  int OCTAL_LITERAL = 13;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "<token of kind 3>",
    "<REGISTER>",
    "<LABEL_DEC>",
    "<IDENTIFIER>",
    "<DIRECTIVE>",
    "<IMMEDIATE>",
    "<NEG_IMMEDIATE>",
    "<POS_IMMEDIATE>",
    "<DECIMAL_LITERAL>",
    "<HEX_LITERAL>",
    "<OCTAL_LITERAL>",
    "\"\\n\"",
    "\",\"",
    "\"{\"",
    "\"}\"",
  };

}