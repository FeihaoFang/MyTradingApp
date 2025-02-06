package com.example.myapp.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ExecutionTimeLogger {
    /**
     * An AOP method that logs the execution time of the target method.
     * <p>
     * This method obtains information about the target method via the {@code ProceedingJoinPoint}.
     * It records the start time before the method execution, proceeds with the method call,
     * and finally calculates and logs the execution duration after the method completes.
     * </p>
     *
     * @param joinPoint the join point providing details about the target method and its arguments
     * @return the result of the target method execution
     * @throws Throwable if the target method throws an exception, it will be propagated
     */
    @Around("execution(* com.example.myapp.service.KlineDataLoadService.*LoadData(..)) ||" +
            "execution(* com.example.myapp.service.KlineDataRetrieveService.*(..))"
    )
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable{
        String methodName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();
        System.out.println("Start executing: " + methodName);
        Object result;
        try {
            result = joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            System.out.println("Execution of " + methodName + " took " + duration + " ms.");
        }
        return result;


    }

}
