package org.kehl.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;
import org.kehl.entity.SwAlarmDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * springcloud-alibaba
 *
 * @author : kehl
 * @date : 2022-05-20 18:45
 **/
@RestController
@RequestMapping("gatewaySentinel")
@Slf4j
public class GatewaySentinelController {
    @RequestMapping("/alarmnotify")
    public void notify(@RequestBody List<SwAlarmDTO> alarmDTOList){
        String content=getContent(alarmDTOList);
        log.info("告警邮件已发送..."+content);
    }

    private String getContent(List<SwAlarmDTO>  alarmDTOList) {
        StringBuilder result = new StringBuilder();
        for (SwAlarmDTO dto : alarmDTOList) {
            result.append("scopeId: ").append(dto.getScopeId())
                    .append("\nscope: ").append(dto.getScope())
                    .append("\n目标Scope 的实体名称:").append(dto.getName()).append("\nScope 实体的ID: ").append(dto.getId0())
                    .append("\nid1: ").append(dto.getId1())
                    .append("\n告警规则名称: ").append(dto.getRuleName())
                    .append("\n告警消息内容: ").append(dto.getAlarmMessage()).append("In告警时间: ").append(dto.getStartTime())
                    .append("\n标签: ").append(dto.getTags())
                    .append("\n\n---------------\n\n");
        }
        return result.toString();
    }
}
