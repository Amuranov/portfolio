import json
import random # Warning: insecure randomness
import sys

from elgamal import ElgamalPublicKey, ElgamalSecretKey, ElgamalGroup, ElgamalCiphertext, gen_group
from vote_dproof import generate_ballot


class Generate_election:
    """ Class containing all election generation methods"""
    def __init__(self, nt=3, nv=3):
        """Nt = trustee number
        Nv = voters number
        Nt,Nv:Type = Int
        """
        self.nt = nt
        self.nv = nv
        self.G = gen_group()
        self.global_pk = None
        self.ballots =[]
        self.talliers_sk = []
        self.decryption_factor = None
        self.decryption_factor_poc = None
        self.talliers_pk = None

    def generate(self):
        """ Main functions calling all generation in order """
        self.tallier_keys()
        self.generate_ballots()
        self.c1 = self.get_c1global()
        self.generate_dec_factors()

    def tallier_keys(self):
        """ Generation of talliers keys"""
        self.talliers_sk = [ElgamalSecretKey(self.G) for _ in range(self.nt)]
        self.talliers_pk = [sk.pk() for sk in self.talliers_sk]
        self.global_pk = sum(self.talliers_pk, ElgamalPublicKey(self.G, 1))


    def generate_ballots(self):
        """ Generation of ballots using vote_dproof library"""
        for i in range(self.nv):
            m = random.randint(0, 1)
            ballot_string = generate_ballot(self.global_pk, m)
            ballot = json.loads(ballot_string)
            self.ballots.append(ballot)

    def get_c1global(self):
        """ Retrieves the combination of all c1"""
        ciphertexts = []
        for ballot in self.ballots:
            c1, c2 = ballot["ct"]["c1"], ballot["ct"]["c2"]
            ciphertexts.append(ElgamalCiphertext(self.G, c1, c2))
        return sum(ciphertexts, self.global_pk.encrypt(0))

    def generate_dec_factors(self):
        """ Generation of decryption factor using ElgamalSecretKey class"""
        self.decryption_factor_poc = [sk.decryption_factor_poc(self.c1.c1, sk.pk().y) for sk in self.talliers_sk]

    def create_json(self):
        """ Creation of the right Json format at the wanted file"""
        abe = {}
        group = {"g": self.G.g, "p":self.G.p}
        abe["group"] = group
        abe["ballots"] = self.ballots
        pk = self.talliers_pk
        sk = self.talliers_sk
        tallier_key = []
        for k in range(self.nt):
            temps = {}
            temps["pk"] = pk[k].y
            commit, response = sk[k].prove_sk()
            temps["commit"] = commit
            temps["response"] = response
            tallier_key.append(temps)
        abe["tallier_keys"] = tallier_key
        abe["decryption_factors"] = self.decryption_factor_poc
        sys.stdout.write(json.dumps(abe))


if __name__ == "__main__":
    election = Generate_election(int(sys.argv[1]), int(sys.argv[2]))
    election.generate()
    election.create_json()




