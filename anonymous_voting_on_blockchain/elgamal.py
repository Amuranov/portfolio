"""
El Gamal encryption library
"""
import json

import gmpy2

from random import randint # Insecure randomness, better to use from "secrets" on python >= 3.6
from number import getPrime, isPrime, bytes_to_long
from vote_dproof import _hashg
import canonicaljson
from Crypto.Hash import SHA256


def dLog(p, g, g_m):
    """Compute the discrete log of g_m with basis g, modulo p"""
    a = 1
    i = 0
    while i < 2 ** 20:
        if a == g_m:
            return i
        else:
            a = a * g % p
            i += 1
    return None  # no DLog < 2**20 found

def inverse(x, p):
    """
    @returns x^-1 in Z*_p
    """
    res = pow(x, p-2, p)
    assert (res * x) % p == 1
    return res


def random_generator(p, q):
    """
    Take uniformly at random a generator of the group.
    Since the group is cyclic and of prime order,
    any (non-unitary) element is a generator.
    """
    # FYI:
    # The group is the group of quadratic residues modulo p, that is,
    # the group of squares in Z*_p, or
    # the set of x in Z such that there exists a y such that x = y^2 mod p
    g_prime = randint(2, p - 1) # Take any (non-unity) element of Z*_p
    g = pow(g_prime, 2, p)  # Squaring it gives generator of the group
    assert pow(g, q, p) == 1
    return g

QNbits=20 # @student: Is that enough ?
def gen_group(qnbits=QNbits):
    # FYI:
    # p has to be a safe prime (that is p=2*q+1 where q is prime)
    # See https://en.wikipedia.org/wiki/Safe_and_Sophie_Germain_primes
    p = 4
    while not isPrime(p):
        q = getPrime(QNbits)
        p = 2 * q + 1
    g = random_generator(p, q)
    return ElgamalGroup(p, g)

def is_prime(num):
    if num > 1:
        # check for factors
        for i in range(2, num):
            if (num % i) == 0:
                break
        else:
            return True
    # if input number is less than
    # or equal to 1, it is not prime
    else:
        return False

def gen_elgamal_keypair(G_group):
    """
        :param G_group: an ElgamalGroup
        @returns (x, y) with x an ElgamalSecretKey and y an ElgamalPublicKey.
    """
    g, p, q = G.g, G.p, G.q
    x = randint(1, int(q - 1))  # secret key
    y = pow(g, x, p)  # public key
    return ElgamalSecretKey(G, x), ElgamalPublicKey(G, y)

class ElgamalGroup:
    """A group in which DDH is difficult.

    For mathematicians: the group of non-zero quadratic residues modulo p,
    where p is an odd prime such that p = 2*q+1 where q is prime.
    (The order of the group is q.)
    """
    def __init__(self, p=None, g=None):
        self.p = p
        self.g = g
        self.q = (p-1)//2
        assert self.is_correct()

    def __eq__(self, other):
        """Makes group1 == group2 work."""
        return self.p == other.p and self.q == other.q and self.g == other.g

    def __hash__(self):
        """Needed to use groups as keys of dict objects."""
        return hash((self.p, self.q, self.g))

    def __contains__(self, elem):
        """Is elem in the group ? can be use by writing 'elem in group'"""
        return 1 <= elem < self.p and pow(elem, self.q, self.p) == 1

    def __repr__(self):
        """Pretty-printing for debug."""
        return "{}(p={}, q={}, g={})".format(type(self), self.p, self.q, self.g)

    def is_correct(self):
        """Are the parameters representing a group of prime order of quadratic
        residues modulo an odd prime ?"""
        return (
            self.p != 2 and
            isPrime(self.p) and
            isPrime(self.q) and
            self.g in self and
            self.q*2+1 == self.p and
            self.g != 1
            )

    def random_exp(self):
        """Take uniformly at random an integer in {0,...,q-1} and return it.

        Therefore, pow(self.g, self.random_exp(), self.p) is a random element
        of the group.
        """
        return randint(0, self.q-1)

