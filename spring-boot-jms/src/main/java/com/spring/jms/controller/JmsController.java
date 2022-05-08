package com.spring.jms.controller;


import com.spring.jms.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.jms.JMSException;
import javax.validation.Valid;

@RestController
@RequestMapping("jms/v1")
@RequiredArgsConstructor
@Tag(description = "JMS API", name = "JMS Services")
public class JmsController {

    private final MessageService messageService;

    @PostMapping("send")
    @Operation(summary = "Send message", description = "Send a message",
                responses = {
                        @ApiResponse(responseCode = "200", description = "Returns 200 if message successfully sent",
                                content = @Content(schema = @Schema(example = "{\"status\": \"ok\", ")))
                }
    )
    public void sendMessage(@Valid @RequestParam String message) {
        messageService.sendMessage(message);
    }

    @GetMapping("ping")
    @Operation(summary = "Ping", description = "Ping",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns 200 if ping was successful",
                            content = @Content(schema = @Schema(example = "{\"status\": \"ok\", ")))
            }
    )
    public void ping() {
        messageService.ping();
    }


    @PostMapping("sendAndReceive")
    @Operation(summary = "Send message, wait for reply", description = "Send a message",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns 200 if message successfully sent",
                            content = @Content(schema = @Schema(example = "{\"status\": \"ok\", ")))
            }
    )
    public String sendAndReceive(@Valid @RequestParam String message) throws JMSException {
        return messageService.sendAndReceive(message);
    }

}
