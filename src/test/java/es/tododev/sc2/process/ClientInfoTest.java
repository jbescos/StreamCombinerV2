package es.tododev.sc2.process;

import java.math.BigDecimal;
import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;

import es.tododev.sc2.common.CompareCache;
import es.tododev.sc2.common.StreamCombinerException;
import es.tododev.sc2.process.ClientInfo;
import es.tododev.sc2.process.Dto;
import es.tododev.sc2.process.IClientInfo;
import es.tododev.sc2.process.IStreamProcessor;

public class ClientInfoTest {
	
	@Mock
	private IStreamProcessor streamProcessor;
	private Comparator<Long> comparatorCache = new CompareCache();
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void nullDto() throws StreamCombinerException {
		IClientInfo clientInfo = new ClientInfo(streamProcessor, comparatorCache);
		clientInfo.add(null);
		Mockito.verify(streamProcessor, Mockito.never()).push(Matchers.any(Dto.class), Matchers.eq(clientInfo));
	}
	
	@Test(expected = StreamCombinerException.class)
	public void obsolete() throws StreamCombinerException {
		IClientInfo clientInfo = new ClientInfo(streamProcessor, comparatorCache);
		clientInfo.add(new Dto(1, new BigDecimal(1)));
		Mockito.verify(streamProcessor, Mockito.never()).push(Matchers.any(Dto.class), Matchers.eq(clientInfo));
		clientInfo.add(new Dto(0, new BigDecimal(1)));
	}
	
	@Test
	public void fullExample() throws StreamCombinerException {
		IClientInfo clientInfo = new ClientInfo(streamProcessor, comparatorCache);
		clientInfo.add(new Dto(1, new BigDecimal(1)));
		Mockito.verify(streamProcessor, Mockito.never()).push(Matchers.any(Dto.class), Matchers.eq(clientInfo));
		Dto dto = new Dto(1, new BigDecimal(1));
		clientInfo.add(dto);
		Mockito.verify(streamProcessor, Mockito.never()).push(Matchers.any(Dto.class), Matchers.eq(clientInfo));
		Dto dto2 = new Dto(2, new BigDecimal(1));
		clientInfo.add(dto2);
		Mockito.verify(streamProcessor, Mockito.times(1)).push(Matchers.eq(dto), Matchers.eq(clientInfo));
		assertEquals(2, dto.getAmount().intValue());
		clientInfo.add(Dto.LAST_TO_SEND);
		Mockito.verify(streamProcessor, Mockito.times(1)).push(Matchers.eq(dto2), Matchers.eq(clientInfo));
		Mockito.verify(streamProcessor, Mockito.times(1)).push(Matchers.eq(Dto.LAST_TO_SEND), Matchers.eq(clientInfo));
	}
	
}
