# Overview of the project 

In this project, I am going to create all together an anonymous distributed network of election runners. I will dive into two subjects of privacy enhancing technologies:
Voting and Cryptocurencies through several objectives: electing the public key of the election, casting votes and computing the results of the elections by interacting with a custom blockchain-based protocol over the Tor network 2. The protocol would work in three-phases: first you must enter the election of the public key to be used to cast votes by broadcasting a valid block with your own public key. After that the chain contains n_talliers_blocks blocks, all election runners can compute the election key from the n_talliers most seen public keys on the chain. 
In case some different keys appear the same number of times and only some of those can be considered to have n_talliers keys, always pick the key that got its last vote first. 

The second phase allows election runners to cast votes when they found valid blocks (the more you find blocks, the more you can vote), and broadcast blocks to all
other members of the distributed network. The second-phase would then finish after n_vote_blocks blocks appeared on the chain. 

The third and final phase is to compute the result of the election and broadcast it to the other participants.
I will have to create an Onion Service and advertise the onion address over a public data record (here we will use the Moodle forum). A simple
web server must be connected to the Onion Service and answer GET/POST requests as we will specify here. An Onion Service supports firewall traversal,
NAT punching, end-to-end encryption and is available online in a matter of seconds once created, so your service should be available wherever you are.
Keep in mind that you need to keep your machine up to be accessible by
others.

## Peer-to-peer network

Each election participant interacts with other participants by simultaneously acting as a web server and as a client making requests to other participants. The web server is accessible as a Tor onion service, through a .onion domain name, which is the address of that participant. You will have to configure your own Onion Service to connect to the HTTP server that we provide in simple_http_server.py. Here are some documentation
that you may find interesting: https://2019.www.torproject.org/docs/tor-onion-service.html.en

## Votes

The voting scheme is the one based on El Gamal encryption. We work in the group of non-zero quadratic residues modulo an odd prime p, that is, the subgroup of order q = (p−1)/2 of Z∗p.

### Voting process 

At a high level, the vote is split into a few conceptual stages:

1. Election parameters defintion. The election settings (such as the group used for the election) have to be agreed upon.

2. Tallier key generation. Each tallier generates its own El Gamal keypair, and publishes its public key along with a zero-knowledge proof of knowledge that he knows the secret key (pk_pok object).

3. Global key computation. Each voter gathers all the tallier pk_pok objects, checks all the proofs, and computes the global election public key as the product of all the tallier public keys.

4. Ballot generation. Each voter encrypts its vote using the global election public key and generates a zero-knowledge proof that the encrypted text is either 0 or 1 (ballot object).

5. Tallying. Any interested party (including talliers) collects all the ballots, checks the proofs for each of those, and homomorphically adds the encrypted votes. The result of this operation is the encrypted election result.

6. Decryption factors. Each tallier computes the decryption factor for the encrypted election result associated to its keypair and publishes it, along with a zero-knwoledge proof that the decryption factor is correct with respect to its public key and the encrypted election result (dec_factor_poc object).

7. Decryption. Each interested party collects all the decryption factors and checks the proofs, then multiplies the talliers decryption factors to get the global election decryption factor. Finally, the encrypted election result can be decrypted using the global election decryption factor.


## Installation
All dependencies of the first project (gmpy, ...)

Dependencies:
* python3.6 or more recent
* PyPI libraries:
```bash
python3 -m pip install --user "requests[socks]" requests_futures
```

All the tor setting files allocated in tor directory


### `main.py`
How we start the main for a new vote

```bash
python3 main.py -n <wanted_name> -p 2iqjbe2vo6cadsnut2nmvpfdi4j5wv57njdjzsocvxh6k53b7q6jmtad.onion -a 2r4t7k6zmxussuuqrw5sq64c4hetxicmgaljdighfbl5rsmnpwltzrad.onion -c socks5h://localhost:9050 new
```


