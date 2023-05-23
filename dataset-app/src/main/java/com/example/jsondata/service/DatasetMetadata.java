package com.example.jsondata.service;


import lombok.Value;

import java.util.List;

@Value
public class DatasetMetadata {
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'";

    List<String> stringFields;
    List<String> doubleFields;
    List<String> intFields;
    List<String> booleanFields;

}
