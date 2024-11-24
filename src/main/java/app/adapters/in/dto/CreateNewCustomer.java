package app.adapters.in.dto;

import lombok.Getter;

@Getter
public class CreateNewCustomer {
    private String name;
    private String email;
    private boolean privileges;
}
