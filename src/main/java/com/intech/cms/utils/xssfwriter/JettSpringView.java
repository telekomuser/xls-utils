package com.intech.cms.utils.xssfwriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jett.transform.ExcelTransformer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.view.AbstractView;

/**
 * Spring View для шаблонов XLS и XLSX форматов, использующий библиотеку Jett. <br>
 * Тип контента определяется по типу файла шаблона.<br>
 * Подходит для небольших выгрузок (<5К строк). Формирование полного файла происходит в памяти, <br>
 * поэтому при выгрузках более 10К строк, очень вероятен громадный расход памяти, медленная работа и OOM.<br>
 * 
 * @see http://jett.sourceforge.net/
 * @author epotanin
 */
public class JettSpringView extends AbstractView {

	private static final Logger	log			= LoggerFactory.getLogger(JettSpringView.class);

	/* Internet media types */
	private static final String	XLSX_IMT	= "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final String	XLS_IMT		= "application/vnd.ms-excel";											// "application/xls"

	private ExcelTransformer	transformer	= new ExcelTransformer();
	private String				template;
	private String				attachmentName;

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		model.put("su", new StringUtils());
		model.put("dfu", new DateFormatUtils());
		ServletOutputStream output = response.getOutputStream();

		try {
			InputStream ris = new ServletContextResource(getServletContext(), template).getInputStream();
			InputStream is = new BufferedInputStream(ris);

			// здесь вся книга загружается и создается/трансформируется в памяти. Это очень накладно.
			Workbook workbook = transformer.transform(is, model);

			// content type and disposition
			String ct = determineContentType(workbook);
			response.setContentType(ct);
			response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\";", attachmentName));

			workbook.write(output);
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

	private String determineContentType(Workbook wb) {

		String ct = XLS_IMT;
		if (wb instanceof XSSFWorkbook) {
			ct = XLSX_IMT;
		}
		return ct;
	}

}
