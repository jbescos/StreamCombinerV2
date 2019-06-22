package es.tododev.sc2.common;

import java.util.List;

import es.tododev.sc2.process.Dto;

public interface IOutput {

	void process(List<Dto> transactions);
	
}
