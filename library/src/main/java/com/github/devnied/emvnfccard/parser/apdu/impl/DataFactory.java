package com.github.devnied.emvnfccard.parser.apdu.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.devnied.emvnfccard.model.enums.IKeyEnum;
import com.github.devnied.emvnfccard.parser.apdu.annotation.AnnotationData;
import com.github.devnied.emvnfccard.utils.EnumUtils;

import fr.devnied.bitlib.BitUtils;

/**
 * Factory to parse data
 * 
 * @author julien Millau
 */
public final class DataFactory {

	/**
	 * Logger of this class
	 */
	public static final Logger LOGGER = LoggerFactory.getLogger(DataFactory.class.getName());

	/**
	 * Constant for EN1545-1 (Date format)
	 */
	public static final int BCD_DATE = 1;

	/**
	 * Half byte size
	 */
	public static final int HALF_BYTE_SIZE = 4;

	/**
	 * Method to get a date from the bytes table
	 * 
	 * @param pAnnotation
	 *            annotation data
	 * @param pBit
	 *            table bytes
	 * @return The date read of null
	 */
	private static Date getDate(final AnnotationData pAnnotation, final BitUtils pBit) {
		Date date = null;
		if (pAnnotation.getDateStandard() == BCD_DATE) {
			SimpleDateFormat sdf = new SimpleDateFormat(pAnnotation.getFormat());
			StringBuffer buff = new StringBuffer();
			for (int i = 0; i < pAnnotation.getSize(); i += HALF_BYTE_SIZE) {
				buff.append(pBit.getNextInteger(HALF_BYTE_SIZE));
			}
			try {
				date = sdf.parse(buff.toString());
			} catch (ParseException e) {
				LOGGER.error("Start date invalid" + e.getMessage(), e);
			}
		} else {
			date = pBit.getNextDate(pAnnotation.getSize(), pAnnotation.getFormat());
		}
		return date;
	}

	/**
	 * This method is used to get an integer
	 * 
	 * @param pAnnotation
	 *            annotation
	 * @param pObject
	 *            the object to set
	 * @param pBit
	 *            bit array
	 */
	private static int getInteger(final AnnotationData pAnnotation, final BitUtils pBit) {
		return pBit.getNextInteger(pAnnotation.getSize());
	}

	/**
	 * Method to read and object from the bytes tab
	 * 
	 * @param pAnnotation
	 *            all information data
	 * @param pBit
	 *            bytes tab
	 * @return an object
	 */
	public static Object getObject(final AnnotationData pAnnotation, final BitUtils pBit) {
		Object obj = null;
		Class<?> clazz = pAnnotation.getField().getType();

		if (clazz.equals(Integer.class)) {
			obj = getInteger(pAnnotation, pBit);
		} else if (clazz.equals(Float.class)) {
			obj = (float) getInteger(pAnnotation, pBit);
		} else if (clazz.equals(String.class)) {
			obj = getString(pAnnotation, pBit);
		} else if (clazz.equals(Date.class)) {
			obj = getDate(pAnnotation, pBit);
		} else if (clazz.equals(Boolean.class)) {
			obj = pBit.getNextBoolean();
		} else if (clazz.isEnum()) {
			obj = getEnum(pAnnotation, pBit);
		}
		return obj;
	}

	/**
	 * This method is used to get an enum with his key
	 * 
	 * @param pAnnotation
	 *            annotation
	 * @param pBit
	 *            bit array
	 */
	@SuppressWarnings("unchecked")
	private static IKeyEnum getEnum(final AnnotationData pAnnotation, final BitUtils pBit) {
		int val = Integer.parseInt(pBit.getNextHexaString(pAnnotation.getSize()));
		return EnumUtils.getValue(val, (Class<? extends IKeyEnum>) pAnnotation.getField().getType());
	}

	/**
	 * This method get a string (Hexa or ASCII) from a bit table
	 * 
	 * @param pAnnotation
	 *            annotation data
	 * @param pBit
	 *            bit table
	 * @return A string
	 */
	private static String getString(final AnnotationData pAnnotation, final BitUtils pBit) {
		String obj = null;

		if (pAnnotation.isReadHexa()) {
			obj = pBit.getNextHexaString(pAnnotation.getSize());
		} else {
			obj = pBit.getNextString(pAnnotation.getSize()).trim();
		}

		return obj;
	}

	/**
	 * Private constructor
	 */
	private DataFactory() {
	}
}
