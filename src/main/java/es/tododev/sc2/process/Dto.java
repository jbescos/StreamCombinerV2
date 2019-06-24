package es.tododev.sc2.process;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
public class Dto {

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
