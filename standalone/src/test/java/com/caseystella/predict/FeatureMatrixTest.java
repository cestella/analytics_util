package com.caseystella.predict;

import com.caseystella.predict.feature.FeatureMatrix;
import com.caseystella.predict.udf.ToArff;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.adrianwalker.multilinestring.Multiline;
import org.apache.pig.data.*;
import org.junit.Assert;
import org.junit.Test;
import weka.core.Instances;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cstella on 8/16/15.
 */
public class FeatureMatrixTest {
    /**
     {
        "features" : [
            {
                "type" : "FLOAT"
               ,"column" : "sepal_length"
            }
           ,{
                "type" : "FLOAT"
               ,"column" : "sepal_width"
            }
           ,{
                "type" : "FLOAT"
               ,"column" : "petal_length"
            }
           ,{
                "type" : "FLOAT"
               ,"column" : "petal_width"
            }
           ,{
                "type" : "CATEGORICAL"
               ,"column" : "iris_class"
               , "config" : {
                        "categories" : ["Iris-setosa", "Iris-versicolor", "Iris-virginica"]
                            }
            }
                     ]
     }
     */
    @Multiline
    public static String featureConfig;

    public static Map<String, Integer> columnToPosition = new HashMap<String, Integer>(){{
            int pos = 0;
            put("sepal_length", pos++);
            put("sepal_width", pos++);
            put("petal_length", pos++);
            put("petal_width", pos++);
            put("iris_class", pos++);
        }};

