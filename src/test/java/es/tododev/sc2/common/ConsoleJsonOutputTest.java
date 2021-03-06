package es.tododev.sc2.common;

import java.math.BigDecimal;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import static org.junit.Assert.assertEquals;

import es.tododev.sc2.common.ConsoleJsonOutput;
import es.tododev.sc2.process.Dto;

public class ConsoleJsonOutputTest {

	@Test
	public void canSerialize() throws JsonProcessingException {
		ConsoleJsonOutput output = new ConsoleJsonOutput();
		Dto dto = new Dto(123456789, new BigDecimal("1234.567890"));
		String json = output.toJson(dto);
		assertEquals("{\"data\":{\"timestamp\":123456789,\"amount\":\"1234.567890\"}}", json);
	}
	
}
