package app.adapters.in.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TransactionResponse {
    private String message;
    private UUID transactionId;
}
