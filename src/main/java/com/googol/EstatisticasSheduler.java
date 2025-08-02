package com.googol;

import com.googol.rmi.BarrelsInfo;
import com.googol.rmi.Gateway_I;
import com.googol.rmi.Pesquisas;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class EstatisticasSheduler {

    @Autowired
    private Gateway_I gateway;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 5000)
    public void enviarInfoBarrels() {
        try {
            List<BarrelsInfo> infos = gateway.getBarrelsInfo();
            messagingTemplate.convertAndSend("/topicos/barrels", infos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Scheduled(fixedRate = 5000)
    public void enviarTop10Pesquisas() {
        try {
            List<Pesquisas> top = gateway.getTop10Pesquisas();
            messagingTemplate.convertAndSend("/topicos/top10", top);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
