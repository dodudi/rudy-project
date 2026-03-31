package com.commerce.ui.controller;

import com.commerce.ui.client.SettlementClient;
import com.commerce.ui.dto.SettlementFilterRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/settlement")
public class SettlementController {

    private final SettlementClient settlementClient;

    public SettlementController(SettlementClient settlementClient) {
        this.settlementClient = settlementClient;
    }

    @GetMapping("/records")
    public String records(@ModelAttribute SettlementFilterRequest filter, Model model) {
        model.addAttribute("records", settlementClient.getRecords(filter));
        model.addAttribute("filter", filter);
        return "settlement/records";
    }

    @GetMapping("/daily")
    public String daily(@RequestParam(required = false) Long sellerId,
                        @RequestParam(required = false) String settlementDate,
                        Model model) {
        model.addAttribute("dailySettlements", settlementClient.getDailySettlements(sellerId, settlementDate));
        model.addAttribute("sellerId", sellerId);
        model.addAttribute("settlementDate", settlementDate != null ? settlementDate : "");
        return "settlement/daily";
    }

    @PostMapping("/trigger")
    public String trigger(@RequestParam(required = false) String settlementDate,
                          RedirectAttributes redirectAttributes) {
        String targetDate = (settlementDate != null && !settlementDate.isBlank())
                ? settlementDate
                : LocalDate.now().minusDays(1).toString();
        try {
            var result = settlementClient.triggerJob(targetDate);
            redirectAttributes.addFlashAttribute("success",
                    "배치 실행됨 — jobId: " + result.get("jobId") + " (" + targetDate + ")");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "배치 실행 실패: " + e.getMessage());
        }
        return "redirect:/settlement/daily";
    }
}
