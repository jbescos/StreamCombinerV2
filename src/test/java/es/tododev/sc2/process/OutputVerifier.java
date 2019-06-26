package es.tododev.sc2.process;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;

import es.tododev.sc2.common.IOutput;



public class OutputVerifier implements IOutput {
	
	private final List<Dto> total = Collections.synchronizedList(new ArrayList<>());

	@Override
	public void process(List<Dto> transactions) {
		total.addAll(transactions);
		System.out.println("Output "+transactions);
	}
	
	public void verify(String expectedAmount) {
		String totalStr = total.toString();
		boolean failed = false;
		Long next = null;
		for(int i=0;i<total.size()-1;i++) {
			Long current = total.get(i).getTimestamp();
			next = total.get(i+1).getTimestamp();
			int compare = current.compareTo(next);
			if(-1 != compare) {
				failed = true;
				break;
			}
		}
		Assert.assertFalse("Check "+next+": "+totalStr, failed);
		BigDecimal totalAmount = new BigDecimal("0.0");
		for(Dto dto : total) {
			totalAmount = totalAmount.add(dto.getAmount());
		}
		assertEquals(totalStr, new BigDecimal(expectedAmount), totalAmount);
	}

}
