import socket
from threading import Thread, Lock
from socketserver import ThreadingMixIn

import sqlite3
from time import sleep


class Movie:

    def __init__(self, movie_id, name, image_url, description):
        self.movie_id = movie_id
        self.name = name
        self.image_url = image_url
        self.description = description

    def get_message(self, msg_id):
        msg = msg_id.to_bytes(4, 'big') + \
              len(self.name).to_bytes(4, 'big') + \
              len(self.image_url).to_bytes(4, 'big') + \
              self.movie_id.to_bytes(4, 'big') + \
              bytes(self.name + self. image_url + self.description, 'utf-8')

        msg = len(msg).to_bytes(4, 'big') + msg

        return msg


# Multi-threaded Python server : TCP Server Socket Thread Pool
class ClientThread(Thread):

    def __init__(self, ip, port, client_socket):
        Thread.__init__(self)
        self.ip = ip
        self.port = port
        self.client_socket = client_socket
        print("[+] New server socket thread started for " + ip + ":" + str(port))

    def run(self):
        self.acknowledge_movie_list()
        self.send_movie_list()

    def acknowledge_movie_list(self):
        data = self.client_socket.recv(8, socket.MSG_WAITALL)
        print("Server received data:", data)

    def send_movie_list(self):
        msg_id = 2

        db = sqlite3.connect('cinema.db')
        db_cursor = db.cursor()
        db_cursor.execute("SELECT * FROM movies")
        rows = db_cursor.fetchall()

        movies = []
        for row in rows:
            movies.append(Movie(row[0], row[1], row[2], row[3]))

        for movie in movies:
            self.client_socket.sendall(movie.get_message(msg_id))

        end_msg = (msg_id + 1).to_bytes(4, 'big')
        end_msg = len(end_msg).to_bytes(4, 'big') + end_msg
        self.client_socket.sendall(end_msg)

        print("Successfully sent movie list")

        db_cursor.close()
        db.close()


# Multi-threaded Python server : TCP Server Socket Program Stub
HOST_IP = 'localhost'
PORT = 2004
BUFFER_SIZE = 20  # Usually 1024, but we need quick response

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
server.bind((HOST_IP, PORT))
threads = []

server.listen(4)
print("Listening...")

while True:
    (client_socket, (ip, port)) = server.accept()

    new_thread = ClientThread(ip, port, client_socket)
    new_thread.start()
    threads.append(new_thread)

# for t in threads:
#     t.join()
