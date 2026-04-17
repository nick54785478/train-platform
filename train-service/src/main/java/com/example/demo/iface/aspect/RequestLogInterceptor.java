package com.example.demo.iface.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

/**
 * 用於日誌紀錄與性能監控的攔截器
 */
@Slf4j
@Aspect
@Component
public class RequestLogInterceptor {

	/**
	 * 定義切入點，針對帶有 @RestController 注解的類進行切入。
	 */
	@Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
	public void pointCut() {
	}

	/**
	 * 用於監控方法執行時間。
	 * 
	 * @param joinPoint 切入點
	 * @return 方法執行結果
	 * @throws Throwable 例外
	 */
	@Around("pointCut()")
	public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
		long start = System.currentTimeMillis();
		Object proceed = joinPoint.proceed();
		long executionTime = System.currentTimeMillis() - start;
		log.info("{} executed in {} ms", joinPoint.getSignature(), executionTime);
		return proceed;
	}

	/**
	 * 記錄方法執行前的訊息。
	 * 
	 * @param joinPoint 切入點
	 */
	@Before("pointCut()")
	public void before(JoinPoint joinPoint) {
		ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		String uri = ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString();
		log.info("URI: {}", uri);
		String method = (servletRequestAttributes != null) ? servletRequestAttributes.getRequest().getMethod() : "";

		log.info("Method: {}", method);
	}

	/**
	 * 進入 Controller 後執行
	 * 
	 * @param joinPoint 切入點
	 */
	@After("pointCut()")
	public void after(JoinPoint joinPoint) {
	}
}
