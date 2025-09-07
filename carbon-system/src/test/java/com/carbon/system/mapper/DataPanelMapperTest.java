package com.carbon.system.mapper;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class DataPanelMapperTest{
    @Autowired
    private DataPanelMapper dataPanelMapper;

    @Test
    public void getDataPanelData() {
        DateTime date = DateUtil.date();
        if(dataPanelMapper!=null)
        {
            Double performanceTotal = dataPanelMapper.getPerformancePrice(date);
            System.out.println(performanceTotal);
        }

    }

}