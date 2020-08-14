package com.unisinsight.sprite.common.utils;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.BaseRowModel;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.unisinsight.sprite.common.constant.ExcelConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description
 *
 * @author zhangyuhe [zhang.yuhe@unisinsight.com]
 * @date 2020/02/25 21:10
 * @since 1.0
 */

@Slf4j
public final class ExcelUtil {

    //单个sheet导出最大数据量
    private static final int SHEET_EXPORT_MAX = 50000;

    /**
     * 构造方法私有化
     */
    private ExcelUtil() {
    }


    /**
     * 导出 Excel ：一个 sheet，带表头.
     *
     * @param response  HttpServletResponse
     * @param list      数据 list，每个元素为一个 BaseRowModel
     * @param fileName  导出的文件名
     * @param sheetName 导入文件的 sheet 名
     * @param model     映射实体类，Excel 模型
     * @throws Exception 异常
     */
    public static void write03Excel(
            HttpServletResponse response, List<? extends BaseRowModel> list,
            String fileName, String sheetName, BaseRowModel model) throws Exception {
        ExcelWriter writer =
                new ExcelWriter(getOutputStream(fileName, response), ExcelTypeEnum.XLS);
        Sheet sheet = new Sheet(1, 0, model.getClass());
        sheet.setSheetName(sheetName);
        writer.write(list, sheet);
        writer.finish();
    }

