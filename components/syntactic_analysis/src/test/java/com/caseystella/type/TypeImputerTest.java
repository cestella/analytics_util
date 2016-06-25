package com.caseystella.type;

import com.google.common.collect.ImmutableList;
import org.junit.Assert;
import org.junit.Test;

public class TypeImputerTest {
  @Test
  public void testIntegralTypes() {
    Assert.assertEquals(ValueSummary.of(7l, TypeInference.Type.INTEGRAL, TypeInference.Modifier.VALID)
            ,TypeInference.Type.infer(7)
    );
    Assert.assertEquals(ValueSummary.of(7l, TypeInference.Type.INTEGRAL, TypeInference.Modifier.VALID)
            ,TypeInference.Type.infer("7")
    );
    Assert.assertEquals(ValueSummary.of(7l, TypeInference.Type.INTEGRAL, TypeInference.Modifier.VALID)
            ,TypeInference.Type.infer("7 ")
    );
  }
  @Test
  public void testFloatTypes() {
    Assert.assertEquals(ValueSummary.of(7.2d, TypeInference.Type.FLOAT, TypeInference.Modifier.VALID)
            , TypeInference.Type.infer(7.2)
    );
    Assert.assertEquals(ValueSummary.of(7.2d, TypeInference.Type.FLOAT, TypeInference.Modifier.VALID)
            , TypeInference.Type.infer("7.2")
    );
    Assert.assertEquals(ValueSummary.of(Double.NaN, TypeInference.Type.FLOAT, TypeInference.Modifier.MISSING)
            , TypeInference.Type.infer("NaN")
    );
  }
  @Test
  public void testDateTypes() {

    {
      String date = "20120";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.INTEGRAL, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "2012";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.INTEGRAL, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "201.2";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.FLOAT, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "201.22";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.FLOAT, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "2010.2";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.FLOAT, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "2014.01.02";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.DATE, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "2014/01/02";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.DATE, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "2014-01-02";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.DATE, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "July 01, 1994";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.DATE, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "Jul 01, 1994";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.DATE, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
    {
      String date = "Jul-01-1994";
      Assert.assertEquals(ValueSummary.of(date, TypeInference.Type.DATE, TypeInference.Modifier.VALID)
              , TypeInference.Type.infer(date)
      );
    }
  }

  @Test
  public void testStringTypes() {
    Assert.assertEquals(ValueSummary.of("", TypeInference.Type.STRING, TypeInference.Modifier.MISSING)
            , TypeInference.Type.infer("")
    );
    Assert.assertEquals(ValueSummary.of("foo", TypeInference.Type.STRING, TypeInference.Modifier.VALID)
            , TypeInference.Type.infer("foo")
    );
    Assert.assertEquals(ValueSummary.of("2014-foo-bar", TypeInference.Type.STRING, TypeInference.Modifier.VALID)
            , TypeInference.Type.infer("2014-foo-bar")
    );
    Assert.assertEquals(ValueSummary.of("182 1994", TypeInference.Type.STRING, TypeInference.Modifier.VALID)
            , TypeInference.Type.infer("182 1994")
    );

  }

  @Test
  public void testInvalidTypes() {
    Assert.assertEquals(ValueSummary.of(null, TypeInference.Type.UNKNOWN, TypeInference.Modifier.MISSING)
            , TypeInference.Type.infer(null)
    );

    Assert.assertEquals(ValueSummary.of(ImmutableList.of("foo", "bar"), TypeInference.Type.UNKNOWN, TypeInference.Modifier.VALID)
            , TypeInference.Type.infer(ImmutableList.of("foo", "bar"))
    );
  }
}
