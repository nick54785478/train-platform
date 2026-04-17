package com.example.demo.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Slf4j
@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JasperUtil {

    /**
     * 根據提供的 Jasper 檔案、資料集合和參數產生PDF報表。
     *
     * @param jasperFilename Jasper 報表範本的檔案名
     * @param list 包含在報表中的資料集合
     * @param parameters 參數要提交給報表範本的參數
     * @return 包含產生的 PDF 報告的 ByteArrayResource ，如果發生錯誤則回傳 null
     */
    public static ByteArrayResource generateReportToPDF(String jasperFilename, Collection<?> list,
            Map<String, Object> parameters) {
        try (var outputStream = new ByteArrayOutputStream();) {
            // 使用資料和參數填入 Jasper 報表模板
            var jasperPrint = fillReport(jasperFilename, list, parameters);
            // 將填入後的報表匯出為 PDF 流
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            byte[] reportContent = outputStream.toByteArray();
            return new ByteArrayResource(reportContent);
        } catch (JRException e) {
            log.error("讀取 Jasper 模板發生錯誤 ", e);
        } catch (IOException e) {
            log.error("轉換 OutputStream 發生錯誤 ", e);
        }
        return null;
    }

    /**
     * 根據提供的 Jasper 檔案、資料集合和參數產生PDF報表。
     *
     * @param inputStream Jasper 報表範本的檔案流
     * @param list 包含在報表中的資料集合
     * @param parameters 參數要提交給報表範本的參數
     * @return 包含產生的PDF報告的 ByteArrayResource ，如果發生錯誤則回傳 null
     */
    public static ByteArrayResource generateReportToPDF(InputStream is, Collection<?> list,
            Map<String, Object> parameters) {
        try (var outputStream = new ByteArrayOutputStream();) {
            // 使用資料和參數填入 Jasper 報表模板
            var jasperPrint = fillReport(is, list, parameters);
            // 將填入後的報表匯出為 PDF 流
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            byte[] reportContent = outputStream.toByteArray();
            return new ByteArrayResource(reportContent);
        } catch (IOException | JRException e) {
            log.error("generate report to PDF error ", e);
        }
        return null;
    }


    /**
     * 根據提供的 Jasper 檔案、資料集合和參數產生PDF報表。
     *
     * @param jasperFilename Jasper 報表範本的檔案名
     * @param list 包含在報表中的資料集合(會根據集合內容元素去設值)
     * @param parameters 參數要提交給報表範本的參數(會根據 key-value 設值)
     * @return 包含產生的PDF報告的 byte[]
     * @throws SQLException 如果在產生報表時發生SQL異常
     */
    public static byte[] generateReportToByte(String jasperFilename, Collection<?> list,
            Map<String, Object> parameters) {

        try (var outputStream = new ByteArrayOutputStream();) {
            // 使用資料和參數填入 Jasper 報表模板
            var jasperPrint = fillReport(jasperFilename, list, parameters);
            // 將填入後的報表匯出為 PDF 流
            JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
            return outputStream.toByteArray();
        } catch (IOException | JRException e) {
            log.error("generate report to PDF error", e);
        }
        return new byte[0];
    }

    /**
     * 建立 Chart 圖表
     * 
     * @param is 資料流
     * @param parameters 參數要提交給報表範本的參數(會根據 key-value 設值)
     * @return 包含產生的PDF報告的 ByteArrayResource ，如果發生錯誤則回傳 null
     * */
    public static ByteArrayResource generateChart(InputStream is, Map<String, Object> parameters) {
        try (var outputStream = new ByteArrayOutputStream();) {
            JasperRunManager.runReportToPdfStream(is, outputStream, parameters,
                    new JREmptyDataSource());
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (JRException | IOException e) {
            log.error("generate report to PDF error", e);
        }
        return null;
    }



    /**
     * 使用提供的資料集合和參數填入 Jasper 報表模板，並傳回填入後的 JasperPrint 物件。
     *
     * @param jasperFilename Jasper 報表範本的檔案名稱（不包括副檔名）
     * @param list 包含在報表中的資料集合
     * @param parameters 參數要提交給報表範本的參數
     * @return 填滿後的 JasperPrint 對象
     * @throws JRException 如果在填入報表時發生 JasperReports 異常
     * @throws IOException 如果無法讀取報表檔案時發生 IO 異常
     */
    private static JasperPrint fillReport(String jasperFilename, Collection<?> list,
            Map<String, Object> parameters) throws JRException, IOException {
        // 使用 JRBeanCollectionDataSource 將資料集合轉換為JasperReports資料來源
        JRDataSource datasource = new JRBeanCollectionDataSource(list);
        // 取得 Jasper 報表範本檔案的路徑
        var jasper = new ClassPathResource("report" + File.separator + jasperFilename + ".jasper");
        log.debug("jasper: {}", "report" + File.separator + jasperFilename + ".jasper");
        return JasperFillManager.fillReport(jasper.getInputStream(), parameters, datasource);
    }

    /**
     * 使用提供的資料集合和參數填入 Jasper 報表模板，並傳回填入後的 JasperPrint物件。
     *
     * @param inputStream Jasper 報表範本的檔案流
     * @param list 包含在報表中的資料集合
     * @param parameters 參數要提交給報表範本的參數
     * @return 填滿後的 JasperPrint 對象
     * @throws JRException 如果在填入報表時發生 JasperReports 異常
     * @throws IOException 如果無法讀取報表檔案時發生 IO 異常
     */
    private static JasperPrint fillReport(InputStream is, Collection<?> list,
            Map<String, Object> parameters) throws JRException {
        // 使用 JRBeanCollectionDataSource 將資料集合轉換為 JasperReports 資料來源
        JRDataSource datasource = new JRBeanCollectionDataSource(list);
        return JasperFillManager.fillReport(is, parameters, datasource);
    }
    


}
