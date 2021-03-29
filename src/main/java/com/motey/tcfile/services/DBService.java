package com.motey.tcfile.services;

import com.motey.tcfile.mapper.ComponentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBService {

	@Autowired
	ComponentMapper componentMapper;

	public ComponentMapper getComponentMapper() {
		return componentMapper;
	}
}