    /**
     * 导出文件时为Writer生成OutputStream.
     *
     * @param fileName 文件名
     * @param response response
     * @return ""
     */
    private static OutputStream getOutputStream(String fileName,
                                                HttpServletResponse response) throws Exception {
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xls");
            response.setHeader("Pragma", "public");
            response.setHeader("Cache-Control", "no-store");
            response.addHeader("Cache-Control", "max-age=0");
            return response.getOutputStream();
        } catch (IOException e) {
            throw new Exception("导出excel表格失败!", e);
        }
    }

    /**
     * 读取文件中的数据
     * 会判断excel版本
     *
     * @param file 文件
     * @param c    泛型入参
     * @param <T>  泛型
     * @return list 返回结果
     * @throws Exception 异常信息
     */
    public static <T> List<T> readExcel(MultipartFile file, Class<T> c) throws Exception {
        log.info("文件读取{}", file.getName());
        boolean isExcel2007 = false;
        InputStream inputStream = file.getInputStream();
        try {
            new HSSFWorkbook(inputStream);
            isExcel2007 = false;
        } catch (OfficeXmlFileException e) {
            isExcel2007 = true;
        } catch (Exception e) {
            throw e;
        }
        inputStream = file.getInputStream();
        if (!isExcel2007) {
            return readExcel2003(inputStream, c);
        } else {
            return readExcel2007(inputStream, c);
        }
    }


    /**
     * 读取Excel2003文件中的数据，返回list
     *
     * @param inputStream 输入流
     * @param c           泛型入参
     * @param <T>         泛型
     * @return list 返回结果
     * @throws Exception 异常
     */
    public static <T> List<T> readExcel2003(InputStream inputStream, Class<T> c) throws Exception {

        Workbook workbook = new HSSFWorkbook(inputStream); //创建工作蒲
        HSSFSheet sheet = (HSSFSheet) workbook.getSheetAt(0); //创建工作表
        HSSFRow row = null;  //对应行
        HSSFCell cell = null; //对应单元格
        row = sheet.getRow(0);

        int totalRow = sheet.getLastRowNum(); //得到sheet表总行数
        int totalCell = row.getLastCellNum(); //列数
        log.info("总行数{}, 总列数{}", totalRow, totalCell);

        T t = c.newInstance();
        Field[] fields = t.getClass().getDeclaredFields();

        List<T> list = new ArrayList<>();
        for (int i = 1; i <= totalRow; i++) {
            row = sheet.getRow(i);

            if (isRowEmpty(row)) {
                continue;
            }

            log.debug("正在处理第【{}】行", i);
            t = c.newInstance();

            for (int j = 1; j <= totalCell; j++) {

                cell = row.getCell(j - 1);

                if (cell != null) {

                    Field field = fields[j - 1];
                    String fieldName = field.getName();
                    String fieldType = field.getType().getSimpleName();

                    String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    Class<? extends Object> tCls = t.getClass();

                    Method method = tCls.getMethod(setMethodName, field.getType());
                    Object value = getCellValue(cell);
                    //判断值的类型后进行强制类型转换
                    if (null != value) {
                        if ("String".equals(fieldType)) {
                            log.debug("String {}", value.toString());
                            method.invoke(t, value.toString());
                        } else if ("Double".equals(fieldType)) {
                            log.debug("Double {}", value.toString());
                            method.invoke(t, (Double) value);
                        } else if ("int".equals(fieldType)) {
                            log.debug("int {}", value.toString());
                            int val = Integer.parseInt((String) value);
                            method.invoke(t, val);
                        } else if ("BigDecimal".equals(fieldType)) {
                            log.debug("BigDecimal {}", value.toString());
                            method.invoke(t, new BigDecimal(value.toString()));
                        } else if ("Integer".equals(fieldType)) {
                            log.debug("Integer {}", value.toString());
                            method.invoke(t, new Integer(value.toString()));
                        } else if ("Date".equals(fieldType)) {
                            log.info("读取的value【{}】", value);
                            log.info("读取到Date【{}】", value.toString());
                            method.invoke(t, DateUtils.stringsToDate(value.toString()));
                        }
                    }
                }

            }
            list.add(t);
        }

        log.info("====文件读取结束====");
        return list;
    }

    /**
     * 读取Excel2007文件中的数据，返回list
     *
     * @param inputStream 输入流
     * @param c           泛型入参
     * @param <T>         泛型
     * @return list 返回结果
     * @throws Exception 异常
     */
    public static <T> List<T> readExcel2007(InputStream inputStream, Class<T> c) throws Exception {

        Workbook workbook = new XSSFWorkbook(inputStream); //创建工作蒲
        XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(0); //创建工作表
        XSSFRow row = null;  //对应行
        XSSFCell cell = null; //对应单元格
        row = sheet.getRow(0);

        int totalRow = sheet.getLastRowNum(); //得到sheet表总行数
        int totalCell = row.getLastCellNum(); //列数
        log.info("总行数【{}】, 总列数【{}】", totalRow, totalCell);

        T t = c.newInstance();
        Field[] fields = t.getClass().getDeclaredFields();

        List<T> list = new ArrayList<>();
        for (int i = 1; i <= totalRow; i++) {
            row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                continue;
            }

            t = c.newInstance();

            log.debug("正在处理第{}行", i);
            for (int j = 0; j < totalCell; j++) {

                cell = row.getCell(j);
                if (cell != null) {
                    Field field = fields[j];
                    String fieldName = field.getName();
                    String fieldType = field.getType().getSimpleName();
                    String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                    Class<? extends Object> tCls = t.getClass();
                    Method method = tCls.getMethod(setMethodName, field.getType());
                    Object value = getCellValue(cell);

                    //判断值的类型后进行强制类型转换
                    if (null != value) {
                        if ("String".equals(fieldType)) {
                            log.debug("String{}", value.toString());
                            method.invoke(t, value.toString());
                        } else if ("Double".equals(fieldType)) {
                            log.debug("Double{}", value.toString());
                            method.invoke(t, (Double) value);
                        } else if ("int".equals(fieldType)) {
                            log.debug("int{}", value.toString());
                            int val = Integer.parseInt((String) value);
                            method.invoke(t, val);
                        } else if ("BigDecimal".equals(fieldType)) {
                            log.debug("BigDecimal{}", value.toString());
                            method.invoke(t, new BigDecimal(value.toString()));
                        } else if ("Integer".equals(fieldType)) {
                            log.debug("Integer{}", value.toString());
                            method.invoke(t, new Integer(value.toString()));
                        } else if ("Date".equals(fieldType)) {
                            log.debug("Date{}", value.toString());
                            method.invoke(t, DateUtils.stringsToDate(value.toString()));
                        }
                    }
                }

            }
            list.add(t);
        }

        log.info("====文件读取结束====");
        return list;
    }

    /**
     * 判断表格空行
     *
     * @param row 行
     * @return boolean 返回结果
     */
    public static boolean isRowEmpty(Row row) {
        int num = row.getLastCellNum();
        int empty = 0;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
                empty++;
            }
        }
        log.info("总列数：" + num + "；空值列数：" + empty);
        return num == empty;
    }

    /**
     * 根据单元格类型取数据
     *
     * @param cell 表格
     * @return Object 返回结果
     */
    public static Object getCellValue(XSSFCell cell) {
        Object o = null;
        int cellType = cell.getCellType();
        switch (cellType) {
            case XSSFCell.CELL_TYPE_BLANK:
                o = "";
                break;
            case XSSFCell.CELL_TYPE_BOOLEAN:
                o = cell.getBooleanCellValue();
                break;
            case XSSFCell.CELL_TYPE_ERROR:
                o = "Bad value!";
                break;
            case XSSFCell.CELL_TYPE_NUMERIC:
                o = getValueOfNumericCell(cell);
                break;
            case XSSFCell.CELL_TYPE_FORMULA:
                try {
                    o = getValueOfNumericCell(cell);
                } catch (IllegalStateException e) {
                    try {
                        o = cell.getRichStringCellValue().toString();
                    } catch (IllegalStateException e2) {
                        o = cell.getErrorCellString();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                break;
            default:
                o = cell.getRichStringCellValue().getString();
        }
        return o;
    }

    /**
     * 根据单元格类型取数据
     *
     * @param cell 表格
     * @return Object 返回结果
     */
    public static Object getCellValue(HSSFCell cell) {
        Object o = null;
        int cellType = cell.getCellType();
        switch (cellType) {
            case XSSFCell.CELL_TYPE_BLANK:
                o = "";
                break;
            case XSSFCell.CELL_TYPE_BOOLEAN:
                o = cell.getBooleanCellValue();
                break;
            case XSSFCell.CELL_TYPE_ERROR:
                o = "Bad value!";
                break;
            case XSSFCell.CELL_TYPE_NUMERIC:
                o = getValueOfNumericCell(cell);
                break;
            case XSSFCell.CELL_TYPE_FORMULA:
                try {
                    o = getValueOfNumericCell(cell);
                } catch (IllegalStateException e) {
                    try {
                        o = cell.getRichStringCellValue().toString();
                    } catch (IllegalStateException e2) {
                        o = cell.getErrorCellValue();
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
                break;
            default:
                o = cell.getRichStringCellValue().getString();
        }
        return o;
    }

    /**
     * 单元格数值处理
     *
     * @param cell 表格对象
     * @return Object 返回结果
     */
    private static Object getValueOfNumericCell(XSSFCell cell) {
        Boolean isDate = HSSFDateUtil.isCellDateFormatted(cell);
        Double d = cell.getNumericCellValue();
        Object o = null;
        if (isDate) {
            o = DateFormat.getDateTimeInstance()
                    .format(cell.getDateCellValue());
            log.debug("date:【】", o);
        } else {
            o = getRealStringValueOfDouble(d);
        }
        return o;
    }

    /**
     * 单元格数值处理
     *
     * @param cell 表格对象
     * @return Object 返回结果
     */
    private static Object getValueOfNumericCell(HSSFCell cell) {
        Boolean isDate = HSSFDateUtil.isCellDateFormatted(cell);
        Double d = cell.getNumericCellValue();
        Object o = null;
        if (isDate) {
            o = DateFormat.getDateTimeInstance()
                    .format(cell.getDateCellValue());
        } else {
            o = getRealStringValueOfDouble(d);
        }
        return o;
    }

    /**
     * 处理科学计数法与普通计数法的字符串显示，尽最大努力保持精度
     *
     * @param d 入参
     * @return string 返回结果
     */
    private static String getRealStringValueOfDouble(Double d) {
        String doubleStr = d.toString();
        boolean b = doubleStr.contains("E");
        int indexOfPoint = doubleStr.indexOf('.');
        if (b) {
            int indexOfE = doubleStr.indexOf('E');
            // 小数部分
            BigInteger xs = new BigInteger(doubleStr.substring(indexOfPoint
                    + BigInteger.ONE.intValue(), indexOfE));
            // 指数
            int pow = Integer.parseInt(doubleStr.substring(indexOfE
                    + BigInteger.ONE.intValue()));
            int xsLen = xs.toByteArray().length;
            int scale = xsLen - pow > 0 ? xsLen - pow : 0;
            doubleStr = String.format("%." + scale + "f", d);
        } else {
            Pattern p = Pattern.compile(".0$");
            Matcher m = p.matcher(doubleStr);
            if (m.find()) {
                doubleStr = doubleStr.replace(".0", "");
            }
        }
        return doubleStr;
    }

    /**
     * 导出2003版Excel
     *
     * @param excelName  Excel文件名
     * @param headers    列标题
     * @param dataList   需要导出的数据
     * @param dateFormat 如果有时间，时间格式
     * @param response   响应
     */
    public static void export03Excel(String excelName, String[] headers, List dataList
            , String dateFormat, HttpServletResponse response) {
        //创建工作薄
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            //表头样式
            HSSFCellStyle style = wb.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);//水平居中
            style.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
            //字体样式
            HSSFFont fontStyle = wb.createFont();
            fontStyle.setFontName("微软雅黑");
            fontStyle.setFontHeightInPoints((short) 12);
            fontStyle.setBold(true);//设置是否加粗
            style.setFont(fontStyle);

            //设置单元格内容自动换行
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setWrapText(true);
            //设置文字居中
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            //垂直居中
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            //新建sheet
            HSSFSheet sheet1 = wb.createSheet("Sheet1");

            //生成sheet1内容
            HSSFRow rowFirst = sheet1.createRow(0); //第一个sheet的第一行为标题
            //写标题
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = rowFirst.createCell(i); //获取第一行的每个单元格
                sheet1.setColumnWidth(i, 5000); //设置每列的列宽
                cell.setCellStyle(style); //加样式
                cell.setCellValue(headers[i]); //往单元格里写数据
            }
            for (int index = 0; index < dataList.size(); index++) {
                HSSFRow row = sheet1.createRow(index + 1);
                Field[] fields = dataList.get(index).getClass().getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    HSSFCell cell = row.createCell(i);
                    //单元格设置内容自动换行
                    cell.setCellStyle(cellStyle);
                    Field field = fields[i];
                    String fieldName = field.getName();
                    String getMethodName = "get"
                            + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);
                    try {
                        Method getMethod = dataList.get(index).getClass().getMethod(getMethodName);
                        Object value = getMethod.invoke(dataList.get(index));
                        //空值，遍历下一个
                        if (value == null || value.toString().isEmpty()) {
                            continue;
                        }
                        // 判断值的类型后进行强制类型转换
                        String textValue = null;
                        if (value instanceof Date) {
                            Date date = (Date) value;
                            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                            textValue = sdf.format(date);
                        } else {
                            // 其它数据类型都当作字符串简单处理
                            textValue = value.toString();
                        }
                        if (textValue != null) {
                            Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                            Matcher matcher = p.matcher(textValue);
                            if (matcher.matches()) {
                                // 是数字当作double处理
                                cell.setCellValue(Double.parseDouble(textValue));
                            } else {
                                HSSFRichTextString richString = new HSSFRichTextString(
                                        textValue);
                                cell.setCellValue(richString);
                            }
                        }
                    } catch (NoSuchMethodException e) {
                        log.info("NoSuchMethodException:", e.getMessage());
                    } catch (IllegalAccessException e) {
                        log.info("IllegalAccessException:", e.getMessage());
                    } catch (InvocationTargetException e) {
                        log.info("InvocationTargetException:", e.getMessage());
                    }
                }
            }
            try (OutputStream output = response.getOutputStream()) {
                excelName = excelName + ExcelConstant.EXCEL_03_FORMAT;
                response.setContentType("APPLICATION/OCTET-STREAM");
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-disposition", "attachment;filename="
                        + excelName);
                wb.write(output);
                output.flush();
            } catch (UnsupportedEncodingException e) {
                log.info("UnsupportedEncodingException:", e.getMessage());
            } catch (IOException e) {
                log.info("IOException:", e.getMessage());
            }
        } catch (Exception e) {
            log.info("Exception:", e.getMessage());
        }
    }


    /**
     * 导出2003版Excel-导出多个sheet
     *
     * @param excelName  Excel文件名
     * @param headers    列标题
     * @param dataList   需要导出的数据
     * @param dateFormat 如果有时间，时间格式
     * @param response   响应
     */
    public static void export03ExcelInSheets(String excelName, String[] headers, List dataList
            , String dateFormat, HttpServletResponse response) {
        //创建工作薄
        try (HSSFWorkbook wb = new HSSFWorkbook()) {

            //表头样式
            HSSFCellStyle style = wb.createCellStyle();
            style.setAlignment(HorizontalAlignment.CENTER);//水平居中
            style.setVerticalAlignment(VerticalAlignment.CENTER);//垂直居中
            //字体样式
            HSSFFont fontStyle = wb.createFont();
            fontStyle.setFontName("微软雅黑");
            fontStyle.setFontHeightInPoints((short) 12);
            fontStyle.setBold(true);//设置是否加粗
            style.setFont(fontStyle);

            //设置单元格内容自动换行
            HSSFCellStyle cellStyle = wb.createCellStyle();
            cellStyle.setWrapText(true);
            //设置文字居中
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            //垂直居中
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            dataToSheets(headers, dataList, dateFormat, wb, style, cellStyle);
            log.info("导出人车关联数据写入完毕");
            try (OutputStream output = response.getOutputStream()) {
                excelName = excelName + ExcelConstant.EXCEL_03_FORMAT;
                response.setContentType("APPLICATION/OCTET-STREAM");
                response.setCharacterEncoding("utf-8");
                response.setHeader("Content-disposition", "attachment;filename="
                        + excelName);
                wb.write(output);
                output.flush();
                log.info("导出人车关联文件输出完毕");
            } catch (UnsupportedEncodingException e) {
                log.info("UnsupportedEncodingException:", e.getMessage());
            } catch (IOException e) {
                log.info("IOException:", e.getMessage());
            }
        } catch (Exception e) {
            log.info("Exception:", e.getMessage());
        }
    }

    private static void dataToSheets(String[] headers, List dataList, String dateFormat,
                                     HSSFWorkbook wb, HSSFCellStyle style, HSSFCellStyle cellStyle) {
        //计算需要的sheet数量
        int sheetCount = dataList.size() % SHEET_EXPORT_MAX == 0 ?
                dataList.size() / SHEET_EXPORT_MAX : dataList.size() / SHEET_EXPORT_MAX + 1;

        for (int m = 0; m < sheetCount; m++) {
            //新建sheet
            HSSFSheet sheet = wb.createSheet("数据表" + (m + 1));
            //生成sheet1内容
            HSSFRow rowFirst = sheet.createRow(0); //第一个sheet的第一行为标题
            //写标题
            for (int i = 0; i < headers.length; i++) {
                HSSFCell cell = rowFirst.createCell(i); //获取第一行的每个单元格
                sheet.setColumnWidth(i, 5000); //设置每列的列宽
                cell.setCellStyle(style); //加样式
                cell.setCellValue(headers[i]); //往单元格里写数据
            }
            if ((m + 1) * SHEET_EXPORT_MAX <= dataList.size()) {
                writeDataToSheet(m * SHEET_EXPORT_MAX, (m + 1) * SHEET_EXPORT_MAX, dataList, dateFormat, cellStyle, sheet);
            } else {
                writeDataToSheet(m * SHEET_EXPORT_MAX, dataList.size(), dataList, dateFormat, cellStyle, sheet);
            }

        }


    }

    private static void writeDataToSheet(int begin, int end, List dataList, String dateFormat, HSSFCellStyle cellStyle, HSSFSheet sheet) {
        for (int index = begin; index < end; index++) {
            HSSFRow row = sheet.createRow(index % SHEET_EXPORT_MAX + 1);
            Field[] fields = dataList.get(index).getClass().getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                HSSFCell cell = row.createCell(i);
                //单元格设置内容自动换行
                cell.setCellStyle(cellStyle);
                Field field = fields[i];
                String fieldName = field.getName();
                String getMethodName = "get"
                        + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);
                try {
                    Method getMethod = dataList.get(index).getClass().getMethod(getMethodName);
                    Object value = getMethod.invoke(dataList.get(index));
                    //空值，遍历下一个
                    if (value == null || value.toString().isEmpty()) {
                        continue;
                    }
                    // 判断值的类型后进行强制类型转换
                    String textValue = null;
                    if (value instanceof Date) {
                        Date date = (Date) value;
                        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                        textValue = sdf.format(date);
                    } else {
                        // 其它数据类型都当作字符串简单处理
                        textValue = value.toString();
                    }
                    if (textValue != null) {
                        Pattern p = Pattern.compile("^//d+(//.//d+)?$");
                        Matcher matcher = p.matcher(textValue);
                        if (matcher.matches()) {
                            // 是数字当作double处理
                            cell.setCellValue(Double.parseDouble(textValue));
                        } else {
                            HSSFRichTextString richString = new HSSFRichTextString(
                                    textValue);
                            cell.setCellValue(richString);
                        }
                    }
                } catch (NoSuchMethodException e) {
                    log.info("NoSuchMethodException:", e.getMessage());
                } catch (IllegalAccessException e) {
                    log.info("IllegalAccessException:", e.getMessage());
                } catch (InvocationTargetException e) {
                    log.info("InvocationTargetException:", e.getMessage());
                }
            }
        }
    }
}
