package com.carbon.system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassifyUtil {
    private String name;

    private String introduce;

    private Double confidence;

    private String type;
}
