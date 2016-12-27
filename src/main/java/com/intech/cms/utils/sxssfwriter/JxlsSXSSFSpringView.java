package com.intech.cms.utils.sxssfwriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.CellRef;
import org.jxls.common.Context;
import org.jxls.transform.poi.PoiTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.view.AbstractView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * Spring View для шаблонов формата XLSX, использующий стриминг SXSSF, использующий библиотеку Jxls.
 * Подходит для больших выгрузок (>10К строк).
 * Параметр rowAccessWindowSize - количество строк, которые удерживаюися в памяти (по-умолчанию 50 строк),
 * остальное находится во временном файле на диске. Такой подход позволяет формировать очень большие файлы
 * быстро и без больших затрат памяти.
 *
 * @see http://jxls.sourceforge.net/index.html
 * Created by arakushin on 27.12.16.
 */
public class JxlsSXSSFSpringView extends AbstractView {

    private static final Logger log			= LoggerFactory.getLogger(JxlsSXSSFSpringView.class);
    /* Internet media types */
    private static final String	XLSX_IMT	= "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private String				template;

    private String				attachmentName;

    private int                 rowAccessWindowSize = 50;

    private boolean             compressTmpFiles = false;

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model,
                                           HttpServletRequest request,
                                           HttpServletResponse response)
            throws Exception {

        model.put("su", new StringUtils());
        model.put("dfu", new DateFormatUtils());
        ServletOutputStream output = response.getOutputStream();

        try {
            response.setContentType(XLSX_IMT);
            response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\";", attachmentName));

            InputStream ris = new ServletContextResource(getServletContext(), template).getInputStream();
            InputStream is = new BufferedInputStream(ris);

            Context context = new Context();
            for(Map.Entry<String, Object> entry : model.entrySet()) {
                context.putVar(entry.getKey(), entry.getValue());
            }

            Workbook workbook = WorkbookFactory.create(is);
            PoiTransformer transformer = PoiTransformer.createSxssfTransformer(workbook, rowAccessWindowSize, compressTmpFiles);

            AreaBuilder areaBuilder = new XlsCommentAreaBuilder(transformer);
            List<Area> xlsAreaList = areaBuilder.build();
            Area xlsArea = xlsAreaList.get(0);
            xlsArea.applyAt(new CellRef("Result!A1"), context);
            Workbook workbook2 = transformer.getWorkbook();
            workbook2.removeSheetAt(0);
            workbook2.write(output);

            is.close();
            output.close();
        }
        catch (Exception e) {
            log.error("Exception while sending xls report", e);
            try {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
            catch (IOException e1) {
            }
        }
        output.flush();
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public int getRowAccessWindowSize() {
        return rowAccessWindowSize;
    }

    public void setRowAccessWindowSize(int rowAccessWindowSize) {
        this.rowAccessWindowSize = rowAccessWindowSize;
    }

    public boolean isCompressTmpFiles() {
        return compressTmpFiles;
    }

    public void setCompressTmpFiles(boolean compressTmpFiles) {
        this.compressTmpFiles = compressTmpFiles;
    }
}
