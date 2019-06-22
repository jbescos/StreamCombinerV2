package es.tododev.sc2.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.tododev.sc2.process.Dto;

public class ConsoleJsonOutput implements IOutput {

	private final ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public void process(List<Dto> transactions) {
		StringBuilder builder = new StringBuilder();
		for(Dto transaction : transactions) {
			try {
				String json = toJson(transaction);
				builder.append("\n").append(json);
			} catch (JsonProcessingException e) {
				builder.append("\n").append(ErrorCodes.DESERIALIZE_OUTPUT.getCode() + ": " + ErrorCodes.DESERIALIZE_OUTPUT.getMessage() + ". " + transaction);
			}
		}
		System.out.println(builder.toString());
		
	}
	
	String toJson(Dto dto) throws JsonProcessingException {
		Map<String, DtoDecorated> map = new HashMap<>();
		map.put("data", DtoDecorated.toDtoDecorated(dto));
		String json = mapper.writeValueAsString(map);
		return json;
	}
	
	// Need this because Dto is serialized as '{"timestamp":0,"amount":0.00}' but we need {"timestamp":0,"amount":"0.00"}
	private static class DtoDecorated {
		
		private long timestamp;
		private String amount;
		
		private static DtoDecorated toDtoDecorated(Dto dto) {
			DtoDecorated decorated = new DtoDecorated();
			decorated.timestamp = dto.getTimestamp();
			decorated.amount = dto.getAmount().toString();
			return decorated;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
		public String getAmount() {
			return amount;
		}
		public void setAmount(String amount) {
			this.amount = amount;
		}
		
	}

}
