package com.lguplus.iptv.testconfigexchanger;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ValueController {

    private final ConfigValues configValues;

    @GetMapping(value = "/values")
    public String values(@RequestParam("key") String key) {
        return configValues.getValues().getOrDefault(key, "NOT_FOUND");
    }

}
