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
public class Slot {

    String slotTime;

    String name;

    String status;

    String slotAssignedBy;

    String slotAssignedTo;

    String type;

    String meetingLink;
}
