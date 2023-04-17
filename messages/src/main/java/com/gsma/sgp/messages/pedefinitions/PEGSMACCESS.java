/**
 * This class file was automatically generated by jASN1 v1.11.3 (http://www.beanit.com)
 */

package com.gsma.sgp.messages.pedefinitions;

import java.io.IOException;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.io.Serializable;
import com.beanit.jasn1.ber.*;
import com.beanit.jasn1.ber.types.*;
import com.beanit.jasn1.ber.types.string.*;


public class PEGSMACCESS implements BerType, Serializable {

	private static final long serialVersionUID = 1L;

	public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);

	public byte[] code = null;
	private PEHeader gsmAccessHeader = null;
	private BerObjectIdentifier templateID = null;
	private File dfGsmAccess = null;
	private File efKc = null;
	private File efKcgprs = null;
	private File efCpbcch = null;
	private File efInvscan = null;
	
	public PEGSMACCESS() {
	}

	public PEGSMACCESS(byte[] code) {
		this.code = code;
	}

	public void setGsmAccessHeader(PEHeader gsmAccessHeader) {
		this.gsmAccessHeader = gsmAccessHeader;
	}

	public PEHeader getGsmAccessHeader() {
		return gsmAccessHeader;
	}

	public void setTemplateID(BerObjectIdentifier templateID) {
		this.templateID = templateID;
	}

	public BerObjectIdentifier getTemplateID() {
		return templateID;
	}

	public void setDfGsmAccess(File dfGsmAccess) {
		this.dfGsmAccess = dfGsmAccess;
	}

	public File getDfGsmAccess() {
		return dfGsmAccess;
	}

	public void setEfKc(File efKc) {
		this.efKc = efKc;
	}

	public File getEfKc() {
		return efKc;
	}

	public void setEfKcgprs(File efKcgprs) {
		this.efKcgprs = efKcgprs;
	}

	public File getEfKcgprs() {
		return efKcgprs;
	}

	public void setEfCpbcch(File efCpbcch) {
		this.efCpbcch = efCpbcch;
	}

	public File getEfCpbcch() {
		return efCpbcch;
	}

	public void setEfInvscan(File efInvscan) {
		this.efInvscan = efInvscan;
	}

	public File getEfInvscan() {
		return efInvscan;
	}

	public int encode(OutputStream reverseOS) throws IOException {
		return encode(reverseOS, true);
	}

	public int encode(OutputStream reverseOS, boolean withTag) throws IOException {

		if (code != null) {
			for (int i = code.length - 1; i >= 0; i--) {
				reverseOS.write(code[i]);
			}
			if (withTag) {
				return tag.encode(reverseOS) + code.length;
			}
			return code.length;
		}

		int codeLength = 0;
		if (efInvscan != null) {
			codeLength += efInvscan.encode(reverseOS, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 6
			reverseOS.write(0xA6);
			codeLength += 1;
		}
		
		if (efCpbcch != null) {
			codeLength += efCpbcch.encode(reverseOS, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 5
			reverseOS.write(0xA5);
			codeLength += 1;
		}
		
		if (efKcgprs != null) {
			codeLength += efKcgprs.encode(reverseOS, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 4
			reverseOS.write(0xA4);
			codeLength += 1;
		}
		
		if (efKc != null) {
			codeLength += efKc.encode(reverseOS, false);
			// write tag: CONTEXT_CLASS, CONSTRUCTED, 3
			reverseOS.write(0xA3);
			codeLength += 1;
		}
		
		codeLength += dfGsmAccess.encode(reverseOS, false);
		// write tag: CONTEXT_CLASS, CONSTRUCTED, 2
		reverseOS.write(0xA2);
		codeLength += 1;
		
		codeLength += templateID.encode(reverseOS, false);
		// write tag: CONTEXT_CLASS, PRIMITIVE, 1
		reverseOS.write(0x81);
		codeLength += 1;
		
		codeLength += gsmAccessHeader.encode(reverseOS, false);
		// write tag: CONTEXT_CLASS, CONSTRUCTED, 0
		reverseOS.write(0xA0);
		codeLength += 1;
		
		codeLength += BerLength.encodeLength(reverseOS, codeLength);

		if (withTag) {
			codeLength += tag.encode(reverseOS);
		}

		return codeLength;

	}

	public int decode(InputStream is) throws IOException {
		return decode(is, true);
	}

	public int decode(InputStream is, boolean withTag) throws IOException {
		int codeLength = 0;
		int subCodeLength = 0;
		BerTag berTag = new BerTag();

		if (withTag) {
			codeLength += tag.decodeAndCheck(is);
		}

		BerLength length = new BerLength();
		codeLength += length.decode(is);

		int totalLength = length.val;
		codeLength += totalLength;

		subCodeLength += berTag.decode(is);
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 0)) {
			gsmAccessHeader = new PEHeader();
			subCodeLength += gsmAccessHeader.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
			templateID = new BerObjectIdentifier();
			subCodeLength += templateID.decode(is, false);
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 2)) {
			dfGsmAccess = new File();
			subCodeLength += dfGsmAccess.decode(is, false);
			if (subCodeLength == totalLength) {
				return codeLength;
			}
			subCodeLength += berTag.decode(is);
		}
		else {
			throw new IOException("Tag does not match the mandatory sequence element tag.");
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 3)) {
			efKc = new File();
			subCodeLength += efKc.decode(is, false);
			if (subCodeLength == totalLength) {
				return codeLength;
			}
			subCodeLength += berTag.decode(is);
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 4)) {
			efKcgprs = new File();
			subCodeLength += efKcgprs.decode(is, false);
			if (subCodeLength == totalLength) {
				return codeLength;
			}
			subCodeLength += berTag.decode(is);
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 5)) {
			efCpbcch = new File();
			subCodeLength += efCpbcch.decode(is, false);
			if (subCodeLength == totalLength) {
				return codeLength;
			}
			subCodeLength += berTag.decode(is);
		}
		
		if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 6)) {
			efInvscan = new File();
			subCodeLength += efInvscan.decode(is, false);
			if (subCodeLength == totalLength) {
				return codeLength;
			}
		}
		throw new IOException("Unexpected end of sequence, length tag: " + totalLength + ", actual sequence length: " + subCodeLength);

		
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		ReverseByteArrayOutputStream reverseOS = new ReverseByteArrayOutputStream(encodingSizeGuess);
		encode(reverseOS, false);
		code = reverseOS.getArray();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendAsString(sb, 0);
		return sb.toString();
	}

	public void appendAsString(StringBuilder sb, int indentLevel) {

		sb.append("{");
		sb.append("\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (gsmAccessHeader != null) {
			sb.append("gsmAccessHeader: ");
			gsmAccessHeader.appendAsString(sb, indentLevel + 1);
		}
		else {
			sb.append("gsmAccessHeader: <empty-required-field>");
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (templateID != null) {
			sb.append("templateID: ").append(templateID);
		}
		else {
			sb.append("templateID: <empty-required-field>");
		}
		
		sb.append(",\n");
		for (int i = 0; i < indentLevel + 1; i++) {
			sb.append("\t");
		}
		if (dfGsmAccess != null) {
			sb.append("dfGsmAccess: ");
			dfGsmAccess.appendAsString(sb, indentLevel + 1);
		}
		else {
			sb.append("dfGsmAccess: <empty-required-field>");
		}
		
		if (efKc != null) {
			sb.append(",\n");
			for (int i = 0; i < indentLevel + 1; i++) {
				sb.append("\t");
			}
			sb.append("efKc: ");
			efKc.appendAsString(sb, indentLevel + 1);
		}
		
		if (efKcgprs != null) {
			sb.append(",\n");
			for (int i = 0; i < indentLevel + 1; i++) {
				sb.append("\t");
			}
			sb.append("efKcgprs: ");
			efKcgprs.appendAsString(sb, indentLevel + 1);
		}
		
		if (efCpbcch != null) {
			sb.append(",\n");
			for (int i = 0; i < indentLevel + 1; i++) {
				sb.append("\t");
			}
			sb.append("efCpbcch: ");
			efCpbcch.appendAsString(sb, indentLevel + 1);
		}
		
		if (efInvscan != null) {
			sb.append(",\n");
			for (int i = 0; i < indentLevel + 1; i++) {
				sb.append("\t");
			}
			sb.append("efInvscan: ");
			efInvscan.appendAsString(sb, indentLevel + 1);
		}
		
		sb.append("\n");
		for (int i = 0; i < indentLevel; i++) {
			sb.append("\t");
		}
		sb.append("}");
	}

}
