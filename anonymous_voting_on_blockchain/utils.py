
import hashlib
import binascii
import canonicaljson

class InvalidBlock(Exception):
    pass


def hash_block(b):
    """Hash of the block b as a byte string."""
    return hashlib.sha256(canonicaljson.dumps(b)).digest()


def block_id(b):
    """Id of the block b (its hex-encoded SHA256 hash)."""
    return binascii.hexlify(hash_block(b)).decode("ascii")


_MSK = (0x0, 0x80, 0xC0, 0xE0, 0xF0, 0xF8, 0xFC, 0xFE)
def hash_nb_leading_zeroes(h, nb):
    """Tests if the byte string hash starts with nb bits sets at 0."""
    off = 0
    while nb >= 8:
        if h[off] != 0:
            return False
        nb -= 8
        off += 1
    return (h[off] & _MSK[nb]) == 0


def pow_check(block, diff):
    """Test if the proof of work is correct."""
    return hash_nb_leading_zeroes(hash_block(block), diff)


def parse_struct(obj, template, **kwargs):
    """Parse an blockchain-encoded datastructure obj as a python object
    matching the template.
    If obj cannot be parsed as the template, raise an InvalidBlock exception.

    Note for students: you don't need to understand the implementation of this
    function. (It is a bit complex, but actually abstracts and automates the
    boring task of parsing a recursive dict/list-based datastructure into
    custom objects.)
    """
    try:
        tt = type(template)
        if tt == dict:
            if type(obj) != dict or set(obj.keys()) != set(template.keys()):
                raise InvalidBlock((obj, template))
            return {k: parse_struct(obj[k], t) for k, t in template.items()}
        elif tt in (list, tuple):
            if type(obj) not in (list, tuple) or len(obj) != len(template):
                raise InvalidBlock((obj, template))
            return tt(parse_struct(o, t) for o, t in zip(obj, template))
        elif template in (int, str):
            if type(obj) == template:
                return obj
            else:
                raise InvalidBlock((obj, template))
        elif hasattr(template, "from_enc"):
            return template.from_enc(obj)
        elif callable(template):
            return template(obj)
        else:
            raise InvalidBlock(template)
    except InvalidBlock as e:
        raise e
    except Exception as e:
        raise InvalidBlock((obj, template))


def is_list_of_2_int(lst):
    """Check List in format [int, int]"""
    return (
        type(lst) == list and
        all(isinstance(x, int) for x in lst) and
        len(lst) == 2
    )