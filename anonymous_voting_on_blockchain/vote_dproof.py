
import hashlib
import json

import canonicaljson

def _hashg(json_obj, q):
    """Hash a JSON object to a integer modulo q.

    :param json_obj: JSON object encoded into a str.

    Procedure:
    * map the object to json in binary canonical form (see
    <https://pypi.org/project/canonicaljson/>)
    * hash it with SHA256
    * interpret the byte string as a big-endian integer
    * reduce it mod q
    """
    return int.from_bytes(
            hashlib.sha256(canonicaljson.dumps(json.loads(json_obj))).digest(),
            byteorder='big'
            ) % q


def verify_ballot(pk, ballot):
    """Verify that the '0 or 1' disjunctive proof for the ballot is correct.

    :param pk: Public key of the election
    :type pk: elgamal.ElgamalPublicKey
    :param ballot: JSON-encoded ballot
    :type ballot: str
    :return: Whether the proof is valid
    :rtype: bool
    """
    p, q, g = pk.G.p, pk.G.q, pk.G.g
    dec = ballot
    e = _hashg(json.dumps({
        "ct": dec["ct"],
        "commit": dec["dproof"]["commit"],
        "pk": pk.y,
        }), q)
    if (sum(dec["dproof"]["challenge"]) % q != e):
        return False
    for s in range(2):
        f = dec["dproof"]["response"][s]
        (d1, d2) = dec["dproof"]["commit"][s]
        e = dec["dproof"]["challenge"][s]
        c1 = dec["ct"]["c1"]
        c2 = dec["ct"]["c2"]
        if s == 1:
            c2 = (c2 * inverse(g, p)) % p
        if (pow(g, f, p) != (d1 * pow(c1, e, p)) % p or
                pow(pk.y, f, p) != (d2 * pow(c2, e, p)) % p):
            return False
    return True


def generate_ballot(pk, m):
    """Generate a voting ballot.

    :param pk: Public key of the election
    :type group: elgamal.ElgamalPublicKey
    :param m: Valute of the vote (0 or 1)
    :type m: int
    :return: JSON-encoded ballot
    :rtype: str
    """
    assert m in (0, 1)
    def _sort(x, y):
        return (x, y) if m == 0 else (y, x)
    p = pk.G.p
    g = pk.G.g
    q = pk.G.q
    # encrypt
    # We cannot use pk.encrypt(m) since we need to know the randomness used.
    r = pk.G.random_exp()
    c1 = pow(g, r, p)
    c2 = (pow(g, m, p) * pow(pk.y, r, p)) % p
    ct = (c1, c2)
    # simulated proof
    e_sim = pk.G.random_exp()
    f_sim = pk.G.random_exp()
    s_sim = (c2*inverse(pow(g, 1-m, p), p)) % p
    d_sim = (
            (pow(g, f_sim, p)*inverse(pow(c1, e_sim, p), p)) % p,
            (pow(pk.y, f_sim, p)*inverse(pow(s_sim, e_sim, p), p)) % p,
            )
    # correct proof
    z = pk.G.random_exp()
    d_true = (pow(g, z, p), pow(pk.y, z, p))
    e = _hashg(json.dumps({
            "ct": {"c1": c1, "c2": c2},
            "commit": _sort(d_true, d_sim),
            "pk": pk.y,
            }), q)
    e_true = (e - e_sim) % q
    f_true = (r*e_true + z) % q
    return json.dumps({
        "ct": {"c1": c1, "c2": c2},
        "dproof": {
            "commit": _sort(d_true, d_sim),
            "challenge": _sort(e_true, e_sim),
            "response": _sort(f_true, f_sim),
            }
        })

def inverse(x, p):
    """
    @returns x^-1 in Z*_p
    """
    res = pow(x, p-2, p)
    assert (res * x) % p == 1
    return res
