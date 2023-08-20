package com.baker.gateway.common.metric;

public class MetricException extends Throwable {

	private static final long serialVersionUID = 4047603330589432718L;

    private Integer code;

    public MetricException(String err){
        super(err);
    }

    public MetricException(String err, Throwable e){
        super(err,e);
    }

    public MetricException(MetricCodeEnum metricCodeEnum, Throwable cause) {
        super(metricCodeEnum.getMsg(), cause);
        this.code = metricCodeEnum.getCode();
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
