package es.tododev.sc2.process;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
public class Dto {

	public final static Dto LAST_TO_SEND = new Dto(Long.MAX_VALUE, new BigDecimal("0.0"));
	public final static Dto FIRST_TO_SEND = new Dto(0, new BigDecimal("0.0"));
	private long timestamp;
	private BigDecimal amount;
	
	public Dto() {
		
	}
	
	public Dto(long timestamp, BigDecimal amount) {
		this.timestamp = timestamp;
		this.amount = amount;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "<timestamp=" + timestamp + ", amount=" + amount + ">";
	}
	
	
}
