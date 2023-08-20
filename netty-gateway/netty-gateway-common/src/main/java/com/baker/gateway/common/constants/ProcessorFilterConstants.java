package com.baker.gateway.common.constants;

/**
 * 所有过滤器常量配置定义
 */
public interface ProcessorFilterConstants {

	/***************** 前置过滤器 *****************/

	String TIMEOUT_PRE_FILTER_ID = "timeoutPreFilter";
	String TIMEOUT_PRE_FILTER_NAME = "超时过滤器";
	int TIMEOUT_PRE_FILTER_ORDER = 2100;

	String LOADBALANCE_PRE_FILTER_ID = "loadBalancePreFilter";
	String LOADBALANCE_PRE_FILTER_NAME = "负载均衡前置过滤器";
	int LOADBALANCE_PRE_FILTER_ORDER = 2000;




	/***************** 路由(中置)过滤器 *****************/

	String HTTP_ROUTE_FILTER_ID = "httpRouteFilter";
	String HTTP_ROUTE_FILTER_NAME = "httpRouteFilter";
	int HTTP_ROUTE_FILTER_ORDER = 5000;
	
	
	String DUBBO_ROUTE_FILTER_ID = "dubboRouteFilter";
	String DUBBO_ROUTE_FILTER_NAME = "dubboRouteFilter";
	int DUBBO_ROUTE_FILTER_ORDER = 5000;




	/***************** 后置过滤器 *****************/


	String STATISTICS_POST_FILTER_ID = "statisticsPostFilter";
	String STATISTICS_POST_FILTER_NAME = "最后的统计分析过滤器";
	int STATISTICS_POST_FILTER_ORDER = Integer.MAX_VALUE;





	/***************** 异常置过滤器 *****************/

	String DEFAULT_ERROR_FILTER_ID = "defaultErrorFilter";
	String DEFAULT_ERROR_FILTER_NAME = "默认的异常处理过滤器";
	int DEFAULT_ERROR_FILTER_ORDER = 20000;

}
