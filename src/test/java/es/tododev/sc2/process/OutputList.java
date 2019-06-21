package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;



public class OutputList implements IOutput {
	
	private final List<Dto> total = new ArrayList<>();

	@Override
	public void process(List<Dto> transactions) {
		total.addAll(transactions);
	}
	
	public void verify(String expectedAmount) {
		for(int i=0;i<total.size()-1;i++) {
			BigDecimal current = total.get(i).getAmount();
			BigDecimal next = total.get(i+1).getAmount();
			int compare = next.compareTo(current);
			assertTrue(total.toString(), compare == 1);
		}
		Optional<BigDecimal> totalAmount = total.stream().map(dto -> dto.getAmount()).reduce((amount1, amount2) -> amount1.add(amount2));
		if(totalAmount.isPresent()) {
			assertEquals(total.toString(), new BigDecimal(expectedAmount), totalAmount.get());
		}
	}

}
