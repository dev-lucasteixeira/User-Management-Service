package com.lucasteixeira.business.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailDTO {
    private String emailTo;
    private String subject;
    private String text;
}
