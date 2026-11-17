package com.github.isaacadams135.subnettoolkit;

/**
 * Special-use IPv4 classification (RFC 1918, RFC 3927, RFC 6598, etc.).
 * Order matters: 127/8 is checked before the private ranges so we never call
 * loopback "private".
 */
public final class IpInfo {

    private IpInfo() {
    }

    public static String classify(int addr) {
        if ((addr >>> 24) == 127) {
            return "loopback";
        }
        if ((addr & 0xff000000) == 0x0a000000                       // 10.0.0.0/8
                || (addr & 0xfff00000) == 0xac100000                // 172.16.0.0/12
                || (addr & 0xffff0000) == 0xc0a80000) {             // 192.168.0.0/16
            return "private";
        }
        if ((addr & 0xffff0000) == 0xa9fe0000) {                    // 169.254.0.0/16
            return "link-local";
        }
        if ((addr & 0xffc00000) == 0x64400000) {                    // 100.64.0.0/10
            return "CGNAT";
        }
        if ((addr & 0xf0000000) == 0xe0000000) {                    // 224.0.0.0/4
            return "multicast";
        }
        if (addr == 0) {
            return "unspecified";
        }
        if ((addr & 0xf0000000) == 0xf0000000) {                    // 240.0.0.0/4
            return "reserved";
        }
        return "global";
    }
}
