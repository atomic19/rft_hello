package com.rft.tone.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Configuration {
    private List<HostConfig> hosts;
}
