package com.caseystella.util;


import org.apache.commons.beanutils.BeanUtilsBean2;
import org.apache.commons.beanutils.ConvertUtilsBean;

public class ConversionUtils {
  private static ThreadLocal<ConvertUtilsBean> UTILS_BEAN  = new ThreadLocal<ConvertUtilsBean>() {
    @Override
    protected ConvertUtilsBean initialValue() {
      ConvertUtilsBean ret = BeanUtilsBean2.getInstance().getConvertUtils();
      ret.deregister();
      ret.register(false,true, 1);
      return ret;
    }
  };
  public static <T> T convert(Object o, Class<T> clazz) {
    if(o == null) {
      return null;
    }
    return clazz.cast(UTILS_BEAN.get().convert(o, clazz));
  }
}
