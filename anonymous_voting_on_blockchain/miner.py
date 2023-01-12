
import json
import random
import binascii
import hashlib
import vote_dproof
from elgamal import *
# NOTE: run "python3 -m pip install --user requests" to get the requests
# library installed.
import requests

import canonicaljson

DIFFICULTY = 10
DANCEMOVE = 11
MY_NAME = "not-so-legit"

"""

    We use a data representation similar to JSON to represent the blocks.
    An object is either:
    * an associative map (aka dictionnary), mapping strings to objects,
    * a list (aka sequence) of objects (may be of heterogenous types),
    * a string (sequence of unicode codepoints, encoded in UTF-8),
    * an integer (a natural number),
    * a boolean.
    In this code, we use two representations:
    * the python representations (each type of object is mapped to a python
    type: map -> dict, list -> list or tuple, string -> string, integer -> int,
    bool -> bool).
    * the canonical JSON representation: a sequence of bytes
    (specified in <http://wiki.laptop.org/go/Canonical_JSON>).
    Conversions:
    * canonical JSON -> python: json.loads
    * python -> canonical JSON: canonicaljson.dumps

    The blocks must follow the following format:
    {
        "parent_id": <parent_id: hex-encoded>,
        "miner_name": <string>,
        "nonce": <int>,
        "content": {
            "dancemove": <int in [0, .., 10]>
        }
    }
    The hash of a block is the byte string computed as the SHA256 of its
    canonical JSON representation.
    The id of a block is the hexadecimal encoding of its hash.

    ! A block is valid if
     * it contains the required fields
     * it does not contain additional fields
     * each of the fields is of the correct type
     * the dancemove is between 0 and 10 (included)
     * its hash starts with 18 '0' bits
     * the length of its canonical JSON representation is at most 10000 bytes
    ! A chain is valid if:
     * all of its blocks are valid
     * for each block, the parent_id field contains the id of the previous block
     in the chain (except for the first block, for which the parent_id should
     be the empty string).

    Good luck!
"""

# Note: the functions from the start of the file until the main() function
# are probably the most interesting to read and/or modify.

def generates_nonce():
    # TODO MODIF
    return random.randint(0, 2 ** DIFFICULTY - 1)

def hash_block(b):
    """Hash of the block b as a byte string."""
    #print(b)
    return hashlib.sha256(canonicaljson.dumps(b)).digest()

def hashhex_to_strbin(hashhex):
    """
    Returns hash of Hexdigest in bin form
    """
    bin_str = str(bin(int(hashhex, 16)))[2:]
    return ("0" * (160 - len(bin_str)) + bin_str)

def block_id(b):
    """Id of the block b (its hex-encoded SHA256 hash)."""
    return binascii.hexlify(hash_block(b)).decode('ascii')

_MSK = (0x0, 0x80, 0xc0, 0xe0, 0xf0, 0xf8, 0xfc, 0xfe)
def hash_nb_leading_zeroes(h, nb):
    """Tests if the byte string hash starts with nb bits sets at 0."""
    off=0
    while nb>=8:
        if h[off] != 0:
            return False
        nb -= 8
        off +=1
    return (h[off] & _MSK[nb]) == 0

def pow_check(block, hardness):
    """Test if the proof of work is correct."""
    return hash_nb_leading_zeroes(hash_block(block), hardness)

def solve_block(b, parent_b):
    """
    Iterate over nonces until a valid proof of work is found for the block.

    :param b: A block.

    Modify b in-place, return True if the block is solved, otherwise return
    False.

    Note: the nonce must be between 0 and 2^31-1
    """
    # @student: Is it optimal to search until you find a block (therefore
    # possibly searching for a very long time) ?
    b["nonce"] = generates_nonce()
    while True:
        b["nonce"] += 1
        hexhash = block_id(b)
        str_bin = hashhex_to_strbin(hexhash)
        assert int(str_bin[0:DIFFICULTY - 1]) == 0

def reconstruct_blockchain(blocks):
    """
    Expect to receive a list of blocks.

    Output: list of lists of blocks. Each list of blocks is a reconstructed
    chain.
    """
    chain = {0: []}
    for block in blocks:
        chain[0].append(block)
    return chain


