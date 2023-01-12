
import json
from http.server import BaseHTTPRequestHandler, HTTPServer
import cgi

from client import Message, Data

class ProjectRequestHandler:
    """match the GET/POST specifications given in the project description

    @write_only_queue: Python Queue object to communicate with the client
    @data: read-only data containing references to the peer list and block list

    student: you should not need to edit any of this
    """
    def __init__(self, write_only_queue, data):
        self.data = data
        self.write_only_queue = write_only_queue

    def handle_get(self, path):
        print('Handle get, path:', path)
        #FIXME better validation of the path, not just 'in'
        if "list_blocks" in path:
            return json.dumps(list(self.data.blocks.keys()))
        elif "peers" in path:
            return json.dumps(list(self.data.peers.keys()))
        elif "blocks" in path:
            ## this should /blocks/blockid1,blockid2,blockid3 ...
            print(path)
            blockids = path.split("/")[-1].split(",")
            blocks = {}
            for blockid in blockids:
                if blockid in self.data.blocks:
                    blocks[blockid] = self.data.blocks[blockid]
            return json.dumps(blocks)
        elif "election_id" in path:
            if self.data.election_id is None:
                return json.dumps({"error": "Oups, we're probably mining the genesis block; try again in a few minutes!"})
            else:
                return json.dumps([self.data.election_id])
        else:
            return json.dumps({})

    def handle_post(self, message, path):
        if message['sender'] not in self.data.peers:
            self.write_only_queue.put(Message(message['sender'], 'peer'))
        if 'block' in message:
            self.write_only_queue.put(Message(message['block'], 'block'))
        return json.dumps(list(self.data.peers.keys()))


def makeHandlerHTTPServer(write_only_queue, data):
    """Class factory method to instanciate an http server with extra
    parameters.

    @write_only_queue: Python Queue object to communicate with the client
    @data: read-only data containing references to the peer list and block list

    student: you should not need to edit any of this
    """
    class Server(BaseHTTPRequestHandler):
        def __init__(self, *args, **kwargs):
            self.prReqHandler = ProjectRequestHandler(write_only_queue, data)
            super().__init__(*args, **kwargs)

        def _set_headers(self):
            """
                We only manipulate json data
            """
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()

        def do_GET(self):
            json_response = self.prReqHandler.handle_get(self.path)
            # Sending back
            self._set_headers()
            self.wfile.write(json_response.encode())

        def do_POST(self):
            # Check MIME header whether we have json
            ctype, pdict = cgi.parse_header(self.headers.get('content-type'))
            if ctype != 'application/json':
                self.send_response(415)
                self.end_headers()
                return
            length = int(self.headers.get('content-length'))
            message = json.loads(self.rfile.read(length).decode("utf-8"))
            #print(message)
            response = self.prReqHandler.handle_post(message, self.path)
            self._set_headers()
            self.wfile.write(response.encode())

    return Server

def server_start(write_only_queue, data, port=1234):
    server_address = ('127.0.0.1', port)
    Server = makeHandlerHTTPServer(write_only_queue, data)
    httpd = HTTPServer(server_address, Server)
    print("Server slowly starting .... just kidding, I am up.")
    httpd.serve_forever()

