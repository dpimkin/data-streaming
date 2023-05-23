package com.example.transform.domain;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import java.util.HashMap;
import java.util.Map;


@RegisterReflectionForBinding
public class PricingElem {
    private Map<String, Object> props = new HashMap<>();

    @JsonAnyGetter
    public Map<String, Object> props() {
        return props;
    }

    @JsonAnySetter
    public void props(String key, Object value) {
        props.put(key, value);
    }

}
