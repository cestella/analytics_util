package com.caseystella.type;

import com.caseystella.parser.DateLexer;
import com.caseystella.parser.DateParser;
import org.antlr.v4.runtime.*;
import org.apache.pig.data.DataType;

/**
 * Created by cstella on 8/13/15.
 */
public class DateType implements ITypeHandler {

    @Override
    public ValueSummary summarize(String name, Object value) {
        ValueSummary ret = new ValueSummary();
        ret.baseType = Type.DATE;
        if(value == null)
        {
            ret.inferredType = Type.MISSING;
            ret.canonicalValue = null;
            ret.value = null;
        }
        else
        {
            ret.inferredType = Type.DATE;
            ret.canonicalValue = canonicalize(value.toString());
            ret.value = value.toString();
        }
        return ret;
    }

    @Override
    public boolean isLikely(String opaqueValue) {
        if(opaqueValue == null || opaqueValue.trim() == "") return false;
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

        return failed[0] == 0;
    }

    @Override
    public String canonicalize(String base) {
        String ret = base.replaceAll("\\d", "d");
        return ret;
    }

    @Override
    public byte[] supportedPigTypes() {
        return new byte[] {DataType.DATETIME};
    }
}
