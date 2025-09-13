package com.carbon.system.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.carbon.domain.common.ApiCode;
import com.carbon.domain.common.ApiResult;
import com.carbon.system.entity.ClassifyUtil;
import com.carbon.system.entity.TreePredictUtil;
import com.opencsv.CSVReader;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/python")
public class PythonController {
    @Value("${app.module_path}")
    private String modulePath;

    private ApiResult<Map<String, BigDecimal>> selectTrees(String targetColumn) {
        targetColumn = targetColumn.trim();
        Map<String, BigDecimal> treeEmissionMap = new HashMap<>();

        File treeFile = new File(modulePath + "/plant/trees.csv");

        try (Reader reader = new FileReader(treeFile);
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> allRows = csvReader.readAll();
            if (allRows.isEmpty()) {
                return ApiResult.ok(treeEmissionMap);
            }

            String[] headers = allRows.get(0);
            int targetIndex = -1;
            int emissionIndex = -1;
            for (int i = 0; i < headers.length; i++) {
                String header = headers[i].trim();
                if (Objects.equals(targetColumn, header)) {
                    targetIndex = i;
                } else if (Objects.equals("emissionReductionTCO2e", header)) {
                    emissionIndex = i;
                }
            }

            if (targetIndex == -1 || emissionIndex == -1) {
                String errMsg = "关键列缺失: " + targetColumn + ", emissionReductionTCO2e";
                return ApiResult.fail(errMsg);
            }

            for (int i = 1; i < allRows.size(); i++) {
                String[] rowData = allRows.get(i);

                if (rowData == null || rowData.length <= Math.max(targetIndex, emissionIndex)) {
                    log.error("第{}行数据不完整：，跳过统计", i + 1);
                    continue;
                }

                String treeType = rowData[targetIndex].trim();
                String emissionStr = rowData[emissionIndex].trim().replace(",", "");
                BigDecimal emissionValue;
                try {
                    emissionValue = new BigDecimal(emissionStr);
                } catch (NumberFormatException e) {
                    log.error("第{}行减排量格式错误值：{}，跳过统计", i + 1, emissionStr);
                    continue;
                }
                treeEmissionMap.put(
                        treeType,
                        treeEmissionMap.getOrDefault(treeType, BigDecimal.ZERO).add(emissionValue)
                );
            }
            return ApiResult.ok(treeEmissionMap);

        } catch (Exception e) {
            String errorMsg = "统计树种减排量失败：" + e.getMessage();
            log.error(errorMsg, e);
            return ApiResult.fail(errorMsg);
        }
    }

