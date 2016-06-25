package com.caseystella.type;

import com.caseystella.parser.DateLexer;
import com.caseystella.parser.DateParser;
import com.caseystella.util.ConversionUtils;
import com.google.common.base.Optional;
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.StringUtils;


public class TypeInference {

  public static class IntegralHandler implements TypeHandler {

    @Override
    public Optional<ValueSummary> summarize(Object o) {
      String s = ConversionUtils.convert(o, String.class);
      if(s != null) {
        try {
          Long l = Long.parseLong(s.trim());
          return Optional.of(ValueSummary.of(l, Type.INTEGRAL, Modifier.VALID));
        }
        catch(Throwable t) {
          return Optional.absent();
        }
      }
      return Optional.absent();
    }

    @Override
    public String canonicalize(String base) {
      return base.replaceAll("\\d", "d");
    }
  }
  public static class FloatHandler implements TypeHandler {

    @Override
    public Optional<ValueSummary> summarize(Object o) {
      Double d = ConversionUtils.convert(o, Double.class);
      if(d == null) {
        return Optional.absent();
      }
      else {
        if(Double.isNaN(d)) {
          return Optional.of(ValueSummary.of(d, Type.FLOAT, Modifier.MISSING));
        }
        return Optional.of(ValueSummary.of(d, Type.FLOAT, Modifier.VALID));
      }
    }

    @Override
    public String canonicalize(String base) {
      return base.replaceAll("\\d", "d");
    }
  }
  public static class StringHandler implements TypeHandler {

    @Override
    public Optional<ValueSummary> summarize(Object o) {
      String s = ConversionUtils.convert(o, String.class);
      if(StringUtils.isEmpty(s)) {
        return Optional.of(ValueSummary.of(s, Type.STRING, Modifier.MISSING));
      }
      if(o instanceof String) {
        return Optional.of(ValueSummary.of(s, Type.STRING, Modifier.VALID));
      }
      return Optional.absent();
    }

    @Override
    public String canonicalize(String o) {
      return ("" + o).trim().toLowerCase().replaceAll("\\d", "d");
    }
  }

  public static class DateHandler implements TypeHandler {

    @Override
    public Optional<ValueSummary> summarize(Object s) {
      String opaqueValue = ConversionUtils.convert(s, String.class);
      if(StringUtils.isEmpty(opaqueValue)) {
        return Optional.absent();
      }
      final Integer[] failed = new Integer[]{0};
      String normalized = opaqueValue.toLowerCase();
      DateLexer lexer = new DateLexer(new ANTLRInputStream(normalized));
      DateParser parser = new DateParser(new CommonTokenStream(lexer));
      parser.removeErrorListeners();
      parser.addErrorListener(new BaseErrorListener(){
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine,
                                String msg, RecognitionException e)
        {
          failed[0] = 1;
        }
      });
      DateParser.DateContext context = parser.date();
      boolean isDate = failed[0] == 0;
      if(isDate) {
        return Optional.of(ValueSummary.of(opaqueValue, Type.DATE, Modifier.VALID));
      }
      return Optional.absent();
    }

    @Override
    public String canonicalize(String o) {
      return o.replaceAll("\\d", "d");
    }
  }

  public enum Modifier {
    VALID, MISSING
  }


  public enum Type {
    DATE(new DateHandler(), false),
    INTEGRAL(new IntegralHandler(), true),
    FLOAT(new FloatHandler(), true),
    STRING(new StringHandler(), false),
    UNKNOWN(new TypeHandler() {
      @Override
      public Optional<ValueSummary> summarize(Object o) {
        return Optional.of(ValueSummary.of(o, Type.UNKNOWN, Modifier.VALID));
      }

      @Override
      public String canonicalize(String o) {
        return o;
      }
    }, false)
    ;
    TypeHandler handler;
    boolean isNumeric;
    Type(TypeHandler handler, boolean isNumeric) {
      this.handler = handler;
      this.isNumeric = isNumeric;
    }

    public TypeHandler getHandler() {
      return handler;
    }

    public boolean isNumeric() {
      return isNumeric;
    }

    public static ValueSummary infer(Object o) {
      if(o == null) {
        return ValueSummary.of(o, UNKNOWN, Modifier.MISSING);
      }
      for(Type t : values()) {
        Optional<ValueSummary> val = t.handler.summarize(o);
        if(val.isPresent()) {
          return val.get();
        }
      }
      return ValueSummary.of(o, UNKNOWN, Modifier.VALID);
    }
  }

}