def choose_chain(blockchains):
    """Choose and return a chain from a list of chains (each chain is a list of blocks)."""
    chosen_ch = []
    top_value = 0
    for chain in blockchains:
        if top_value < len(blockchains[chain]):
            chosen_ch = blockchains[chain]
            top_value = len(blockchains[chain])
    return chosen_ch

def build_block(parent_block):
    """Constructs a new block from the parent block id."""
    block = dict()
    block["parent_id"] = block_id(parent_block)
    block["miner_name"] = MY_NAME
    block["nonce"] = generates_nonce()
    block["content"] = {}
    return block

def build_genesis():
    """Constructs a genesis block."""
    block = dict()
    block["parent_id"] = ""
    block["miner_name"] = MY_NAME
    block["nonce"] = generates_nonce()
    block["content"] = {}
    block = build_genesis_content(block)
    while not pow_check(block, DIFFICULTY):
        block["nonce"] += 1
    return block

def build_genesis_content(block, n_tallier_blocks=5, n_talliers=1, n_vote_blocks=20, hardness=DIFFICULTY, comment="genesis"):
    """Constructs the content of genesis block."""
    G = gen_group()
    block["content"] = {}
    block["content"]["n_tallier_blocks"] = n_tallier_blocks
    block["content"]["n_talliers"] = n_talliers
    block["content"]["group"] = {}
    block["content"]["group"]['g'] = G.g
    block["content"]["group"]['p'] = G.p
    block["content"]["comment"] = comment
    block["content"]["n_vote_blocks"] = n_vote_blocks
    block["content"]["hardness"] = hardness
    return block

def build_tallier_block(gen_block, parent_block, sk):
    """Constructs a new tallier block from the parent block id and ElgamalSecretKey."""
    hardness = gen_block["content"]["hardness"]
    pk = sk.pk()
    dproof = sk.prove_sk()
    block = build_block(parent_block)
    block["content"] = {}
    block["content"]["tallier_key"] = {}
    block["content"]["tallier_key"]["pk"] = pk.y
    block["content"]["tallier_key"]["commit"] = dproof[0]
    block["content"]["tallier_key"]["response"] = dproof[1]
    while not pow_check(block, hardness):   # Hardness proof
        block["nonce"] += 1
    return block

def build_ballot_block(gen_block, parent_block, pk, m=0):
    """Constructs a new ballot block from the parent block id and ElgamalPublicKey with a message m (default -> 0)."""
    hardness = gen_block["content"]["hardness"]
    block = build_block(parent_block)
    block["content"]["ballot"] = {}
    ballot = vote_dproof.generate_ballot(pk, m)
    ballot = json.loads(ballot)
    block["content"]["ballot"]["pk"] = pk.y
    block["content"]["ballot"]["ct"] = ballot["ct"]
    block["content"]["ballot"]["dproof"] = ballot["dproof"]
    while not pow_check(block, hardness):   # Hardness proof
        block["nonce"] += 1
    return block

def build_dec_factor_block(gen_block, parent_block, pk, c1, dec_factor, commit, response):
    """
        Constructs a new dec factor block from
            - the parent block id
            - ElgamalPublicKey
            - Total of c1 from ballots election
            - decryption factor
            - commit
            - response
    """
    hardness = gen_block["content"]["hardness"]
    block = build_block(parent_block)
    block["content"] = {}
    block["content"]["dec_factor"] = {}
    block["content"]["dec_factor"]["pk"] = pk.y
    block["content"]["dec_factor"]["c1"] = c1
    block["content"]["dec_factor"]["decryption_factor"] = dec_factor
    block["content"]["dec_factor"]["commit"] = commit
    block["content"]["dec_factor"]["response"] = response
    while not pow_check(block, hardness):   # Hardness proof
        block["nonce"] += 1
    return block

def get_blocks(filename):
    """Download all blocks from server, returns a list of blocks (in python
    representation).
    """
    with open(filename) as json_file:
        data = json.load(json_file)
    return data
