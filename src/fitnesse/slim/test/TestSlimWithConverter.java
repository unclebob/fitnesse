package fitnesse.slim.test;

import fitnesse.slim.Converter;
import fitnesse.slim.converters.ConverterRegistry;
import fitnesse.slim.converters.DefaultConverter;
import fitnesse.slim.converters.GenericCollectionConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSlimWithConverter {
    private Converter<List> standardConverter = null;

    public void setListConverter() {
        standardConverter = ConverterRegistry.getConverterForClass(List.class);
        ConverterRegistry.addConverter(List.class, new ListConverter());
    }

    public void removeListConverter() {
        ConverterRegistry.addConverter(List.class, standardConverter);
    }

    public Object getObject() {
        return getArrayList();
    }

    public List<String> getList() {
        return getArrayList();
    }

    public ArrayList<String> getArrayList() {
        return new ArrayList<>(Arrays.asList("a", "b", "c"));
    }

    public boolean sameList(List otherList) {
        return getList().equals(otherList);
    }

    private class ListConverter implements Converter<List> {
        private GenericCollectionConverter<Object, List<Object>> converter;
        public ListConverter() {
            converter = new GenericCollectionConverter<>(List.class, new DefaultConverter());
        }

        @Override
        public String toString(List o) {
            String s = converter.toString(o);
            return s.replace("[", "{").replace("]", "}");
        }

        @Override
        public List fromString(String arg) {
            String replace = arg.replace("{", "[").replace("}", "]");
            return converter.fromString(replace);
        }
    }
}
