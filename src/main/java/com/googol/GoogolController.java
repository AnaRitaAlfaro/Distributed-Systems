package com.googol;

import com.googol.rmi.BarrelsInfo;
import com.googol.rmi.Gateway_I;
import com.googol.rmi.PageInfo;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class GoogolController {

    private final Gateway_I gateway;

    public GoogolController(Gateway_I gateway) {
        this.gateway=gateway;

    }


    @GetMapping("/pesquisar")
    public String pesquisar(@RequestParam String termo,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        int pageSize = 10;
        try {
            gateway.registarTermo(termo);
            List<PageInfo> resultados = gateway.pesquisaBarrel(termo);
            int totalResultados = resultados.size();
            int totalPages = (int) Math.ceil((double) totalResultados / pageSize);

            int start = page * pageSize;
            int end = Math.min(start + pageSize, totalResultados);

            List<PageInfo> paginaAtual = resultados.subList(start, end);

            model.addAttribute("resultados", paginaAtual);
            model.addAttribute("termo", termo);
            model.addAttribute("page", page);
            model.addAttribute("totalPages", totalPages);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("erro", "Erro ao processar a pesquisa: " + e.getMessage());
            model.addAttribute("resultados", Collections.emptyList());
        }
        return "resultados";
    }



    @GetMapping("/search")
    public String mostrarPesquisa() {
        return "search";
    }
    @GetMapping("/")
    public String home() {
        return "menu";
    }

    @GetMapping("/index")
    public String mostrarIndexar() {
        return "indexar";
    }
    @PostMapping("/index")
    public String indexarURL(@RequestParam String url, Model model) {
        try {
            gateway.addURlQueue(url);
            model.addAttribute("msg", "URL adicionada à fila com sucesso: " + url);
        } catch (Exception e) {
            model.addAttribute("msg", "Erro ao indexar URL: " + e.getMessage());
        }
        return "mensagem";
    }

    @GetMapping("/estatisticas")
    public String mostrarEstatisticas() {
        return "estatisticas";
    }



}