class ElgamalPublicKey:
    """El Gamal public key"""

    def __init__(self, G, y):
        """G is an ElgamalGroup and y is an element of that group."""
        self.G = G
        self.y = y

    def __eq__(self, other):
        """Makes pk1 == pk2 work."""
        return self.G == other.G and self.y == other.y

    def __hash__(self):
        """Needed to use public keys as keys of dict objects."""
        return hash((self.G, self.y))

    def __repr__(self):
        """Pretty-printing for debug."""
        return "{}(y={}, G={})".format(type(self),self.y,self.G)

    def __add__(self, other):
        """Combination of keys"""
        assert self.G == other.G
        y = (self.y * other.y) % self.G.p
        return ElgamalPublicKey(self.G, y)

    def encrypt(self, m, r=None):
        """Encrypt a message.

        :param m: plaintext
        :type m: int
        :return: ElGamalCiphertext
        """
        if r == None:
            r = self.G.random_exp()
        assert len(format(m, "b")) <= 20
        g = self.G.g
        p = self.G.p
        if r < 0:
            r = p - r
        c1 = pow(g, r, p)
        c2 = (pow(g, m, p) * pow(self.y, r, p)) % p

        return ElgamalCiphertext(self.G, c1, c2)


class ElgamalSecretKey:
    """El Gamal secret key."""
    def __init__(self, G, x=None):
        self.G = G
        if x is None:
            x = G.random_exp()
        self.x = x

    def __eq__(self, other):
        return self.G == other.G and self.x == other.x

    def pk(self):
        """Generate the corresponding public key.
        
        :rtype: ElgamalPublicKey
        """
        y = pow(self.G.g, self.x, self.G.p)
        return ElgamalPublicKey(self.G, y)

    def prove_sk(self):
        """ Generates the corresponding commit and response"""
        r = self.G.random_exp()
        commit = pow(self.G.g, r, self.G.p)
        obj = {"pk": self.pk().y,"commit": commit}
        challenge = bytes_to_long(SHA256.new(canonicaljson.dumps(obj)).digest()) % self.G.q
        response = (r + (self.x * challenge)) % self.G.q
        return commit, response

    def decryption_factor(self, c1):
        """ Generates the corresponding decryption factor of a commit
        :param c1: cipertext
        :type c1: ElgamalCiphertext
        :returns: int
        """
        return pow(c1, self.x, self.G.p)

    def decryption_factor_poc(self, c1, pk):
        """ Generates decryption factor PoC
        :param c1: cipertext
        :type c: ElgamalCiphertext
        :param pk: ElGamalPublicKey
        :returns: dictionnary
        """
        res = {}
        res["pk"] = pk
        res["c1"] = c1
        res["decryption_factor"] = self.decryption_factor(c1)
        r = self.G.random_exp()
        d0 = pow(self.G.g, r, self.G.p)
        d1 = pow(c1, r, self.G.p)
        res["commit"] = [d0, d1]
        e = _hashg(json.dumps(res), self.G.q)
        res["response"] = r + ((e * self.x) % self.G.q)
        return res

    def decrypt(self, c):
        """Decrypt ciphertext c.

        :param c: cipertext
        :type c: ElgamalCiphertext
        :returns: plaintext
        :rtype: int
        """
        g = self.G.g
        p = self.G.p
        c1 = c.c1
        c2 = c.c2
        c1_prime = pow(c1, self.x, p)
        g_m = gmpy2.divm(c2, c1_prime, p)
        m = dLog(p, g, g_m)
        return m


