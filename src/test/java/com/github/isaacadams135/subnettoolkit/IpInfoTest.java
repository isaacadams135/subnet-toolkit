package com.github.isaacadams135.subnettoolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class IpInfoTest {

    @Test
    void classifiesSpecialUseAddresses() {
        assertEquals("loopback", IpInfo.classify(Subnet.parseAddress("127.0.0.1")));
        assertEquals("private", IpInfo.classify(Subnet.parseAddress("10.0.0.5")));
        assertEquals("private", IpInfo.classify(Subnet.parseAddress("172.16.0.1")));
        assertEquals("private", IpInfo.classify(Subnet.parseAddress("192.168.1.1")));
        assertEquals("link-local", IpInfo.classify(Subnet.parseAddress("169.254.1.1")));
        assertEquals("CGNAT", IpInfo.classify(Subnet.parseAddress("100.64.0.1")));
        assertEquals("multicast", IpInfo.classify(Subnet.parseAddress("224.0.0.1")));
        assertEquals("unspecified", IpInfo.classify(Subnet.parseAddress("0.0.0.0")));
        assertEquals("reserved", IpInfo.classify(Subnet.parseAddress("240.0.0.1")));
        assertEquals("global", IpInfo.classify(Subnet.parseAddress("8.8.8.8")));
    }
}
