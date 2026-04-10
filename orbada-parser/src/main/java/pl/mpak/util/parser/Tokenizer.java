package pl.mpak.util.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Andrzej Ka�u�a
 * 
 * Tokenizer pozwalaj�cy analizowa� ci�g znak�w
 *
 *  try {
 *    Tokenizer pr = new Tokenizer("system .out.println(\"polopiryn'a\")", new SimpleTokenHandle());
 *    while (!pr.isEof()) {
 *      pr.getNextToken();
 *      System.out.println("T:" +pr.getToken() +", " +pr.getTokenString());
 *    }
 *  }
 *  catch (IOException ex) {
 *    ex.printStackTrace();
 *  }
 */
public class Tokenizer  {

  private StringBuffer source;
  private Reader reader = null;
  private int readedCh = 0;
  private int token = -1;
  
  private int tokenLine = 0;
  private int tokenCol = 0;
  private int tokenStartPos = -1;
  private int currentPos = -1;
  private int currentLine = 0;
  private int currentCol = 0;

  private int bufPos = 0;
  private int bufReaded = -1;
  private char[] buffer = new char[1024];
  
  private StringBuffer tokenBuffer = new StringBuffer();
  private TokenHandle parserHandle = null;
  
  public Tokenizer() {
    super();
    source = new StringBuffer();
  }

  public Tokenizer(Reader reader, TokenHandle parserHandle) throws IOException {
    this();
    setParserHandle(parserHandle);
    setReader(reader);
  }

  public Tokenizer(String text, TokenHandle parserHandle) throws IOException {
    this(new StringReader(text), parserHandle);
  }

  public void setReader(Reader reader) throws IOException {
    if (this.reader != reader) {
      this.reader = reader;
      if (this.reader != null) {
        resetReader();
      }
      else {
        currentPos = -1;
      }
    }
  }
  
  /**
   * Resetuje reader-a do ponownego odczytu
   * Samego strumienia niestety nie resetuje 
   * @throws IOException 
   */
  public void resetReader() throws IOException {
    bufPos = 0;
    bufReaded = -1;
    currentPos = 0;
    currentLine = 1;
    currentCol = 0;
    tokenBuffer.setLength(0);
    source.setLength(0);
    readCh();
  }

  public Reader getReader() {
    return reader;
  }
  
  /**
   * Zwraca informacj� czy osi�gni�to koniec zbioru
   * R�wnoznaczne z getReadedCh() == 0 
   * @return
   */
  public boolean isEof() {
    return readedCh == 0;
  }
  
  /**
   * Kt�ry znak w ci�gu
   * @return
   */
  public int getCurrentPos() {
    return currentPos;
  }
  
  /**
   * Kt�ra linia 
   * @return
   */
  public int getLine() {
    return tokenLine;
  }
  
  /**
   * Kt�ry znak w wierszu
   * @return
   */
  public int getColumn() {
    return tokenCol;
  }
  
  /**
   * Pocz�tkowy znak tokenu w ci�gu znak�w
   * @return
   */
  public int getTokenStartPos() {
    return tokenStartPos;
  }
  
  public final int readCh() throws IOException {
    if (bufPos >= bufReaded) {
      bufReaded = reader.read(buffer);
      if (bufReaded <= 0) {
        return readedCh = 0;
      }
      bufPos = 0;
    }
    currentPos++;
    if ((readedCh = buffer[bufPos++]) == '\n') {
      currentLine++;
      currentCol = 0;
    }
    else {
      currentCol++;
    }
    source.append((char)readedCh);
    return readedCh;
  }
  
  public final int readedCh() {
    return readedCh;
  }
  
  /**
   * Testuje czy pobrany znak jest znakiem podanym
   * @param ch
   * @return
   */
  public final boolean isReadedCh(char ch) {
    return readedCh == ch;
  }
  
  /**
   * Testuje czy pobrany znak jest jednym ze znak�w w text
   * @param ch
   * @return
   */
  public final boolean areReadedCh(String text) {
    return text.indexOf(readedCh) >= 0;
  }
  
  public StringBuffer getTokenBuffer() {
    return tokenBuffer;
  }

  public void setSource(StringBuffer source) {
    this.source = source;
  }

  public StringBuffer getSource() {
    return source;
  }

  public void setParserHandle(TokenHandle parserHandle) {
    this.parserHandle = parserHandle;
  }

  public TokenHandle getParserHandle() {
    return parserHandle;
  }
  
  /**
   * Pobiera nast�pny token
   * @return
   * @throws IOException
   * @see getToken()
   * @see getTokenString()
   */
  public int getNextToken() throws IOException {
    if (parserHandle != null) {
      tokenLine = currentLine;
      tokenCol = currentCol;
      tokenStartPos = currentPos;
      tokenBuffer.setLength(0);
      token = parserHandle.readToken(this, tokenBuffer);
      return token;
    }
    else {
      return token = 0;
    }
  }
  
  public int getNextToken(int skipTokenType) throws IOException {
    while (getNextToken() == skipTokenType);
    return token;
  }
  
  public int getNextToken(int[] skipTokenType) throws IOException {
    getNextToken();
    skip(skipTokenType);
    return token;
  }
  
  /**
   * Zwraca pobrany typ tokenu
   * @return
   */
  public final int getToken() {
    return token;
  }
  
  /**
   * Zwraca pobrany token
   * @return
   */
  public String getString() {
    return tokenBuffer.toString();
  }
  
  /**
   * Zwraca token w postaci numerycznej
   * @return
   */
  public double getNumber() {
    return Double.parseDouble(getString());
  }
  
  /**
   * Zwraca token w postaci liczby ca�kowitej
   * @return
   */
  public int getInteger() {
    return Integer.parseInt(getString());
  }
  
  /**
   * Zwraca token w postaci liczby ca�kowitej du�ej
   * @return
   */
  public BigInteger getBigInteger() {
    return new BigInteger(getString());
  }
  
  /**
   * Zwraca token w postaci liczby rzeczywistej du�ej
   * @return
   */
  public BigDecimal getBigDecimal() {
    return new BigDecimal(getString());
  }
  
  /**
   * Testuje token na r�wno�� z getTokenString()
   * Je�li s� r�wne funkcja zwraca true
   * @param token
   * @param caseInsensitive
   * @return
   */
  public boolean isString(String token, boolean caseInsensitive) {
    return caseInsensitive ? token.equalsIgnoreCase(getString()) : token.equals(getString()); 
  }
  
  /**
   * Testuje ci�gi znak� w tokens na r�wno�� z getTokenString()
   * Je�li kt�rykolwiek jest r�wny funkcja zwraca true
   * @param tokens
   * @param caseInsensitive
   * @return
   */
  public boolean areString(String[] tokens, boolean caseInsensitive) {
    for (String token : tokens) {
      if (isString(token, caseInsensitive)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Pomija elementy podanego typu
   * @param tokenType
   * @throws IOException 
   */
  public void skip(int tokenType) throws IOException {
    if (getToken() == tokenType) {
      while (getNextToken() == tokenType)
        ;
    }
  }
  
  public void skip(int[] tokenType) throws IOException {
    for (int token : tokenType) {
      if (getToken() == token) {
        getNextToken();
      }
    }  
  }
  
}
