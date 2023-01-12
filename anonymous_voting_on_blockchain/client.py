"""

Contains a simple client, the data format and the Message definition used to
exchange peers and blocks between the local server and the local client

"""
import collections
import copy
import random
import time
from urllib.parse import urlparse
import concurrent.futures

import requests
from requests_futures.sessions import FuturesSession

from miner import block_id # @student: You have to provide this.

# Number of failed attemps before we declare a peer to be unreachable.
DEFAULT_UP_CTR = 10

class Data:
    """The structure for inter-thread communication (client -> server):
    client publishes updates in this structure, and server reads it.
    Updates to the structure have thus to be atomic.
    """
    def __init__(self):
        self.blocks = {}
        self.peers = {}
        self.election_id = None


class Message:
    """Message in the queue from the server to the client."""
    def __repr__(self):
        return "Message(type={},value={})".format(self.type, self.elem)

    def __init__(self, elem, typ):
        assert typ == "peer" or typ == "block", "Invalid type"
        self.elem = elem
        self.type = typ


class Client:
    """
        Manage communications through the Tor network or through a local
        network.

        The most interesting methods for the user are:
        * sync: to re-synchronize the client if you think you have lost the
        network connection at some point, or there are some blocks in the
        network that you did not receive
        * broadcast_block: broadcast a block to all known peers
        * add_blocks: add blocks the list of blocks known by the client.
        (You should call both add_blocks and broadcast_block when you have
        mined a new block.)
        * get_new_blocks: to receive the new blocks from the P2P network.
        Also has the side-effect of updating the list of peers. You might want
        to call this often.
        * get_all_blocks: returns all the blocks known by the client
    """
    def __init__(self, read_only_queue, data,
            server_address, proxy, init_peers):
        self.bad_peers = set()
        self.session = FuturesSession()
        self.myaddress = server_address
        if proxy is not None:
            print("Setting Sock5 proxy")
            self.session.proxies = {}
            self.session.proxies['http'] = proxy
        self._data = data
        self.read_only_queue = read_only_queue
        self.add_peers(init_peers)
        self.sync()

    def _req(self, peer, path, method='get', **kwargs):
        """Make a GET/POST request to a peer, at path (additional arguments
        forwarded to requests.get/post())."""
        kwargs.update({'timeout': 15})
        url = 'http://{}/{}'.format(peer, path)
        if method == 'get':
            res = self.session.get(url, **kwargs)
        else:
            assert method == 'post'
            res = self.session.post(url, **kwargs)
        res.bckch_peer = peer
        return res

    def sync(self):
        """Syncs the client with all peers.

        The is done in the initialization, but might be useful in case of a
        loss of connection.

        1. Sync the peers: send a post / to all the initial peers, which allows
        to register to those peers and incrementally get knowledge of new
        peers (to which we register).
        2. Sync the blocks: make a /list_blocks to each peer, then retreive
        each block to a peer (retrying another peer if that fails).

        """
        print("Syncing our Peer list with the one of known addresses")
        self._broadcast({'sender': self.myaddress})
        print("peerlist: {}".format(self._data.peers))
        print("Syncing our Block list with the ones of known addresses")
        # Get all the block ids known by each peer
        fs = [self._req(peer, 'list_blocks') for peer in self._data.peers.keys()]
        # map: block id -> list of peers knowing this block
        blocks_peers = dict()
        for resp in concurrent.futures.as_completed(fs):
            peer, l = self._try_fut(resp, val=self._val_strlist)
            if l:
                for block in l:
                    blocks_peers.setdefault(block, []).append(peer)
        print("blocks-peers map: {}".format(blocks_peers))
        # Retrieve blocks. Strategy: for each block, try a first peer.
        # If it fails, retry with another peer knowing that block.
        fs = set()
        # initial request for each block
        for block, peers in blocks_peers.items():
            if block_id(block) not in self._data.blocks:
                peer = peers.pop()
                fs.add(self._req(peer, 'blocks/{}'.format(block)))
        while fs:
            (done, fs) = concurrent.futures.wait(fs,
                    return_when=concurrent.futures.FIRST_COMPLETED)
            for req in done:
                peer, blocks = self._try_fut(req)
                b_id = req.result().request.url.split('/')[-1]
                if (blocks and
                        isinstance(blocks, dict) and b_id in blocks and
                        block_id(blocks[b_id]) == b_id):
                    # request successful, we have a block to handle
                    self.read_only_queue.put(Message(blocks[b_id], 'block'))
                else:
                    # failed request, retry another peer (if there exists one)
                    # let's make the peer fail instead
                    self._failed_peer('failed sync req', peer)
                    peers = blocks_peers[b_id]
                    if peers:
                        fs.add(self._req(peers.pop(), 'blocks/{}'.format(b_id)))
        #print("blocks: {}".format(self._data.blocks))

    def _failed_peer(self, resp, peer):
        """Failed to connect to a peer."""
        # May happen if the first connection to that peer fails
        if peer not in self._data.peers:
            return
        self._data.peers[peer] -= 1
        if self._data.peers[peer] <= 0:
            # this is atomic, no sync needed
            self._data.peers.pop(peer, None)

    def _bad_peer(self, resp, peer):
        """Ban a peer because it gives bad info ;)."""
        print('peer is non-compliant', peer, 'result', resp)
        self._data.peers.pop(peer, None)
        self.bad_peers.add(peer)
    
    def _try_fut(self, fut, val=lambda x: x):
        """Try to get the result out of a future fut, apply the function val on
        it and return it.

        If it fails, mark the peer as failed/banned.
        """
        peer = fut.bckch_peer
        try:
            res = fut.result()
        except requests.exceptions.RequestException as e:
            print("Got exception {} for peer {}".format(e, peer))
            self._failed_peer(e, peer)
        else:
            if res.ok:
                try:
                    #add the sending url to our peers
                    self.add_peers([peer])
                    return (urlparse(res.request.url).netloc,
                            val(res.json()))
                except Exception as e:
                    print("Got exception {} for peer {}".format(e, peer))
                    self._bad_peer(res, peer)
            else:
                print("Got res {} ?? for peer {}", res, peer)
                self._failed_peer(res)
            peer = urlparse(res.request.url).netloc
        return peer, None

    @staticmethod
    def _val_strlist(l):
        """Check that l is a list of string."""
        if not (isinstance(l, list) and all(isinstance(p, str) for p in l)):
            raise ValueError(l)
        else:
            return l

    def _broadcast(self, data, peers_to_send=None):
        """Broadcast a message to all known peers, updating peer list along the
        way.

        data is a dictionnary
        """
        peers_sent = set()
        new_peers = set()
        if peers_to_send is None:
            peers_to_send = set(self._data.peers.keys())
        first=True
        while peers_to_send or (first or fs):
            first=False
            fs = set()
            # we can do plain http since Tor takes care of confidentiality and
            # authentication
            fs |= set(self._req(peer, '', method='post', json=data)
                    for peer in peers_to_send
                    )
            peers_sent |= peers_to_send
            peers_to_send = set()
            (done, fs) = concurrent.futures.wait(fs,
                    return_when=concurrent.futures.FIRST_COMPLETED)
            for req in done:
                _, l = self._try_fut(req, self._val_strlist)
                if l is not None:
                    for peer in l:
                        if peer not in peers_sent and peer not in self.bad_peers:
                            self.add_peers([peer])
                            peers_to_send.add(peer)
                            new_peers.add(peer)

    def broadcast_block(self, block):
        """Send block to all known peers."""
        self._broadcast({'block': block, 'sender': self.myaddress})

    def _process_messages(self):
        """Returns list of messages, or an empty list if no message."""
        messages = []
        while not self.read_only_queue.empty():
            messages.append(self.read_only_queue.get())
        blocks = [m.elem for m in messages if m.type == 'block']
        peers = [m.elem for m in messages if m.type == 'peer']
        self.add_blocks(blocks)
        peers_to_confirm = self._check_new_peers(peers)
        #Will be added to our list if the broadcast succeeds
        print("Trying to confirm addresses of {0}".format(peers_to_confirm))
        self._broadcast({'sender':self.myaddress}, set(peers_to_confirm))
        return blocks, peers_to_confirm

    def get_new_blocks(self):
        """Get the blocks that have been broadcasted to us, and also update the
        list of known peers.
        """
        blocks, _ = self._process_messages()
        return blocks

    def add_blocks(self, blocks):
        """Thread-safely add a list of blocks to our blocklist."""
        for block in blocks:
            # this is atomic, no sync needed
            self._data.blocks[block_id(block)] = block

    def remove_blocks(self, blocks):
        """Thread-safely remove a list of blocks from our blocklist."""
        for block in blocks:
            # this is atomic, no sync needed
            self._data.blocks.pop(block_id(block), None)

    def _check_new_peers(self, peers):
        peers_to_confirm = []
        for peer in peers:
            if peer not in self._data.peers and peer != self.myaddress:
                # this is atomic, no sync needed
                peers_to_confirm.append(peer)
        return peers_to_confirm

    def add_peers(self, peers):
        """Thread-safely add a list of peers to our peer list."""
        for peer in peers:
            if peer not in self.bad_peers and peer != self.myaddress:
                # this is atomic, no sync needed
                print("Adding peers {0} to list; or resetting its counter".format(peer))
                self._data.peers[peer] = DEFAULT_UP_CTR
            elif peer == self.myaddress:
                print("Trying to add ourself in the peer list?")
            else:
                print("Peer {0} appears to be in our bad_peer list".format(peer))

    def remove_peers(self, peers):
        """Thread-safely remove a list of peers from our peer list."""
        for peer in peers:
            # this is atomic, no sync needed
            self._data.peers.pop(peer, None)

    def get_all_blocks(self):
        """Return all the blocks known by the client."""
        return copy.deepcopy(self._data.blocks)

    def get_all_peers(self):
        """Return all the peers known by the client."""
        return copy.deepcopy(self._data.peers)

    def get_election_id(self):
        """Return the election id."""
        return copy.deepcopy(self._data.election_id)