    private static Integer execPython(String file, Object... params) {
        int exitValue = -1;
        try {
            List<String> cmdList = new ArrayList<>();
            cmdList.add("python");
            cmdList.add(file);
            for (Object param : params) {
                cmdList.add(param.toString());
            }
            String[] cmd = cmdList.toArray(new String[0]);
            Process process = Runtime.getRuntime().exec(cmd);
            exitValue = process.waitFor();
            if (exitValue != 0) {
                InputStream errorStream = process.getErrorStream();
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream, StandardCharsets.UTF_8));
                StringBuilder errorMsg = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorMsg.append(line).append("\n");
                }
                log.error("Python脚本执行失败，退出值：{}，错误信息：{}", exitValue, errorMsg);
                throw new Exception(errorMsg.toString());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return exitValue;
    }

    @PostMapping("/classifyProjectType")
    @ApiOperation(value = "预测项目类型",notes = "预测项目类型")
    public ApiResult<ClassifyUtil> classifyProjectType(@RequestBody ClassifyUtil util) {
        if (execPython(
                modulePath + "/methodology/classify.py",
                util.getName(), util.getIntroduce()) == 0) {
            JSONObject jsonObject = JSONUtil.readJSONObject(
                    new File(modulePath + "/methodology/carbon_project_prediction.json"),
                    StandardCharsets.UTF_8
            );
            Map<String, String> top = (Map<String, String>) jsonObject.getJSONArray("top_predictions").get(0);
            String type = top.get("methodology");
            String confidence = top.get("confidence");
            ClassifyUtil res = new ClassifyUtil();
            res.setConfidence(Double.parseDouble(confidence.replace("%", "")));
            res.setType(type);
            res.setName(util.getName());
            res.setIntroduce(util.getIntroduce());
            return ApiResult.result(ApiCode.SUCCESS, res);
        } else {
            return ApiResult.fail("执行失败");
        }
    }

    @PostMapping("/predictTree")
    public ApiResult<BigDecimal> predictTree(@RequestBody TreePredictUtil util) {
        if (execPython(
                modulePath + "/plant/predict.py",
                util.getTreeType(), util.getPlantingAreaAcre(), util.getPlantingMonths(),
                util.getSoilFertility(), util.getClimateType()) == 0) {
            JSONObject jsonObject = JSONUtil.readJSONObject(
                    new File(modulePath + "/plant/prediction.json"),
                    StandardCharsets.UTF_8
            );
            BigDecimal emission = (BigDecimal) jsonObject.getJSONObject("prediction_result").get("emissionReductionTCO2e");
            return ApiResult.result(ApiCode.SUCCESS, emission);
        } else {
            return ApiResult.fail("执行失败");
        }
    }

    @GetMapping("/blockTrading")
    public ApiResult<List<List<Object>>> blockTrading() {
        List<List<Object>> carbonDataList = new ArrayList<>();
        String target = modulePath + "/crawer/crawer.py";
        if (execPython(target) == 0) {
            File resource = new File(modulePath + "/crawer/blockTrading.csv");
            try (Reader reader = new FileReader(resource);
                 CSVReader csvReader = new CSVReader(reader)) {

                List<String[]> rows = csvReader.readAll();
                if (rows.isEmpty()) {
                    return ApiResult.ok(carbonDataList);
                }
                String[] headers = rows.get(0);
                carbonDataList.add(new ArrayList<>(Arrays.asList(headers)));

                for (int i = 1; i < rows.size(); i++) {
                    String[] rowStr = rows.get(i);
                    List<Object> carbonData = new ArrayList<>();

                    // 日期,成交量,成交额,均价,折溢价
                    LocalDate date = LocalDate.parse(rowStr[0], DateTimeFormatter.ISO_DATE);
                    Long volume = Long.parseLong(rowStr[1].replace(",", ""));
                    BigDecimal turnover = new BigDecimal(rowStr[2].replace(",", ""));
                    BigDecimal averagePrice = new BigDecimal(rowStr[3]);
                    String premiumRate = rowStr[4];

                    carbonData.add(date.toString());
                    carbonData.add(volume);
                    carbonData.add(turnover);
                    carbonData.add(averagePrice);
                    carbonData.add(premiumRate);

                    carbonDataList.add(carbonData);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return ApiResult.fail(e.getMessage());
            }
        } else {
            return ApiResult.fail("python脚本执行失败");
        }
        return ApiResult.ok(carbonDataList);
    }

    @GetMapping("/marketConditions")
    public ApiResult<List<List<Object>>> marketConditions() {
        List<List<Object>> carbonDataList = new ArrayList<>();
        String target = modulePath + "/crawer/crawer.py";
        if (execPython(target) == 0) {
            File resource = new File(modulePath + "/crawer/marketConditions.csv");
            try (Reader reader = new FileReader(resource);
                 CSVReader csvReader = new CSVReader(reader)) {

                List<String[]> rows = csvReader.readAll();
                if (rows.isEmpty()) {
                    return ApiResult.ok(carbonDataList);
                }
                String[] headers = rows.get(0);
                carbonDataList.add(new ArrayList<>(Arrays.asList(headers)));

                for (int i = 1; i < rows.size(); i++) {
                    String[] rowStr = rows.get(i);
                    List<Object> carbonData = new ArrayList<>();

                    // 日期,开盘,收盘,最高,最低,涨跌,成交量,成交额,振幅
                    LocalDate date = LocalDate.parse(rowStr[0], DateTimeFormatter.ISO_DATE);
                    BigDecimal openPrice = new BigDecimal(rowStr[1]);
                    BigDecimal closePrice = new BigDecimal(rowStr[2]);
                    BigDecimal highPrice = new BigDecimal(rowStr[3]);
                    BigDecimal lowPrice = new BigDecimal(rowStr[4]);
                    String priceChange = rowStr[5];
                    Long volume = Long.parseLong(rowStr[6].replace(",", ""));
                    BigDecimal turnover = new BigDecimal(rowStr[7].replace(",", ""));
                    String amplitude = rowStr[8];

                    carbonData.add(date.toString());
                    carbonData.add(openPrice);
                    carbonData.add(closePrice);
                    carbonData.add(highPrice);
                    carbonData.add(lowPrice);
                    carbonData.add(priceChange);
                    carbonData.add(volume);
                    carbonData.add(turnover);
                    carbonData.add(amplitude);

                    carbonDataList.add(carbonData);
                }
            } catch (Exception e) {
                log.error("解析marketConditions.csv失败: {}", e.getMessage());
                return ApiResult.fail("数据解析失败: " + e.getMessage());
            }
        } else {
            return ApiResult.fail("python脚本执行失败");
        }
        return ApiResult.ok(carbonDataList);
    }

    @PostMapping("/predictPrice")
    public ApiResult<List<List<Object>>> predictPrice() {
        List<List<Object>> priceList = new ArrayList<>();
        if (execPython(modulePath + "/price/predict.py") == 0) {
            File resource = new File(modulePath + "/price/prediction.csv");
            try (Reader reader = new FileReader(resource);
                 CSVReader csvReader = new CSVReader(reader)) {

                List<String[]> rows = csvReader.readAll();
                if (rows.isEmpty()) {
                    return ApiResult.ok(priceList);
                }
                String[] headers = rows.get(0);
                priceList.add(new ArrayList<>(Arrays.asList(headers)));

                for (int i = 1; i < rows.size(); i++) {
                    String[] rowStr = rows.get(i);
                    List<Object> priceData = new ArrayList<>();

                    // 日期,预测最低价,预测最高价,预测中位价,价格区间宽度,相对变化率
//                    LocalDate date = LocalDate.parse(rowStr[0], DateTimeFormatter.ISO_DATE);
                    LocalDate date = LocalDate.now().plusDays(i);
                    Double lowPrice = new Double(rowStr[1].replace(",", ""));
                    Double highPrice = new Double(rowStr[2].replace(",", ""));
                    Double mediumPrice = new Double(rowStr[3].replace(",", ""));
                    Double priceRange = new Double(rowStr[4].replace(",", ""));
                    Double changeRate = new Double(rowStr[5]);

                    priceData.add(date.toString());
                    priceData.add(lowPrice);
                    priceData.add(highPrice);
                    priceData.add(mediumPrice);
                    priceData.add(priceRange);
                    priceData.add(changeRate);

                    priceList.add(priceData);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                return ApiResult.fail(e.getMessage());
            }
        } else {
            return ApiResult.fail("执行失败");
        }
        return ApiResult.ok(priceList);
    }

    @GetMapping("/barTreeWithType")
    public ApiResult<Map<String, BigDecimal>> barTreeWithType() {
        return selectTrees("treeType");
    }

    @GetMapping("/barTreeWithSoil")
    public ApiResult<Map<String, BigDecimal>> barTreeWithSoil() {
        return selectTrees("soilFertility");
    }

    @GetMapping("/barTreeWithClimate")
    public ApiResult<Map<String, BigDecimal>> barTreeWithClimate() {
        return selectTrees("climateType");
    }
}
