package com.conu.findhelp.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;


@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSlotRequest {
    String date;
    String assignedBy;
    String status;
    String timeSlot;
}
