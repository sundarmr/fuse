package com.ally.redhat.poc.rest;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
@RolesAllowed(value = { "Manager" })
public class AllyRestApplication extends Application{
	 public Set<Class<?>> getClasses() {
		 final Set<Class<?>> classes = new HashSet<>();
		 classes.add(CustomerServiceImpl.class);
		 return classes;
	 }
}
