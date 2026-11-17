# subnet-toolkit

A small IPv4 subnet/CIDR calculator with address classification and basic DNS
resolution. I built it while prepping for networking interviews — subnetting by
hand was the skill I kept fumbling, so writing the math out in plain Java made
it stick. Runs anywhere with JDK 21+, no privileges needed; only the `dns`
command touches the network.

## Features

- `calc` — network, netmask, wildcard, broadcast, first/last host, totals for any CIDR
- accepts `192.168.1.130/25` or a bare address (treated as `/32`)
- correct `/31` and `/32` handling (RFC 3021 — no network/broadcast split)
- `ip` — classifies loopback, private, link-local, CGNAT, multicast, unspecified, reserved, global
- `dns` — resolves a name to its A/AAAA records
- strict input parsing: octets, prefix length, and IPv6 rejection all produce clear errors
- exit codes for scripting: 0 ok, 1 lookup failure, 2 bad input

## Build

Requires JDK 21+.

```bash
./gradlew build
./gradlew jar
```

On Windows use `gradlew.bat`. The jar lands in `build/libs/`.

## Usage

```bash
java -jar build/libs/subnet-toolkit-1.0.0.jar calc 192.168.1.130/25
```

```
address         192.168.1.130
prefix          /25
network         192.168.1.128
netmask         255.255.255.128
wildcard        0.0.0.127
broadcast       192.168.1.255
first host      192.168.1.129
last host       192.168.1.254
total addresses 128
usable hosts    126
```

```bash
java -jar build/libs/subnet-toolkit-1.0.0.jar calc 172.16.5.99/20
```

```
address         172.16.5.99
prefix          /20
network         172.16.0.0
netmask         255.255.240.0
wildcard        0.0.15.255
broadcast       172.16.15.255
first host      172.16.0.1
last host       172.16.15.254
total addresses 4096
usable hosts    4094
```

```bash
java -jar build/libs/subnet-toolkit-1.0.0.jar ip 127.0.0.1
```

```
classification loopback
```

`dns` does real lookups, so its output depends on your resolver:

```bash
java -jar build/libs/subnet-toolkit-1.0.0.jar dns example.com
```

```
A 104.20.23.154
A 172.66.147.243
```

Bad input exits 2 with a message on stderr:

```
$ java -jar build/libs/subnet-toolkit-1.0.0.jar calc 2001:db8::1
error: only IPv4 for now: 2001:db8::1
```

## How it works

An address is parsed into a 32-bit `int`, and the netmask is one left shift of
all ones (`0xffffffff << (32 - prefix)`, with `/0` special-cased because Java
shifts are mod 32). Network, broadcast, and host bounds fall out of a handful
of AND/OR operations on that int. Classification is a fixed sequence of range
checks against the well-known special-use blocks. `dns` is a thin wrapper over
`InetAddress.getAllByName`.

## Only use it on stuff you're allowed to

Run it against addresses and names you own or are authorized to look up.

## License

MIT