    /**
5.1,3.5,1.4,0.2,Iris-setosa
4.9,3.0,1.4,0.2,Iris-setosa
4.7,3.2,1.3,0.2,Iris-setosa
4.6,3.1,1.5,0.2,Iris-setosa
5.0,3.6,1.4,0.2,Iris-setosa
5.4,3.9,1.7,0.4,Iris-setosa
4.6,3.4,1.4,0.3,Iris-setosa
5.0,3.4,1.5,0.2,Iris-setosa
4.4,2.9,1.4,0.2,Iris-setosa
4.9,3.1,1.5,0.1,Iris-setosa
5.4,3.7,1.5,0.2,Iris-setosa
4.8,3.4,1.6,0.2,Iris-setosa
4.8,3.0,1.4,0.1,Iris-setosa
4.3,3.0,1.1,0.1,Iris-setosa
5.8,4.0,1.2,0.2,Iris-setosa
5.7,4.4,1.5,0.4,Iris-setosa
5.4,3.9,1.3,0.4,Iris-setosa
5.1,3.5,1.4,0.3,Iris-setosa
5.7,3.8,1.7,0.3,Iris-setosa
5.1,3.8,1.5,0.3,Iris-setosa
5.4,3.4,1.7,0.2,Iris-setosa
5.1,3.7,1.5,0.4,Iris-setosa
4.6,3.6,1.0,0.2,Iris-setosa
5.1,3.3,1.7,0.5,Iris-setosa
4.8,3.4,1.9,0.2,Iris-setosa
5.0,3.0,1.6,0.2,Iris-setosa
5.0,3.4,1.6,0.4,Iris-setosa
5.2,3.5,1.5,0.2,Iris-setosa
5.2,3.4,1.4,0.2,Iris-setosa
4.7,3.2,1.6,0.2,Iris-setosa
4.8,3.1,1.6,0.2,Iris-setosa
5.4,3.4,1.5,0.4,Iris-setosa
5.2,4.1,1.5,0.1,Iris-setosa
5.5,4.2,1.4,0.2,Iris-setosa
4.9,3.1,1.5,0.1,Iris-setosa
5.0,3.2,1.2,0.2,Iris-setosa
5.5,3.5,1.3,0.2,Iris-setosa
4.9,3.1,1.5,0.1,Iris-setosa
4.4,3.0,1.3,0.2,Iris-setosa
5.1,3.4,1.5,0.2,Iris-setosa
5.0,3.5,1.3,0.3,Iris-setosa
4.5,2.3,1.3,0.3,Iris-setosa
4.4,3.2,1.3,0.2,Iris-setosa
5.0,3.5,1.6,0.6,Iris-setosa
5.1,3.8,1.9,0.4,Iris-setosa
4.8,3.0,1.4,0.3,Iris-setosa
5.1,3.8,1.6,0.2,Iris-setosa
4.6,3.2,1.4,0.2,Iris-setosa
5.3,3.7,1.5,0.2,Iris-setosa
5.0,3.3,1.4,0.2,Iris-setosa
7.0,3.2,4.7,1.4,Iris-versicolor
6.4,3.2,4.5,1.5,Iris-versicolor
6.9,3.1,4.9,1.5,Iris-versicolor
5.5,2.3,4.0,1.3,Iris-versicolor
6.5,2.8,4.6,1.5,Iris-versicolor
5.7,2.8,4.5,1.3,Iris-versicolor
6.3,3.3,4.7,1.6,Iris-versicolor
4.9,2.4,3.3,1.0,Iris-versicolor
6.6,2.9,4.6,1.3,Iris-versicolor
5.2,2.7,3.9,1.4,Iris-versicolor
5.0,2.0,3.5,1.0,Iris-versicolor
5.9,3.0,4.2,1.5,Iris-versicolor
6.0,2.2,4.0,1.0,Iris-versicolor
6.1,2.9,4.7,1.4,Iris-versicolor
5.6,2.9,3.6,1.3,Iris-versicolor
6.7,3.1,4.4,1.4,Iris-versicolor
5.6,3.0,4.5,1.5,Iris-versicolor
5.8,2.7,4.1,1.0,Iris-versicolor
6.2,2.2,4.5,1.5,Iris-versicolor
5.6,2.5,3.9,1.1,Iris-versicolor
5.9,3.2,4.8,1.8,Iris-versicolor
6.1,2.8,4.0,1.3,Iris-versicolor
6.3,2.5,4.9,1.5,Iris-versicolor
6.1,2.8,4.7,1.2,Iris-versicolor
6.4,2.9,4.3,1.3,Iris-versicolor
6.6,3.0,4.4,1.4,Iris-versicolor
6.8,2.8,4.8,1.4,Iris-versicolor
6.7,3.0,5.0,1.7,Iris-versicolor
6.0,2.9,4.5,1.5,Iris-versicolor
5.7,2.6,3.5,1.0,Iris-versicolor
5.5,2.4,3.8,1.1,Iris-versicolor
5.5,2.4,3.7,1.0,Iris-versicolor
5.8,2.7,3.9,1.2,Iris-versicolor
6.0,2.7,5.1,1.6,Iris-versicolor
5.4,3.0,4.5,1.5,Iris-versicolor
6.0,3.4,4.5,1.6,Iris-versicolor
6.7,3.1,4.7,1.5,Iris-versicolor
6.3,2.3,4.4,1.3,Iris-versicolor
5.6,3.0,4.1,1.3,Iris-versicolor
5.5,2.5,4.0,1.3,Iris-versicolor
5.5,2.6,4.4,1.2,Iris-versicolor
6.1,3.0,4.6,1.4,Iris-versicolor
5.8,2.6,4.0,1.2,Iris-versicolor
5.0,2.3,3.3,1.0,Iris-versicolor
5.6,2.7,4.2,1.3,Iris-versicolor
5.7,3.0,4.2,1.2,Iris-versicolor
5.7,2.9,4.2,1.3,Iris-versicolor
6.2,2.9,4.3,1.3,Iris-versicolor
5.1,2.5,3.0,1.1,Iris-versicolor
5.7,2.8,4.1,1.3,Iris-versicolor
6.3,3.3,6.0,2.5,Iris-virginica
5.8,2.7,5.1,1.9,Iris-virginica
7.1,3.0,5.9,2.1,Iris-virginica
6.3,2.9,5.6,1.8,Iris-virginica
6.5,3.0,5.8,2.2,Iris-virginica
7.6,3.0,6.6,2.1,Iris-virginica
4.9,2.5,4.5,1.7,Iris-virginica
7.3,2.9,6.3,1.8,Iris-virginica
6.7,2.5,5.8,1.8,Iris-virginica
7.2,3.6,6.1,2.5,Iris-virginica
6.5,3.2,5.1,2.0,Iris-virginica
6.4,2.7,5.3,1.9,Iris-virginica
6.8,3.0,5.5,2.1,Iris-virginica
5.7,2.5,5.0,2.0,Iris-virginica
5.8,2.8,5.1,2.4,Iris-virginica
6.4,3.2,5.3,2.3,Iris-virginica
6.5,3.0,5.5,1.8,Iris-virginica
7.7,3.8,6.7,2.2,Iris-virginica
7.7,2.6,6.9,2.3,Iris-virginica
6.0,2.2,5.0,1.5,Iris-virginica
6.9,3.2,5.7,2.3,Iris-virginica
5.6,2.8,4.9,2.0,Iris-virginica
7.7,2.8,6.7,2.0,Iris-virginica
6.3,2.7,4.9,1.8,Iris-virginica
6.7,3.3,5.7,2.1,Iris-virginica
7.2,3.2,6.0,1.8,Iris-virginica
6.2,2.8,4.8,1.8,Iris-virginica
6.1,3.0,4.9,1.8,Iris-virginica
6.4,2.8,5.6,2.1,Iris-virginica
7.2,3.0,5.8,1.6,Iris-virginica
7.4,2.8,6.1,1.9,Iris-virginica
7.9,3.8,6.4,2.0,Iris-virginica
6.4,2.8,5.6,2.2,Iris-virginica
6.3,2.8,5.1,1.5,Iris-virginica
6.1,2.6,5.6,1.4,Iris-virginica
7.7,3.0,6.1,2.3,Iris-virginica
6.3,3.4,5.6,2.4,Iris-virginica
6.4,3.1,5.5,1.8,Iris-virginica
6.0,3.0,4.8,1.8,Iris-virginica
6.9,3.1,5.4,2.1,Iris-virginica
6.7,3.1,5.6,2.4,Iris-virginica
6.9,3.1,5.1,2.3,Iris-virginica
5.8,2.7,5.1,1.9,Iris-virginica
6.8,3.2,5.9,2.3,Iris-virginica
6.7,3.3,5.7,2.5,Iris-virginica
6.7,3.0,5.2,2.3,Iris-virginica
6.3,2.5,5.0,1.9,Iris-virginica
6.5,3.0,5.2,2.0,Iris-virginica
6.2,3.4,5.4,2.3,Iris-virginica
5.9,3.0,5.1,1.8,Iris-virginica*/
    @Multiline
    public static String irisData;

