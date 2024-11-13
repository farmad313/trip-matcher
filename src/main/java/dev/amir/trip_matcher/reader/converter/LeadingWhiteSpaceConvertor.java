package dev.amir.trip_matcher.reader.converter;

import com.opencsv.bean.AbstractBeanField;

public class LeadingWhiteSpaceConvertor extends AbstractBeanField<String, String> {

    @Override
    protected String convert(String value) {
        return value.trim();
    }
}

