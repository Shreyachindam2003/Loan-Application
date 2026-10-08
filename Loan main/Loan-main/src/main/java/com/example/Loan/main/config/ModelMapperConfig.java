package com.example.Loan.main.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.Loan.main.dto.EmiPaymentDto.response.PenaltyDetailResponse;
import com.example.Loan.main.entity.EmiPaymentEntity.PenaltyCharge;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper=new ModelMapper();

        modelMapper.typeMap(PenaltyCharge.class,PenaltyDetailResponse.class)
                .addMapping(PenaltyCharge::getPenaltyAmount,PenaltyDetailResponse::setAmount);

        return modelMapper;
    }
}