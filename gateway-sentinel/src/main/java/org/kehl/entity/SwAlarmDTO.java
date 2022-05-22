package org.kehl.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * springcloud-alibaba
 * skywalking 告警类
 *
 * @author : kehl
 * @date : 2022-05-22 16:38
 **/
@Getter
@Setter
public class SwAlarmDTO {
    private int scopeId;
    private String scope;
    private String name;
    private String id0;
    private String id1;
    private String ruleName;
    private String alarmMessage;
    private List<Tag> tags;
    private long startTime;
    private transient int period;
    private transient boolean onlyAsCondition;

    @Data
    public static class Tag{
        private String key;
        private String value;
    }
}
