package com.carbon.system.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TreePredictUtil {
    private String treeType;

    private Double plantingAreaAcre;

    private Double plantingMonths;

    private String soilFertility;

    private String climateType;
}
