package com.example.Loan.main.entity.cibilEntity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "cibilreports")
@Data

public class CibilReports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CibilReportId")
    private Long  CibilReportId;
    @OneToOne
    @JoinColumn(name = "CustomerId", nullable = false, unique = true)
    private Customer customer;
    @Column(name ="PanNo")
    private String PanNo;
    @Column(name ="CibilScore")
    private Integer CibilScore;
    @Column(name ="CheckDate")
    private LocalDateTime CheckDate;
    @Column(name ="Status")
    private String Status;

}
