package com.fs.common.core.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"serversInitialized"})
public class ProjectData {
    private String name;
    private Servers servers;
    private String bannedWord;

    private boolean serversInitialized = false;

    public void setServers(Servers servers) {
        this.servers = servers;
        this.serversInitialized = true;
    }
}