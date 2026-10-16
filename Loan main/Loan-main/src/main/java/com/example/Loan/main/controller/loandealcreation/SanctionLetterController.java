package com.example.Loan.main.controller.loandealcreation;

import com.example.Loan.main.dto.loandealcreation.response.ResponseApi;
import com.example.Loan.main.dto.loandealcreation.response.SanctionLetterResponse;
import com.example.Loan.main.service.loandealcreation.SanctionLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sanction-letters")
@RequiredArgsConstructor
public class SanctionLetterController {

    private final SanctionLetterService service;

    @PostMapping("/deals/{dealId}")
    public ResponseApi<SanctionLetterResponse> generate(
            @PathVariable("dealId") Integer dealId
    ) {
        return ResponseApi.success(
                "Sanction letter ready",
                service.generate(dealId)
        );
    }

    @GetMapping("/{sanctionId}")
    public ResponseApi<SanctionLetterResponse> getById(
            @PathVariable("sanctionId") Integer sanctionId
    ) {
        return ResponseApi.success(
                "Sanction letter fetched successfully",
                service.getById(sanctionId)
        );
    }

    @GetMapping("/deal/{dealId}")
    public ResponseApi<SanctionLetterResponse> getByDealId(
            @PathVariable("dealId") Integer dealId
    ) {
        return ResponseApi.success(
                "Loan application sanction letter fetched",
                service.getByDealId(dealId)
        );
    }

    @GetMapping("/{sanctionId}/pdf")
    public ResponseEntity<byte[]> download(
            @PathVariable("sanctionId") Integer sanctionId
    ) {
        byte[] pdf = service.downloadPdf(sanctionId);

        String disposition = ContentDisposition.attachment()
                .filename("sanction-" + sanctionId + ".pdf")
                .build()
                .toString();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition
                )
                .contentLength(pdf.length)
                .body(pdf);
    }
}