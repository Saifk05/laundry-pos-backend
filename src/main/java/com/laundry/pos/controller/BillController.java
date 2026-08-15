package com.laundry.pos.controller;

import com.laundry.pos.response.BillListResponse;
import com.laundry.pos.service.BillService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin
public class BillController {

    private final BillService billService;

    public BillController(
            BillService billService
    ) {

        this.billService =
                billService;
    }


    @GetMapping
    public ResponseEntity<BillListResponse>
    getBills() {

        return ResponseEntity.ok(
                billService
                        .getBills()
        );
    }
}