    @Test
    public void testAttributeComputation() throws IOException {
        FeatureMatrix matrix = FeatureMatrix.create(featureConfig);
        Assert.assertNotNull(matrix);
        Assert.assertEquals(matrix.getFeatures().size(), 5);
        {
            Assert.assertEquals(matrix.getAttributes("iris_class").size(), 5);
            String attributes = "@attribute sepal_length numeric\n" +
                    "@attribute sepal_width numeric\n" +
                    "@attribute petal_length numeric\n" +
                    "@attribute petal_width numeric\n" +
                    "@attribute iris_class {Iris-setosa,Iris-versicolor,Iris-virginica}";
            Assert.assertEquals(attributes, Joiner.on('\n').join(matrix.getAttributes("iris_class")));
        }
        {
            Assert.assertEquals(matrix.getAttributes("sepal_width").size(), 8);
            String attributes = "@attribute sepal_length numeric\n" +
                    "@attribute petal_length numeric\n" +
                    "@attribute petal_width numeric\n" +
                    "@attribute iris_class" + Constants.ONE_HOT_ENCODING_SEPARATOR + "Iris-setosa numeric\n" +
                    "@attribute iris_class" + Constants.ONE_HOT_ENCODING_SEPARATOR + "Iris-versicolor numeric\n" +
                    "@attribute iris_class" + Constants.ONE_HOT_ENCODING_SEPARATOR + "Iris-virginica numeric\n" +
                    "@attribute iris_class" + Constants.ONE_HOT_ENCODING_SEPARATOR + "missing numeric\n" +
                    "@attribute sepal_width numeric";
            Assert.assertEquals(attributes, Joiner.on('\n').join(matrix.getAttributes("sepal_width")));
        }
    }

