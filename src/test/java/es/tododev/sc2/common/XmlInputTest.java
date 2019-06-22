package es.tododev.sc2.common;

import java.math.BigDecimal;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import es.tododev.sc2.process.Dto;

public class XmlInputTest {

	private final IInput<Dto> input = new XmlInput();
	
	@Test
	public void checkInput() throws StreamCombinerException {
		String xml = "<data> <timestamp>123456789</timestamp> <amount>1234.567890</amount> </data>";
		Dto dto = input.toObject(xml);
		assertEquals(123456789, dto.getTimestamp());
		assertEquals(new BigDecimal("1234.567890"), dto.getAmount());
	}
	
}
