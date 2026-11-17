package com.github.isaacadams135.subnettoolkit;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Main {

    private static final String USAGE = """
        usage: subnet-toolkit <command> [args]

        commands:
          calc <cidr>       show network details for a CIDR, e.g. 192.168.1.130/25
                            (a bare address is treated as /32)
          ip <address>      classify an IPv4 address
          dns <name>        resolve a name to its A/AAAA records
          -h, --help        show this help

        exit codes:
          0  success
          1  lookup failure (dns could not resolve the name)
          2  bad input or usage
        """;

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("error: missing command");
            System.err.println(USAGE);
            System.exit(2);
            return;
        }
        switch (args[0]) {
            case "-h", "--help", "help" -> System.out.println(USAGE);
            case "calc" -> calc(args);
            case "ip" -> ip(args);
            case "dns" -> dns(args);
            default -> {
                System.err.println("error: unknown command: " + args[0]);
                System.err.println(USAGE);
                System.exit(2);
            }
        }
    }

    private static void calc(String[] args) {
        if (args.length != 2) {
            System.err.println("error: calc needs a CIDR, e.g. 192.168.1.0/24");
            System.exit(2);
            return;
        }
        Subnet subnet;
        try {
            subnet = Subnet.parse(args[1]);
        } catch (IllegalArgumentException e) {
            System.err.println("error: " + e.getMessage());
            System.exit(2);
            return;
        }
        System.out.printf("address         %s%n", Subnet.format(subnet.address()));
        System.out.printf("prefix          /%d%n", subnet.prefix());
        System.out.printf("network         %s%n", Subnet.format(subnet.network()));
        System.out.printf("netmask         %s%n", Subnet.format(subnet.netmask()));
        System.out.printf("wildcard        %s%n", Subnet.format(subnet.wildcard()));
        System.out.printf("broadcast       %s%n", Subnet.format(subnet.broadcast()));
        System.out.printf("first host      %s%n", Subnet.format(subnet.firstHost()));
        System.out.printf("last host       %s%n", Subnet.format(subnet.lastHost()));
        System.out.printf("total addresses %d%n", subnet.totalAddresses());
        System.out.printf("usable hosts    %d%n", subnet.usableHosts());
    }

    private static void ip(String[] args) {
        if (args.length != 2) {
            System.err.println("error: ip needs an address, e.g. 192.168.1.1");
            System.exit(2);
            return;
        }
        int addr;
        try {
            addr = Subnet.parseAddress(args[1].trim());
        } catch (IllegalArgumentException e) {
            System.err.println("error: " + e.getMessage());
            System.exit(2);
            return;
        }
        System.out.printf("classification %s%n", IpInfo.classify(addr));
        try {
            String name = InetAddress.getByName(args[1].trim()).getHostName();
            if (!name.equals(args[1].trim()) && !name.equals(Subnet.format(addr))) {
                System.out.printf("reverse DNS    %s%n", name);
            }
        } catch (UnknownHostException | SecurityException e) {
            // no PTR record or DNS is unreachable - classification still stands
        }
    }

    private static void dns(String[] args) {
        if (args.length != 2) {
            System.err.println("error: dns needs a name, e.g. example.com");
            System.exit(2);
            return;
        }
        String name = args[1];
        InetAddress[] found;
        try {
            found = InetAddress.getAllByName(name);
        } catch (UnknownHostException e) {
            System.err.println("error: could not resolve '" + name + "'");
            System.exit(1);
            return;
        }
        for (InetAddress inet : found) {
            String type = inet instanceof Inet6Address ? "AAAA" : "A";
            System.out.printf("%s %s%n", type, inet.getHostAddress());
        }
    }
}
