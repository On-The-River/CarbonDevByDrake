package com.carbon.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class VerifyResult implements Serializable {
    private Boolean available;
}
