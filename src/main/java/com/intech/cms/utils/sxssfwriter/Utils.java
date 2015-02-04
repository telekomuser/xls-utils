package com.intech.cms.utils.sxssfwriter;

import org.apache.poi.ss.usermodel.CellStyle;
import org.xml.sax.Attributes;

class Utils {
	
	static String attributesToString(Attributes aa) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < aa.getLength(); i++) {
			sb.append(aa.getLocalName(i));
			sb.append('(');
			sb.append(aa.getType(i));
			sb.append(')');
			sb.append('=');
			sb.append(aa.getValue(i));
			sb.append(';');
			sb.append(' ');
		}
		return sb.toString();
	}

//	/**
//	 * key = name of attribute, value is attribute object
//	 * 
//	 * @param aa
//	 * @return
//	 */
//	public static Map<String, Attribute> copyAttributes(Attributes aa) {
//
//		Map<String, Attribute> result = new HashMap<String, Attribute>();
//
//		for (int i = 0; i < aa.getLength(); i++) {
//			result.put(aa.getLocalName(i), new Attribute(aa.getLocalName(i), aa.getType(i), aa.getValue(i)));
//		}
//
//		return result;
//	}
	
	static String cellStyleToString(CellStyle cs){
		return 
				"Alignment: "+cs.getAlignment() +", "+
				"BorderBottom: "+cs.getBorderBottom() +", "+
				"BorderLeft: "+cs.getBorderLeft() +", "+
				"BorderRight: "+cs.getBorderRight() +", "+
				"BorderTop: "+cs.getBorderTop() +", "+
				"BottomBorderColor: "+cs.getBottomBorderColor() +", "+
				"DataFormat: "+cs.getDataFormat() +", "+
				"DataFormatString: "+cs.getDataFormatString() +", "+
				"FillBackgroundColor: "+cs.getFillBackgroundColor() +", "+
				"FillBackgroundColorColor: "+cs.getFillBackgroundColorColor() +", "+
				"FillForegroundColor: "+cs.getFillForegroundColor() +", "+
				"FillForegroundColorColor: "+cs.getFillForegroundColorColor() +", "+
				"FillPattern: "+cs.getFillPattern() +", "+
				"FontIndex: "+cs.getFontIndex() +", "+
				"Hidden: "+cs.getHidden() +", "+
				"Indention: "+cs.getIndention() +", "+
				"Index: "+cs.getIndex() +", "+
				"LeftBorderColor: "+cs.getLeftBorderColor() +", "+
				"Locked: "+cs.getLocked() +", "+
				"RightBorderColor: "+cs.getRightBorderColor() +", "+
				"Rotation: "+cs.getRotation() +", "+
				"ShrinkToFit: "+cs.getShrinkToFit() +", "+
				"TopBorderColor: "+cs.getTopBorderColor() +", "+
				"VerticalAlignment: "+cs.getVerticalAlignment() +", "+
				"WrapTex: "+cs.getWrapText();
	}
	
}
