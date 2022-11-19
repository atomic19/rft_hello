package com.rft.tone.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class HostConfig {
    private String name;
    private int port;
    private String dbName;
}
