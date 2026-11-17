package com.github.isaacadams135.subnettoolkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SubnetTest {

    @Test
    void slash25SplitsNetworkAndBroadcast() {
        Subnet subnet = Subnet.parse("192.168.1.130/25");

        assertEquals("192.168.1.128", Subnet.format(subnet.network()));
        assertEquals("192.168.1.255", Subnet.format(subnet.broadcast()));
        assertEquals("192.168.1.129", Subnet.format(subnet.firstHost()));
        assertEquals("192.168.1.254", Subnet.format(subnet.lastHost()));
        assertEquals(128, subnet.totalAddresses());
        assertEquals(126, subnet.usableHosts());
    }

    @Test
    void slash24Gives254UsableHosts() {
        Subnet subnet = Subnet.parse("10.0.0.0/24");

        assertEquals(256, subnet.totalAddresses());
        assertEquals(254, subnet.usableHosts());
        assertEquals("10.0.0.1", Subnet.format(subnet.firstHost()));
        assertEquals("10.0.0.254", Subnet.format(subnet.lastHost()));
    }

    @Test
    void slash30GivesTwoUsableHosts() {
        Subnet subnet = Subnet.parse("203.0.113.4/30");

        assertEquals(4, subnet.totalAddresses());
        assertEquals(2, subnet.usableHosts());
    }

    @Test
    void slash31UsesBothAddresses() {
        Subnet subnet = Subnet.parse("192.0.2.0/31");

        assertEquals(2, subnet.totalAddresses());
        assertEquals(2, subnet.usableHosts());
        assertEquals(subnet.network(), subnet.firstHost());
        assertEquals(subnet.broadcast(), subnet.lastHost());
    }

    @Test
    void slash32IsASingleAddress() {
        Subnet subnet = Subnet.parse("198.51.100.9/32");

        assertEquals(1, subnet.totalAddresses());
        assertEquals(1, subnet.usableHosts());
        assertEquals(subnet.address(), subnet.firstHost());
        assertEquals(subnet.address(), subnet.lastHost());
        assertEquals(subnet.address(), subnet.network());
    }

    @Test
    void computesNetmaskAndWildcard() {
        Subnet subnet = Subnet.parse("192.168.1.130/25");

        assertEquals("255.255.255.128", Subnet.format(subnet.netmask()));
        assertEquals("0.0.0.127", Subnet.format(subnet.wildcard()));
    }

    @Test
    void bareIpDefaultsToSlash32() {
        assertEquals(32, Subnet.parse("192.0.2.1").prefix());
        assertEquals(32, Subnet.parse(" 192.0.2.1 ").prefix());
    }

    @Test
    void rejectsBadInput() {
        assertThrows(IllegalArgumentException.class, () -> Subnet.parse("1.2.3"));
        assertThrows(IllegalArgumentException.class, () -> Subnet.parse("999.1.1.1"));
        assertThrows(IllegalArgumentException.class, () -> Subnet.parse("10.0.0.0/33"));
        assertThrows(IllegalArgumentException.class, () -> Subnet.parse("10.0.0.x"));
        assertThrows(IllegalArgumentException.class, () -> Subnet.parse("abc"));
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> Subnet.parse("2001:db8::1"));
        assertTrue(e.getMessage().contains("only IPv4"));
    }
}
