package es.tododev.sc2.common;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import es.tododev.sc2.process.Dto;

public class XmlInput implements IInput<Dto> {

	private final JAXBContext jc;
	
	public XmlInput(){
		try {
			jc = JAXBContext.newInstance(Dto.class);
		} catch (JAXBException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	@Override
	public Dto toObject(String input) throws StreamCombinerException {
		try {
			Unmarshaller unmarsaller = jc.createUnmarshaller();
			StringReader reader = new StringReader(input);
			StreamSource stream = new StreamSource(reader);
			return unmarsaller.unmarshal(stream, Dto.class).getValue();
		} catch (JAXBException e) {
			throw new StreamCombinerException(ErrorCodes.DESERIALIZE_INPUT, e);
		}
		
	}

}
