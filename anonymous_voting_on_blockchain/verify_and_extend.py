from elgamal import *
from miner import *
from verify_election import *
import sys

class Verify_and_extend():
    """ This class is used to verify a block from a given blockchain and mining the next one"""
    def __init__(self, json_file, sk):
        """
        Take an optional json_file for a local copy and an optionnal secret key
        @sk: int
        @json_file: str
        """
        self.G = None
        self.sk = sk
        self.blocks = []
        self.blockchain = None
        self.gen_block = None
        self.json_file = json_file
        self.data = {}

    def check_validity(self, json_file = None):
        """Return true if chain validity is ok (every block of the chain must be ok)"""
        if json_file == None:
            json_file = self.json_file
        self.data = get_blocks(json_file)
        self.blocks = [self.data[block] for block in self.data]
        self.blockchain = Blockchain(json_file, False)
        if not self.blockchain.parse():
            valid = False
        else:
            valid = True
        return valid

    def get_trustees(self):
        """Get the trustees list"""
        n_talliers = self.gen_block["content"]["n_tallier_blocks"]
        trustees = []
        for i in range(1, n_talliers):
            if self.blocks[i]["miner_name"] not in trustees:
                trustees.append(self.blocks[i]["miner_name"])
        return trustees

    def get_c1s(self):
        """ Get all the ballots c1, used for the decryption"""
        n_talliers = self.gen_block["content"]["n_tallier_blocks"]
        n_votes = self.gen_block["content"]["n_vote_blocks"]
        c = 1
        for k in range(n_talliers +1, n_talliers + n_votes + 1):
            c1 = self.blocks[k]["content"]["ballot"]["ct"]["c1"]
            c *= c1 % self.G.p
        c = c % self.G.p
        return ElgamalCiphertext(self.G, c, 0)

    def add_block(self):
        """Take every possibility in account:
        - if no last block
        - if last block is a genesis -> next is tallier
        - if last block is a tallier -> next is tallier or ballot
        - if last block is a ballot -> next is ballot or dec_factor
        - if last block is dec_factor -> next one is dec_factor it one (or n) more blocks is (are) needed.
        """
        block = None
        if self.check_validity():
            if len(self.blocks) > 0:
                gen_block = self.blocks[0]
                self.gen_block = gen_block
                self.G = ElgamalGroup(gen_block["content"]["group"]["p"], gen_block["content"]["group"]["g"])
                self.sk = ElgamalSecretKey(self.G, self.sk)
                parent_block = self.blocks[-1]
                if parent_block["parent_id"] == "":     # BUILD FIRST TALLIER BLOCK
                    block = build_tallier_block(gen_block, parent_block, self.sk)
                elif set(parent_block["content"].keys()) == set(["tallier_key"]) :  # BUILD REST OF TALLIER BLOCK
                    if len(self.blocks) <= gen_block["content"]["n_tallier_blocks"]:
                        block = build_tallier_block(gen_block, parent_block, self.sk)
                    else: # BUILD FIRST BALLOT BLOCK
                        block = build_ballot_block(gen_block, parent_block, self.sk.pk())
                elif set(parent_block["content"].keys()) == set(["ballot"]):    # BUILD REST OF BALLOT BLOCK
                    if len(self.blocks) <= gen_block["content"]["n_tallier_blocks"] + gen_block["content"]["n_vote_blocks"]:
                        block = build_ballot_block(gen_block, parent_block, self.sk.pk())
                    else:   # BUILD FIRST DEC FACTOR
                        trustees = self.get_trustees()
                        if MY_NAME in trustees:
                            c1 = self.get_c1s()
                            dec_factor = self.sk.decryption_factor_poc(c1.c1, self.sk.pk().y)
                            block = build_dec_factor_block(self.gen_block, parent_block, self.sk.pk(), c1.c1,
                                                           dec_factor["decryption_factor"], dec_factor["commit"],
                                                           dec_factor["response"])
                elif set(parent_block["content"].keys()) == set(["dec_factor"]): # BUILD POSSIBLE REST OF DEC FACTOR
                    trustees = self.get_trustees()
                    dec_fact_miner_names = self.get_dec_factor_names()
                    if MY_NAME in trustees and MY_NAME not in dec_fact_miner_names:
                        c1 = self.get_c1s()
                        dec_factor = self.sk.decryption_factor_poc(c1.c1, self.sk.pk().y)
                        block = build_dec_factor_block(self.gen_block, parent_block,self.sk.pk(), c1.c1, dec_factor["decryption_factor"], dec_factor["commit"], dec_factor["response"])
                elif block:
                    self.blocks.append(block)
            else:
                block = build_genesis()
            print(block)
            self.blocks.append(block)
            self.update_json()
        return block

    def get_dec_factor_names(self):
        """Retreive all miner names dec factors. """
        stop = False
        miner_names = []
        i = 1
        while not stop:
            last_block = self.blocks[-i]
            # if last block is a decryption factor block
            if set(last_block["content"].keys()) == set(["dec_factor"]):
                miner_names.append(last_block["miner_name"])
            else:
                stop = True
            i += 1
        return miner_names

    def update_json(self):
        """Update the local copy of the blockchain"""
        block = self.blocks[-1]
        if block :
            id = block_id(block)
            self.data[id] = block
            with open(self.json_file, 'w') as self.json_file:
                json.dump(self.data, self.json_file)
        else :
            print("Already at the end of the chain.")

def main():
    """
    Verify the blockchain ( or ready to generate a genesis content if empty)
    and add the next block, or return Tally if the chain is completed
    """
    json_file = sys.argv[1]
    sk = int(sys.argv[2])
    verify_and_extend = Verify_and_extend(json_file, sk)
    verify_and_extend.add_block()


if  __name__ == "__main__":
    main()
