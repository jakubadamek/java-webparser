package com.jakubadamek.robotemil.services;

import org.aspectj.lang.annotation.Pointcut;

public class AopPointcut {
	@Pointcut("execution(* com.jakubadamek.robotemil.services.Jdbc*.*(..))")
	public void anyServiceMethod() { /* */ }
	
	@Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
	public void transactional() { /* */ }
	
	@Pointcut("anyServiceMethod() && transactional()")
	public void txPointcut() { /* */ }	
}
