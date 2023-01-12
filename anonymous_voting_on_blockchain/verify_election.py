from miner import *
from elgamal import *
import vote_dproof
from utils import is_list_of_2_int
import sys


class Verify_election:
    """
    Contains all methodes needed for the blockchain
    validity of a block, parsing of the chains...
    """

    def __init__(self, filename = "election_content.json", stdout=True):
        self.stdout = stdout
        self.filename = filename
        self.trustees = []
        self.gen_block = None
        self.hardness = None
        self.talliers_keys = []
        self.dec_factor_keys = []
        self.ballot_c1s = []
        self.G = None
        self.last_id = ""
        self.chains = []
        self.decryption_factors = []
        self.ballot_c2s = []

    def build_chain(self, blocks, i=0, chain = []):
        """Build Recursively the multiple possible blockchains """
        if i == len(blocks) - 1:
            chain.append(blocks[i])
            self.chains.append(chain)
        elif blocks[i]["parent_id"] == "":
            if len(chain) > 0:
                self.chains.append(chain)
                chain = []
            chain.append(blocks[i])
            self.build_chain(blocks, i + 1)
        else:
            chain.append(blocks[i])
            self.build_chain(blocks, i + 1)

    def reset(self):
        """ Reset every parametres"""
        self.trustees = []
        self.gen_block = None
        self.hardness = None
        self.talliers_keys = []
        self.dec_factor_keys = []
        self.ballot_c1s = []
        self.G = None
        self.last_id = ""
        self.chains = []
        self.decryption_factors = []
        self.ballot_c2s = []

    def parse(self):
        """Parse and verify the blockchain"""
        self.reset()
        valid = True
        data = get_blocks(self.filename)
        blocks = [data[block] for block in data]
        if blocks:
            self.build_chain(blocks)
            self.chains.sort(key=len)
        for i in range(len(self.chains) -1, -1, -1):
            gen_block = self.chains[i][0]
            self.hardness = gen_block["content"]["hardness"]
            if not(self.is_genesis_valid(gen_block)):   # GENESIS VERIFICATION
                valid = False
            self.G = ElgamalGroup(self.chains[i][0]["content"]["group"]["p"],
                                  self.chains[i][0]["content"]["group"]["g"])
            n_tallier_block = gen_block["content"]["n_tallier_blocks"]
            n_vote_block = gen_block["content"]["n_vote_blocks"]
            j = 1 # nbr of current tallier
            while j < len(self.chains[i]) and j <= n_tallier_block:     # TALLIER VERIFICATION
                block = self.chains[i][j]
                parent_block = self.chains[i][j-1]
                if block["miner_name"] not in self.trustees:
                        self.trustees.append(block["miner_name"])
                if not(self.is_tallier_block_valid(gen_block, block, parent_block)):
                    valid = False
                if block["content"]["tallier_key"]["pk"] not in self.talliers_keys:
                    self.talliers_keys.append(block["content"]["tallier_key"]["pk"])
                j += 1
            j -= 1
            if j == n_tallier_block:    # BALLOT VERIFICATION
                k = 1 # nbr of current ballot
                while k+j < len(self.chains[i]) and  k <= n_vote_block:
                    block = self.chains[i][j+k]
                    parent_block = self.chains[i][j+k - 1]
                    if not(self.is_block_ballot_valid(gen_block, block, parent_block)):
                        valid = False
                    self.ballot_c2s.append(block["content"]["ballot"]["ct"]["c2"])  # Get all c2 for the dec phase
                    self.ballot_c1s.append(block["content"]["ballot"]["ct"]["c1"])  # Get all c1 for the dec phase
                    k += 1
                k -= 1
                if k == n_vote_block:   # DEC FACTOR VERIFICATION
                    n_trustee = len(self.trustees)
                    t = 0 # nbr of trustees
                    dec_fac = k + j + t + 1 # Current block id
                    while dec_fac < len(self.chains[i]) and t < n_trustee:
                        block = self.chains[i][dec_fac]
                        parent_block = self.chains[i][dec_fac - 1]
                        if not self.is_dec_factor_block_valid(block, parent_block):
                            valid = False
                        else:
                            self.decryption_factors.append(block["content"]["dec_factor"]["decryption_factor"])
                        t += 1
                    if t == n_trustee:
                        self.decryption()
            if not self.is_dec_factor_content_valid(self.chains[i][-1]) and valid and self.stdout:
                print("OK ", self.chains[i][-1]["parent_id"])
            if not valid:
                print("FAIL")
        return valid

    def is_block_valid(self, block):
        """Check validity of a block. Return True or False"""
        return (
                type(block) == dict and
                set(block.keys()) == set(["parent_id", "miner_name", "nonce", "content"]) and
                type(block["parent_id"]) == str and
                type(block["miner_name"]) == str and
                type(block["nonce"]) == int and
                type(block["content"]) == dict and
                pow_check(block, self.hardness)
        )

    def is_genesis_valid(self, gen_block):
        """Check validity of a genesis block. Return True or False"""
        return (
                self.is_block_valid(gen_block) and
                self.is_gen_content_valid(gen_block)
        )

    def is_gen_content_valid(self, block):
        """Check validity of a genesis content block. Return True or False"""
        valid = False
        if (block["content"]["n_tallier_blocks"] > 0 and
                block["content"]["n_vote_blocks"] > 0 and
                0 < block["content"]["n_talliers"] <= block["content"]["n_tallier_blocks"] and
                block["content"]["hardness"] > 0):
            valid = True
        return valid

    def is_gen_group_valid(self, G, y, x):
        """Check validity of a Genesis group keys (g, p and q). Return True or False"""
        return (
                G.q == (G.p - 1) / 2 and
                2 <= G.g < G.p and
                is_prime(G.q) and
                isPrime(G.p) and
                1 <= y < G.p and
                0 <= x < G.q and
                y ** G.q == 1 % G.p
        )

    def is_parent_hash_valid(self, block, parent_block):
        """Check validity of the parent hash . Return True or False"""
        return (
                block["parent_id"] == block_id(parent_block)
        )

    def is_tallier_block_valid(self, gen_block, block, parent_block):
        """Check validity of a tallier block. Return True or False"""
        return (
                set(block["content"].keys()) == set(["tallier_key"]) and
                set(block["content"]["tallier_key"].keys()) == set(["pk", "commit", "response"]) and
                type(block["content"]["tallier_key"]["pk"]) == int and
                type(block["content"]["tallier_key"]["commit"]) == int and
                type(block["content"]["tallier_key"]["response"]) == int and
                self.is_block_valid(block) and
                self.is_parent_hash_valid(block, parent_block) and
                self.is_block_tallier_keys_valid(gen_block, block)
        )

    def is_block_tallier_keys_valid(self, gen_block, tallier_block):
        """Check validity of tallier keys. Return True or False"""
        G = ElgamalGroup(gen_block["content"]["group"]["p"], gen_block["content"]["group"]["g"])
        pk = tallier_block["content"]["tallier_key"]["pk"]
        commit = tallier_block["content"]["tallier_key"]["commit"]
        response = tallier_block["content"]["tallier_key"]["response"]
        e = vote_dproof._hashg(json.dumps({"pk": pk, "commit": commit}), G.q)
        return (
                G.is_correct() and
                commit < G.p and
                pow(G.g, response, G.p) == (commit * pow(pk, e, G.p)) % G.p
        )

    def is_block_ballot_valid(self, gen_block, block, parent_block):
        """Check validity of a ballot block structure. Return True or False"""
        return (
                type(block["content"]["ballot"]["pk"]) == int and
                set(block["content"].keys()) == set(["ballot"]) and
                type(block["content"]["ballot"]["ct"]["c1"]) == int and
                type(block["content"]["ballot"]["ct"]["c2"]) == int and
                len(block["content"]["ballot"]["dproof"]["commit"]) == 2 and
                type(block["content"]["ballot"]["dproof"]["commit"]) == list and
                set(block["content"]["ballot"]["ct"].keys()) == set(["c1", "c2"]) and
                is_list_of_2_int(block["content"]["ballot"]["dproof"]["commit"][0]) and
                is_list_of_2_int(block["content"]["ballot"]["dproof"]["commit"][1]) and
                is_list_of_2_int(block["content"]["ballot"]["dproof"]["challenge"]) and
                is_list_of_2_int(block["content"]["ballot"]["dproof"]["response"] ) and
                set(block["content"]["ballot"].keys()) == set(["pk", "ct", "dproof"]) and
                set(block["content"]["ballot"]["dproof"].keys()) == set(["commit", "challenge", "response"]) and
                self.is_parent_hash_valid(block, parent_block) and
                self.is_block_valid(block) and
                self.is_ballot_valid(block, gen_block)
        )

    def is_ballot_valid(self, block, gen_block):
        """Check validity of a ballot block. Return True or False"""
        g, p = gen_block["content"]["group"]["g"], gen_block["content"]["group"]["p"]
        G = ElgamalGroup(p, g)
        pk = block["content"]["ballot"]["pk"]
        return (
            vote_dproof.verify_ballot(ElgamalPublicKey(G, pk), block["content"]["ballot"])
        )

    def is_dec_factor_valid(self, block):
        """Check validity of a decryption factor block. Return True or False"""
        valid = True
        c1 = block["content"]["dec_factor"]["c1"]
        h = block["content"]["dec_factor"]["pk"]
        s = block["content"]["dec_factor"]["decryption_factor"]
        d0 = block["content"]["dec_factor"]["commit"][0]
        d1 = block["content"]["dec_factor"]["commit"][1]
        f = block["content"]["dec_factor"]["response"]
        e = vote_dproof._hashg(json.dumps(
            {"pk": h, "c1": c1,
             "decryption_factor": s,
             "commit": block["content"]["dec_factor"]["commit"]}), self.G.q)
        if (s > self.G.p or d0 > self.G.p or d1 > self.G.p):
            valid = False
        if block["content"]["dec_factor"]["pk"] in self.talliers_keys:
            constructed_c1 = 1
            for k in range(len(self.ballot_c1s)):
                constructed_c1 *= self.ballot_c1s[k]
            if c1 == (constructed_c1 % self.G.p):
                if not((pow(self.G.g, f, self.G.p) == (d0 * pow(h, e, self.G.p)) % self.G.p) and
                            pow(c1, f, self.G.p) == (d1 * pow(s, e, self.G.p) % self.G.p)):
                    valid = False
        return valid

    def is_dec_factor_block_valid(self, block, parent_block):
        """Check validity of a dec factor block. Return True or False"""
        valid = True
        if self.is_block_valid(block) and \
            self.is_dec_factor_content_valid(block) and \
                self.is_parent_hash_valid(block, parent_block) and \
                block["content"]["dec_factor"]["pk"] not in self.dec_factor_keys and \
                self.is_dec_factor_valid(block):

            self.dec_factor_keys.append(block["content"]["dec_factor"]["pk"])
        else:
            valid = False
        return valid

    def is_dec_factor_content_valid(self, block):
        """Check validity of a dec factor content. Return True or False"""
        return (
                set(block["content"].keys()) == set(["dec_factor"]) and
                set(block["content"]["dec_factor"].keys()) == set(["pk", "c1", "decryption_factor", "commit", "response"]) and
                type(block["content"]["dec_factor"]["pk"]) == int and
                type(block["content"]["dec_factor"]["c1"]) == int and
                type(block["content"]["dec_factor"]["decryption_factor"]) == int and
                type(block["content"]["dec_factor"]["commit"]) == list and
                type(block["content"]["dec_factor"]["response"]) == int and
                is_list_of_2_int(block["content"]["dec_factor"]["commit"])
        )

    def decryption(self):
        """Decrypt the ballots set. Return Tally with the number of '0' votes"""
        ct2 = 1
        for c2 in self.ballot_c2s:
            ct2 *= c2 % self.G.p
        ct2 %= self.G.p
        ct1 = 1
        for c1 in self.decryption_factors:
            ct1 = (c1 * ct1) % self.G.p
        ct1 %= self.G.p
        val = gmpy2.divm(ct2, ct1, self.G.p)
        res = dLog(self.G.p, self.G.g, val)
        if self.stdout:
            print("TALLY: ", res)


def main():
    jsonfile = sys.argv[1]
    blockchain = Verify_election(jsonfile)
    blockchain.parse()


if  __name__ == "__main__":
    main()
