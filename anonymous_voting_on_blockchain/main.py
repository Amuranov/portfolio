
import threading
import argparse
import sys
import queue
import time
import json

import blockchain_server
import client
import miner
import verify_and_extend


parser = argparse.ArgumentParser(description="Looking for help when you're about to jump in"
                     " spacetime?")
parser.add_argument("-l", "--listen", type=int, default=1234,
        help=
        "Local port on which the server will listen. "
        "The port should be the same as the one in the Tor reverse proxy configuration."
        )
parser.add_argument("-a", "--address", type=str, default=None,
        help="Public address of you server. "
        "For Tor: the '.onion' address. For local tests without Tor: empty "
        "(will set the address to localhost:port)."
        )
parser.add_argument("-c", "--client", type=str, default=None,
        help="Proxy to use for sending requests. (You might want to "
        "use socks5h://localhost:9050 if you use the default Tor configuration.)")
parser.add_argument("-p", "--peers", type=str, default="",
        help="Comma-separated list of addresses of peers to connect to.")
parser.add_argument("-s", "--sk", type=int, default=1111, help="Your tallier secret key (0 if you want it to be automatically generated).")
parser.add_argument("-w", "--way", type=int, default=None, help="The path you want to take in the dimensions.")
parser.add_argument("-n", "--name", type=str, required=True, help="Your miner name (required).")
subparsers = parser.add_subparsers(
    title="subcommands",
    description="valid subcommands",
    help="",
    dest="command",
)
parser_workon = subparsers.add_parser("workon", help="Work on an existing election")
parser_workon.add_argument("--election_id", type=str,
        help="Id of the election (which is the id of the genesis block)")
parser_new = subparsers.add_parser("new", help="Create and work on a new election")
parser_new.add_argument("-m",
        help="group modulus (p), generate a random one if not provided.",
        default=None)
parser_new.add_argument("-g",
        help="group generator (g), generate a random one if not provided.",
        default=None)
parser_new.add_argument("--voteblocks", type=int, help="Number of vote blocks.")
parser_new.add_argument("--talblocks", type=int, help="Number of tallier blocks.")
parser_new.add_argument("--talliers", type=int, help="Number of talliers.")
parser_new.add_argument("--hardness", type=int, help="Hardness of the chain.")
parser_new.add_argument("--comment", type=str, help="Comment in the genesis block.")

def startup_client_server(election_id, listen_port, address, proxy, init_peers):
    """Start a P2P server and a P2P client, return the client.

    Note: you don't need to interact with the server, interact only with the
    client (which in turn communicates with the server).

    :arg election_id: id of the election
    :type election_id: str
    :arg listen_port: port on which the server will listen
    :type listen_port: int
    :arg address: public address of the server
    :type address: str
    :arg proxy: proxy to use for client requests
    :type proxy: str
    :arg init_peers: initial list of peers
    :type init_peers: list of str
    """
    # Shared dict object between threads!
    data = client.Data()
    data.election_id = election_id
    server_to_client = queue.Queue()
    # Daemonize, so we can easily exit from client function
    # (there is no cleanup to do)
    server_thread = threading.Thread(
            target=blockchain_server.server_start,
            args=(server_to_client, data, listen_port),
            daemon=True)
    server_thread.start()
    return client.Client(server_to_client, data, address, proxy, init_peers)

def main():
    # Get the command-line arguments, parse and post-process them.
    args = parser.parse_args()
    if args.address is None:
        args.address = "localhost:{}".format(args.listen)
    peers = list(args.peers.split(',')) if args.peers else []
    if args.way not in (None, 0, 1):
        print("Invalid path: {} (should be 0 or 1).".format(args.way))

    # # Get the election id
    if args.command == "new":
        block = miner.build_genesis()
        election_id = miner.block_id(block)
    elif args.command == "workon":
        # Same as new, we could workon a given block, we just didn't implement it here
        election_id = args.election_id
        block = miner.build_genesis()
    # # Start the client and the server.
    # # The server will run in the background, and communicate with the client.
    # # Use methods on the client to get the new blocks from your peers and to
    # # send them your new blocks.

    client = startup_client_server(election_id, args.listen, args.address,
                                   args.client, peers)

    list_blocks = {}
    list_blocks[election_id] = block
    filename = "temp.json"
    with open(filename, 'w') as json_file:
        json.dump(list_blocks, json_file)

    # Build a chain with a 2sec intervalle between each block
    # At each lap, we brodacast, sync, and add the block to our list
    while True:
        client.sync()
        blockchain = verify_and_extend.Verify_and_extend(filename, args.sk)
        new_block = blockchain.add_block()
        tmp_block = {}
        tmp_block[miner.block_id(new_block)] = new_block
        client.add_blocks(tmp_block)
        client.broadcast_block(tmp_block)
        time.sleep(2)


if __name__ == "__main__":
    main()
