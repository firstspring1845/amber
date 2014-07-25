package net.firsp.amber.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class FuncUtil {

    public static <A,B> ArrayList<B> map(Collection <A> data, MapFunction<A,B> func){
        ArrayList<B> list = new ArrayList<B>();
        for (A a : data) {
            list.add(func.apply(a));
        }
        return list;
    }

    public interface MapFunction<A,B>{
        public B apply(A data);
    }

    public static <A> ArrayList<A> filter(Collection<A> data, FilterFunction<A> func){
        ArrayList<A> list = new ArrayList<A>();
        for (A a : data) {
            if(func.apply(a)){
                list.add(a);
            }
        }
        return list;
    }

    public interface FilterFunction<A>{
        public boolean apply(A data);
    }

}