class ElgamalCiphertext:
    """El Gamal ciphertext.

    Thanks to group homomorphism, operations over plaintexts can be implemented
    in the ciphertext domain.
    For two ciphertexts ca, cb and an integer a,
    * ca+cb corresponds to plaintext addition (hence ciphertext multiplication)
    * a*ca corresponds to plaintext multiplication (hence ciphertext
    exponentiation)
    """
    def __init__(self, G, c1, c2):
        self.G = G
        self.c1 = c1
        self.c2 = c2

    def __eq__(self, other):
        return self.G == other.G and self.c1 == other.c1 and self.c2 == other.c2

    def __repr__(self):
        """Pretty-printing for debug."""
        return "{}(c1={}, c2={})".format(type(self), self.c1, self.c2)

    def __add__(self, other):
        """Generate a new ciphertext that encrypts the sum of the plaintexts
        corresponding to self and other.
        
        :param other: ElgamalCiphertext
        :rtype: ElgamalCiphertext
        """
        return ElgamalCiphertext(
                self.G,
                (self.c1*other.c1) % self.G.p,
                (self.c2*other.c2) % self.G.p
                )

    def homomorphic_neg(self):
        """Generate a new ciphertext that encrypts the opposite of the
        plaintext encrypted by self.

        :rtype: ElgamalCiphertext
        """
        inv_c1 = inverse(self.c1, self.G.p)
        inv_c2 = inverse(self.c2, self.G.p)
        return ElgamalCiphertext(self.G, inv_c1, inv_c2)

    def homomorphic_sub(self, other):
        """Generate a new ciphertext that encrypts the subtraction of the plaintexts
        corresponding to self and other.
        
        :param other: ElgamalCiphertext
        :rtype: ElgamalCiphertext
        """
        # Hint: you can use the two methods defined above
        return self + (other.homomorphic_neg())

    def homomorphic_mul(self, alpha):
        """Generate a new ciphertext that encrypts the product of alpha and the plaintext
        corresponding to self.
        
        :param alpha: int
        :rtype: ElgamalCiphertext
        """
        return ElgamalCiphertext(self.G, pow(self.c1, alpha, self.G.p), pow(self.c2, alpha, self.G.p))

if __name__ == "__main__":
    # A few simple tests
    Ntests = 100
    pt_nbits = 10
    print('Testing dLog...', end='')
    for _ in range(Ntests):
        p = getPrime(32)
        g = randint(1, p-1)
        x = randint(1, 2**pt_nbits-1)
        assert dLog(p, g, pow(g, x, p)) == x
    print('OK.')
    print('Testing inverse...', end='')
    for _ in range(Ntests):
        p = getPrime(32)
        x = randint(1, p-1)
        assert (inverse(x, p)*x) % p == 1
    print('OK.')
    print('Testing gen_elgamal_keypair...', end='')
    for _ in range(Ntests):
        G = gen_group()
        sk, pk = gen_elgamal_keypair(G)
        assert pk == sk.pk()
    print('OK.')
    print('Testing encrypt/decrypt...', end='')
    for _ in range(Ntests):
        G = gen_group()
        sk, pk = gen_elgamal_keypair(G)
        m = randint(0, 2**pt_nbits-1)
        assert sk.decrypt(pk.encrypt(m)) == m
    print('OK.')
    print('Testing homomorphic addition...', end='')
    for _ in range(Ntests):
        G = gen_group()
        m1 = randint(0, 2**(pt_nbits-1)-1)
        m2 = randint(0, 2**(pt_nbits-1)-1)
        sk, pk = gen_elgamal_keypair(G)
        assert sk.decrypt(pk.encrypt(m1)+(pk.encrypt(m2))) == m1+m2
    print('OK.')
    print('Testing homomorphic subtraction...', end='')
    for _ in range(Ntests):
        G = gen_group()
        sk, pk = gen_elgamal_keypair(G)
        m1 = randint(0, 2**(pt_nbits-1)-1)
        m2 = randint(0, 2**(pt_nbits-1)-1)
        assert sk.decrypt(pk.encrypt(m1+m2).homomorphic_sub(pk.encrypt(m2))) == m1
    print('OK.')
    print('Testing homomorphic multiplication...', end='')
    for _ in range(Ntests):
        G = gen_group()
        sk, pk = gen_elgamal_keypair(G)
        m = randint(0, 2**(pt_nbits//2)-1)
        alpha = randint(0, 2**(pt_nbits//2)-1)
        assert sk.decrypt(pk.encrypt(m).homomorphic_mul(alpha)) == alpha*m
    print('OK.')

