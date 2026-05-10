package com.taxi.trip.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String licenseNumber;
    private String status;
}