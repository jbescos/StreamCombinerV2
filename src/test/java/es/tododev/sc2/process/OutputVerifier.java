package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;



public class OutputVerifier implements IOutput {
	
	private final List<Dto> total = new ArrayList<>();

	@Override
	public void process(List<Dto> transactions) {
		total.addAll(transactions);
	}
	
	public void verify(String expectedAmount) {
		for(int i=0;i<total.size()-1;i++) {
			Long current = total.get(i).getTimestamp();
			Long next = total.get(i+1).getTimestamp();
			int compare = current.compareTo(next);
			assertEquals("Check "+next+": "+total.toString(), -1, compare);
		}
		BigDecimal totalAmount = new BigDecimal("0.0");
		for(Dto dto : total) {
			totalAmount = totalAmount.add(dto.getAmount());
		}
		assertEquals(total.toString(), new BigDecimal(expectedAmount), totalAmount);
	}

}
