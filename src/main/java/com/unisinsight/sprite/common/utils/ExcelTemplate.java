package com.unisinsight.sprite.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * 无样式Excel导出/解析
 *
 * @author zhanghengyuan
 * @date 2018/12/20 15:26
 * @since 1.0
 */
@Slf4j
public final class ExcelTemplate {


    /**
     * 文件类型
     */
    private static final String EXCEL_VERSION = ".xls";

    /**
     * 构造方法
     */
    private ExcelTemplate() {
    }

    /**
     * 创建模板
     *
     * @param fileName Excel文件名
     * @param handers  Excel列标题(数组)
     * @param downData 下拉框数据(数组)
     * @param downRows 下拉列的序号(数组,序号从0开始)
     * @param response response
     * @throws
     * @Title: createTemplate
     * @Description: 生成Excel导入模板
     */
    public static void createTemplate(String fileName, String[] handers,
                                      List<String[]> downData, String[] downRows, HttpServletResponse response) {

        //创建工作薄
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            //表头样式
            HSSFCellStyle style = wb.createCellStyle();
            HSSFCellStyle columnStyle = wb.createCellStyle();
            //水平居中
            style.setAlignment(HorizontalAlignment.CENTER);
            //垂直居中
            style.setVerticalAlignment(VerticalAlignment.CENTER);
            //字体样式
            HSSFFont fontStyle = wb.createFont();
            fontStyle.setFontName("微软雅黑");
            fontStyle.setFontHeightInPoints((short) 12);
            //设置是否加粗
            fontStyle.setBold(true);
            style.setFont(fontStyle);
            //新建sheet
            HSSFSheet sheet1 = wb.createSheet("Sheet1");
            HSSFSheet sheet2 = wb.createSheet("Sheet2");

            DataFormat format = wb.createDataFormat();
            columnStyle.setDataFormat(format.getFormat("@"));

            //生成sheet1内容
            //第一个sheet的第一行为标题
            HSSFRow rowFirst = sheet1.createRow(0);
            //写标题
            for (int i = 0; i < handers.length; i++) {
                //获取第一行的每个单元格
                HSSFCell cell = rowFirst.createCell(i);
                //设置每列的列宽
                sheet1.setColumnWidth(i, 8000);
                //加样式
                cell.setCellStyle(style);
                //往单元格里写数据
                cell.setCellValue(handers[i]);
                cell.setCellType(CellType.STRING);
                sheet1.setDefaultColumnStyle(i, columnStyle);
            }

            //设置下拉框数据
            String[] arr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L",
                    "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
            int index = 0;
            HSSFRow row;
            if (null != downRows) {
                for (int r = 0; r < downRows.length; r++) {
                    //获取下拉对象
                    String[] dlData = downData.get(r);
                    int rowNum = Integer.parseInt(downRows[r]);

                    int length = (dlData == null ? 0 : dlData.length);

                    //255以上的下拉，即下拉列表元素很多的情况
                    //1、设置有效性
                    //Sheet2第A1到A5000作为下拉列表来源数据
                    //Sheet2第A1到A5000作为下拉列表来源数据
                    String strFormula = "Sheet2!$" + arr[index] + "$1:$" + arr[index] + "$" + length;
                    //设置每列的列宽
                    sheet2.setColumnWidth(r, 4000);
                    //设置数据有效性加载在哪个单元格上,参数分别是：从sheet2获取A1到A5000作为一个下拉的数据、起始行、终止行、起始列、终止列
                    //下拉列表元素很多的情况
                    sheet1.addValidationData(setDataValidation(strFormula, 1, length, rowNum, rowNum));

                    //2、生成sheet2内容
                    for (int j = 0; j < length; j++) {
                        //第1个下拉选项，直接创建行、列
                        if (index == 0) {
                            //创建数据行
                            row = sheet2.createRow(j);
                            //设置每列的列宽
                            sheet2.setColumnWidth(j, 4000);
                            //设置对应单元格的值
                            row.createCell(0).setCellValue(dlData[j]);

                        } else { //非第1个下拉选项
                            int rowCount = sheet2.getLastRowNum();
                            //前面创建过的行，直接获取行，创建列
                            if (j <= rowCount) {
                                //获取行，创建列
                                //设置对应单元格的值
                                sheet2.getRow(j).createCell(index).setCellValue(dlData[j]);

                            } else {
                                //未创建过的行，直接创建行、创建列
                                //设置每列的列宽
                                sheet2.setColumnWidth(j, 4000);
                                //创建行、创建列
                                //设置对应单元格的值
                                sheet2.createRow(j).createCell(index).setCellValue(dlData[j]);
                            }
                        }
                    }
                    index++;
                }
            }
            try (OutputStream output = response.getOutputStream()) {
                fileName = fileName + EXCEL_VERSION;
                response.setContentType("APPLICATION/OCTET-STREAM");
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-disposition", "attachment;filename=" + fileName);
                wb.write(output);
                output.flush();
            } catch (IOException e) {
                log.error("批量导入信息异常：", e);

            }
        } catch (Exception e) {
            log.error("异常", e);
        }
    }

    /**
     * 下拉列表元素很多的情况
     *
     * @param strFormula st
     * @param firstRow   起始行
     * @param endRow     终止行
     * @param firstCol   起始列
     * @param endCol     终止列
     * @return HSSFDataValidation
     * @Title: setDataValidation
     * @Description: 下拉列表元素很多的情况 (255以上的下拉)
     */
    private static HSSFDataValidation setDataValidation(String strFormula,
                                                        int firstRow, int endRow, int firstCol, int endCol) {

        // 设置数据有效性加载在哪个单元格上。四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        DVConstraint constraint = DVConstraint.createFormulaListConstraint(strFormula);
        HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);

        dataValidation.createErrorBox("Error", "Error");
        dataValidation.createPromptBox("", null);

        return dataValidation;
    }


    /**
     * 下拉列表元素不多的情况
     *
     * @param sheet    表格
     * @param textList 数据list
     * @param firstRow 起始行
     * @param endRow   结束行
     * @param firstCol 起始列
     * @param endCol   结束列
     * @return DataValidation
     * @Title: setDataValidationInfo
     * @Description: 下拉列表元素不多的情况(255以内的下拉)
     */
    private static DataValidation setDataValidationInfo(Sheet sheet, String[] textList,
                                                        int firstRow, int endRow, int firstCol, int endCol) {

        DataValidationHelper helper = sheet.getDataValidationHelper();
        //加载下拉列表内容
        DataValidationConstraint constraint = helper.createExplicitListConstraint(textList);
        constraint.setExplicitListValues(textList);
        //设置数据有效性加载在哪个单元格上。四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList((short) firstRow,
                (short) endRow, (short) firstCol, (short) endCol);
        //数据有效性对象
        return helper.createValidation(constraint, regions);

    }


}
