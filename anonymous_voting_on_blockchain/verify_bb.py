#Gmpy for Mod division
import gmpy2
import sys

import vote_dproof
from elgamal import *


class Verify_election:
    """ Object containing all method to verify the json election given in param
    data:type Json"""
    def __init__(self, data):
        self.talliers = []
        self.G        = []
        self.ballots  = []
        self.decryption_factors = []
        self.get_data(data)
        self.global_pk = self.generates_global_pk()

    def generates_global_pk(self):
        """Get all the talliers pk and creates the global pk"""
        # fetching all the keys
        pk_list = []
        for k in range(0, len(self.talliers)):
            pk_list.append(ElgamalPublicKey(self.G, self.talliers[k]['pk']))
        # Creating the global one
        global_pk = pk_list[0]
        for k in range(1, len(self.talliers)):
            global_pk += pk_list[k]
        return(global_pk)

    def get_data(self, data):
        """ Fetch the data from the json file and initialise the class attributs"""
        for k in data['tallier_keys']:
            self.talliers.append(k)
        for k in data['ballots']:
            self.ballots.append(k)
        for k in data['decryption_factors']:
            self.decryption_factors.append(k)
        self.G = ElgamalGroup(data["group"]['p'], data["group"]['g'])

    def verify_ballots(self):
        """Verify the ballots correctness"""
        k = 0
        res = True
        while(res and k < len(self.ballots)):
            res = vote_dproof.verify_ballot(self.global_pk, self.ballots[k])
            k += 1
        return res

    def verify_talliers(self):
        """Verify talliers PoK"""
        proof = True
        k = 0
        while(proof and k < len(self.talliers)):
            e = vote_dproof._hashg(json.dumps({"pk": self.talliers[k]['pk'], "commit": self.talliers[k]["commit"]}), self.G.q)
            proof = (pow(self.G.g, self.talliers[k]['response'], self.G.p) == (self.talliers[k]["commit"]* pow(self.talliers[k]["pk"], e, self.G.p)% self.G.p))
            k += 1
        return proof

    def verify_dec_factors(self):
        """ Verify the decryption factor"""
        proof_correct = True
        k = 0
        while(proof_correct and k<len(self.decryption_factors)):
            h = self.decryption_factors[k]["pk"]
            s = self.decryption_factors[k]["decryption_factor"]
            e = vote_dproof._hashg(json.dumps({"pk": self.decryption_factors[k]["pk"], "c1" : self.decryption_factors[k]["c1"], "decryption_factor" : self.decryption_factors[k]["decryption_factor"], "commit": self.decryption_factors[k]["commit"]}), self.G.q)
            f = self.decryption_factors[k]["response"]
            d0 = self.decryption_factors[k]["commit"][0]
            d1 = self.decryption_factors[k]["commit"][1]
            c1 = self.decryption_factors[k]["c1"]

            proof_correct = (pow(self.G.g, f, self.G.p) == (d0 * pow(h, e, self.G.p)) % self.G.p) and \
                            pow(c1, f, self.G.p) == (d1 * pow(s, e, self.G.p) % self.G.p)
            k += 1
        return proof_correct

    def diplay_res(self):
        """Display the election result,
        Print("error") if the votes are not correct"""
        if (self.verify_ballots() and self.verify_talliers() and self.verify_dec_factors()):
            c2 = 1
            for k in self.ballots:
                c2 *= k["ct"]["c2"]
            c1 = 1
            for k in self.decryption_factors:
                c1 = (k["decryption_factor"] * c1) % self.G.p

            val = gmpy2.divm(c2, c1, self.G.p)
            res = dLog(self.G.p, self.G.g, val)
            print("OK ", res)
        else:
            print("Fail")


if __name__ == "__main__":
    with open(sys.argv[1]) as json_file:
        data = json.load(json_file)
        election = Verify_election(data)
        election.verify_ballots()
        election.diplay_res()