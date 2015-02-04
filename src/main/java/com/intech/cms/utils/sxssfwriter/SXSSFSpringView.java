package com.intech.cms.utils.sxssfwriter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.support.ServletContextResource;
import org.springframework.web.servlet.view.AbstractView;

import com.intech.cms.utils.sxssfwriter.dom.JtForEachTagNode;
import com.intech.cms.utils.sxssfwriter.dom.JtForTagNode;
import com.intech.cms.utils.sxssfwriter.dom.Node;
import com.intech.cms.utils.sxssfwriter.dom.WritableNode;

/**
 * Spring View для шаблонов формата XLSX, использующий стриминг SXSSF. <br>
 * Подходит для больших выгрузок (>10К строк). При формирование ответа в памяти удерживается только 50 строк, <br>
 * остальное находится во временном файле на диске. Такой подход позволяет формировать очень большие файлы<br>
 * быстро и без больших затрат памяти. <br>
 * В шаблоне поддерживаются только теги jt:forEach и jt:for (по аналогии с Jett).<br>
 * В отличие от Jett, открывающий и закрывающий теги должны быть в первой ячейке строки. <br>
 * Остальные ячейки в стороке тега отбрасываются.<br>
 * 
 * @author epotanin
 * @see JtForEachTagNode
 * @see JtForTagNode
 */
public class SXSSFSpringView extends AbstractView {

	private static final Logger	log			= LoggerFactory.getLogger(SXSSFSpringView.class);
	/* Internet media types */
	private static final String	XLSX_IMT	= "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	private String				template;
	private String				attachmentName;

	private Node				parsedTemplateRoot;

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		if (parsedTemplateRoot == null) {
			synchronized (this) {
				if (parsedTemplateRoot == null) {
					InputStream gis = new ServletContextResource(getServletContext(), template).getInputStream();
					parsedTemplateRoot = new XLSXDOMTemplateParser().parse(gis);
				}
			}
		}

		model.put("su", new StringUtils());
		model.put("dfu", new DateFormatUtils());
		ServletOutputStream output = response.getOutputStream();

		try {
			response.setContentType(XLSX_IMT);
			response.addHeader("Content-Disposition", String.format("attachment; filename=\"%s\";", attachmentName));

			InputStream ris = new ServletContextResource(getServletContext(), template).getInputStream();
			InputStream is = new BufferedInputStream(ris);

			XSSFWorkbook book = (XSSFWorkbook) WorkbookFactory.create(is);

			SXSSFWriter writer = new SXSSFWriter();
			writer.write((WritableNode) parsedTemplateRoot, book, model, output);

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

}