    @Test
    public void testArffConversion() throws Exception {
        FeatureMatrix matrix = FeatureMatrix.create(featureConfig);
        Tuple t = DefaultTupleFactory.getInstance().newTuple();
        {
            //5.1,3.5,1.4,0.2,Iris-setosa
            String firstLine = Splitter.on('\n').split(irisData).iterator().next();
            for(String token : Splitter.on(',').split(firstLine) )
            {
                t.append(token);
            }
        }
        ToArff toArff = new ToArff();
        {
            Instances instances = toArff.getInstances(Lists.<Tuple>newArrayList(t), matrix, "iris_class", columnToPosition);
            String arff_out ="@relation iris_class\n" +
                    "\n" +
                    "@attribute sepal_length numeric\n" +
                    "@attribute sepal_width numeric\n" +
                    "@attribute petal_length numeric\n" +
                    "@attribute petal_width numeric\n" +
                    "@attribute iris_class {Iris-setosa,Iris-versicolor,Iris-virginica}\n" +
                    "\n" +
                    "@data\n" +
                    "5.1,3.5,1.4,0.2,Iris-setosa";
            Assert.assertEquals(arff_out, instances.toString());
        }
        {
            Instances instances = toArff.getInstances(Lists.<Tuple>newArrayList(t), matrix, "sepal_width", columnToPosition);
            String arff_out = "@relation sepal_width\n" +
                    "\n" +
                    "@attribute sepal_length numeric\n" +
                    "@attribute petal_length numeric\n" +
                    "@attribute petal_width numeric\n" +
                    "@attribute iris_class->Iris-setosa numeric\n" +
                    "@attribute iris_class->Iris-versicolor numeric\n" +
                    "@attribute iris_class->Iris-virginica numeric\n" +
                    "@attribute iris_class->missing numeric\n"+
                    "@attribute sepal_width numeric\n" +
                    "\n" +
                    "@data\n" +
                    "5.1,1.4,0.2,1,0,0,0,3.5";
            Assert.assertEquals(arff_out, instances.toString());
        }
    }

    @Test
    public void testFeatureImportance() throws Exception
    {
        FeatureMatrix matrix = FeatureMatrix.create(featureConfig);
        List<Tuple> data = new ArrayList<Tuple>();
        for(String line : Splitter.on('\n').split(irisData))
        {
            Tuple t = DefaultTupleFactory.getInstance().newTuple();
            {
                //5.1,3.5,1.4,0.2,Iris-setosa
                for(String token : Splitter.on(',').split(line) )
                {
                    t.append(token);
                }
            }
            data.add(t);
        }
        ToArff toArff = new ToArff();
        {
            Instances instances = toArff.getInstances(data, matrix, "iris_class", columnToPosition);
            System.out.println(instances);
            System.out.println(new FeatureImportance().findImportance(instances, matrix.getAttributes("iris_class"), false));
        }
        {
            Instances instances = toArff.getInstances(data, matrix, "sepal_width", columnToPosition);
            System.out.println(new FeatureImportance().findImportance(instances, matrix.getAttributes("sepal_width"), true));
        }
    }
}
