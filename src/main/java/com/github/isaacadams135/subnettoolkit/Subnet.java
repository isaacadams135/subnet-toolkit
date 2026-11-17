package com.github.isaacadams135.subnettoolkit;

/**
 * An IPv4 address with a prefix length, plus the network math derived from
 * both. Everything is plain 32-bit int arithmetic; no libraries involved.
 */
public record Subnet(int address, int prefix) {

    public Subnet {
        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("prefix out of range 0-32: /" + prefix);
        }
    }

    /**
     * Parses "192.168.1.130/25" or a bare address (treated as /32).
     */
    public static Subnet parse(String input) {
        String s = input.trim();
        int slash = s.indexOf('/');
        if (slash < 0) {
            return new Subnet(parseAddress(s), 32);
        }
        String prefixPart = s.substring(slash + 1);
        int prefix;
        try {
            prefix = Integer.parseInt(prefixPart);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("bad prefix length: '" + prefixPart + "'");
        }
        return new Subnet(parseAddress(s.substring(0, slash)), prefix);
    }

    /**
     * Parses a dotted-quad into a 32-bit int. Strict: exactly four decimal
     * octets, no leading signs, no hex, nothing between 0 and 255.
     */
    static int parseAddress(String s) {
        if (s.indexOf(':') >= 0) {
            throw new IllegalArgumentException("only IPv4 for now: " + s);
        }
        String[] parts = s.split("\\.", -1);
        if (parts.length != 4) {
            throw new IllegalArgumentException("not a valid IPv4 address: " + s);
        }
        int addr = 0;
        for (String part : parts) {
            if (part.isEmpty() || !part.chars().allMatch(c -> c >= '0' && c <= '9')) {
                throw new IllegalArgumentException("not a valid IPv4 address: " + s);
            }
            int octet = Integer.parseInt(part);
            if (octet > 255) {
                throw new IllegalArgumentException("octet out of range in: " + s);
            }
            addr = (addr << 8) | octet;
        }
        return addr;
    }

    /**
     * Renders a 32-bit int as dotted quad. The int is signed in Java, so we
     * widen to long first - otherwise >>> on a negative value still works but
     * mixing it with plain int math is an easy way to print 4294967295.x.
     */
    public static String format(int addr) {
        long v = addr & 0xffffffffL;
        return (v >>> 24) + "." + ((v >>> 16) & 0xff) + "." + ((v >>> 8) & 0xff) + "." + (v & 0xff);
    }

    public int netmask() {
        // Java shifts are mod 32, so << 32 would be a no-op; handle /0 by hand
        return prefix == 0 ? 0 : (0xffffffff << (32 - prefix));
    }

    public int wildcard() {
        return ~netmask();
    }

    public int network() {
        return address & netmask();
    }

    public int broadcast() {
        return network() | wildcard();
    }

    public int firstHost() {
        // /31 and /32 have no network/broadcast split (RFC 3021)
        if (prefix >= 31) {
            return network();
        }
        return network() + 1;
    }

    public int lastHost() {
        if (prefix >= 31) {
            return broadcast();
        }
        return broadcast() - 1;
    }

    public long totalAddresses() {
        return 1L << (32 - prefix);
    }

    public long usableHosts() {
        if (prefix >= 31) {
            return totalAddresses();
        }
        return totalAddresses() - 2;
    }
}
