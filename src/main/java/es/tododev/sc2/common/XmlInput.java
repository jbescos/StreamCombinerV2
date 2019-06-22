package es.tododev.sc2.common;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import es.tododev.sc2.process.Dto;

public class XmlInput implements IInput<Dto> {

	@Override
	public Dto toObject(String input) throws StreamCombinerException {
		try {
			JAXBContext jc = JAXBContext.newInstance(Dto.class);
			Unmarshaller unmarsaller = jc.createUnmarshaller();
			StringReader reader = new StringReader(input);
			StreamSource stream = new StreamSource(reader);
			return unmarsaller.unmarshal(stream, Dto.class).getValue();
		} catch (JAXBException e) {
			throw new StreamCombinerException(ErrorCodes.DESERIALIZE_INPUT, e);
		}
		
	}

}
